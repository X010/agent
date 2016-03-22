package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.IHeartbeatProvider;
import com.dssmp.agent.Logging;
import com.dssmp.agent.tailing.checkpoints.Checkpointer;
import com.dssmp.agent.tailing.checkpoints.FileCheckpointStore;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.channels.ClosedByInterruptException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class SimplePublisher<R extends IRecord> implements IHeartbeatProvider {
    protected final Logger logger;
    protected final String name;

    @VisibleForTesting
    final Checkpointer<R> checkpointer;
    protected final ISender<R> sender;
    protected volatile boolean isOpen = true;

    @Getter
    final AgentContext agentContext;
    @Getter
    final FileFlow<R> flow;
    @Getter
    final PublishingQueue<R> queue;

    private final AtomicLong sendSuccess = new AtomicLong();
    private final AtomicLong sendPartialSuccess = new AtomicLong();
    private final AtomicLong sendError = new AtomicLong();
    private final AtomicLong buffersDropped = new AtomicLong();
    private final AtomicLong totalSentBuffers = new AtomicLong();

    /**
     * @param agentContext
     * @param flow
     * @param checkpoints
     * @param sender
     */
    public SimplePublisher(
            AgentContext agentContext,
            FileFlow<R> flow,
            FileCheckpointStore checkpoints,
            ISender<R> sender) {
        this.logger = Logging.getLogger(getClass());
        this.agentContext = agentContext;
        this.flow = flow;
        this.queue = new PublishingQueue<>(flow, flow.getPublishQueueCapacity());
        this.sender = sender;
        this.checkpointer = new Checkpointer<>(this.flow, checkpoints);
        this.name = getClass().getSimpleName() + "[" + flow.getId() + "]";
    }

    public String name() {
        return name;
    }

    public void close() {
        isOpen = false;
        queue.close();
        queue.discardAllRecords();
    }

    /**
     * Returns immediately if the record could not be published because the
     * queue is full (or if publisher is shutting down).
     *
     * @param record
     * @param block
     * @return {@code true} if the record was successfully added to the
     * current buffer, and {@code false} otherwise.
     */
    public boolean publishRecord(R record) {
        if (isOpen && queue.offerRecord(record, false)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Flushes any buffered records and makes them available for publishing.
     */
    public void flush() {
        queue.flushPendingRecords();
    }

    public RecordBuffer<R> pollNextBuffer(boolean block) {
        return queue.take(block);
    }

    public boolean sendNextBufferSync(boolean block) {
        final RecordBuffer<R> buffer = pollNextBuffer(block);
        if (buffer != null) {
            sendBufferSync(buffer);
            return true;
        } else
            return false;
    }

    public void sendBufferSync(RecordBuffer<R> buffer) {
        BufferSendResult<R> result = null;
        try {
            result = sender.sendBuffer(buffer);
        } catch (Throwable t) {
            onSendError(buffer, t);
            return;
        }
        totalSentBuffers.incrementAndGet();
        switch (result.getStatus()) {
            case SUCCESS:
                onSendSuccess(buffer);
                break;
            case PARTIAL_SUCCESS:
                onSendPartialSuccess(buffer, result);
                break;
        }
    }

    protected boolean queueBufferForRetry(RecordBuffer<R> buffer) {
        if (isOpen) {
            if (queue.queueBufferForRetry(buffer)) {
                logger.trace("{}:{} Buffer Queued for Retry", name(), buffer);
                return true;
            } else {
                onBufferDropped(buffer, "retry rejected by queue");
                return false;
            }
        } else {
            onBufferDropped(buffer, "retry rejected: publisher is closed");
            return false;
        }
    }

    protected void onBufferDropped(RecordBuffer<R> buffer, String reason) {
        buffersDropped.incrementAndGet();
        logger.trace("{}:{} Buffer Dropped: {}", name(), reason, buffer);
    }

    /**
     * This method should not raise any exceptions.
     *
     * @param buffer
     */
    protected void onSendSuccess(RecordBuffer<R> buffer) {
        sendSuccess.incrementAndGet();
        logger.trace("{}:{} Send SUCCESS", name(), buffer);
        try {
            checkpointer.saveCheckpoint(buffer);
        } catch (Exception e) {
            logger.error("{}:{} Error in onSendSuccess", name(), buffer, e);
        }
    }

    /**
     * This method should not raise any exceptions.
     *
     * @param buffer
     * @param result
     * @return {@code true} if buffer was requed for retrying, {@code false}
     * if not for any reason (e.g. queue is closed).
     */
    protected boolean onSendPartialSuccess(RecordBuffer<R> buffer, BufferSendResult<R> result) {
        sendPartialSuccess.incrementAndGet();
        logger.debug("{}:{} Send PARTIAL_SUCCESS: Sent: {}, Failed: {}", name(),
                buffer, result.sentRecordCount(), result.remainingRecordCount());
        return queueBufferForRetry(buffer);
    }

    /**
     * This method should not raise any exceptions.
     *
     * @param buffer
     * @param t
     * @return {@code true} if buffer was requed for retrying, {@code false}
     * if not for any reason (e.g. queue is closed, error is
     * non-retriable).
     */
    protected boolean onSendError(RecordBuffer<R> buffer, Throwable t) {
        sendError.incrementAndGet();
        // Retry the buffer if it's a runtime exception
        if (isRetriableSendException(t)) {
            logger.debug("{}:{} Retriable send error ({}: {}). Will retry.", name(), buffer, t.getClass().getName(), t.getMessage());
            return queueBufferForRetry(buffer);
        } else {
            logger.error("{}:{} Non-retriable send error. Will NOT retry.", name(), buffer, t);
            onBufferDropped(buffer, "non-retriable exception (" + t.getClass().getName() + ")");
            return false;
        }
    }

    protected boolean isRetriableSendException(Throwable t) {
        return !(t instanceof NullPointerException) &&
                !(t instanceof IllegalArgumentException) &&
                !(t instanceof IllegalStateException) &&
                !(t instanceof ClassCastException) &&
                !(t instanceof IndexOutOfBoundsException) &&
                !(t instanceof SecurityException) &&
                !(t instanceof UnsupportedOperationException) &&
                !(t instanceof ClosedByInterruptException)
                && (t.getCause() == null || isRetriableSendException(t.getCause()));
    }

    @Override
    public Object heartbeat(AgentContext agent) {
        return queue.heartbeat(agent);
    }

    // Use for debugging only please.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
                .append("(")
                .append("queue=").append(queue)
                .append(")");
        return sb.toString();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = queue.getMetrics();
        metrics.putAll(sender.getMetrics());
        metrics.put("SimplePublisher.TotalSentBuffers", totalSentBuffers);
        metrics.put("SimplePublisher.SendSuccess", sendSuccess);
        metrics.put("SimplePublisher.SendPartialSuccess", sendPartialSuccess);
        metrics.put("SimplePublisher.SendError", sendError);
        metrics.put("SimplePublisher.BuffersDropped", buffersDropped);
        return metrics;
    }
}
