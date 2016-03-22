package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.IHeartbeatProvider;
import com.dssmp.agent.Logging;
import com.dssmp.agent.tailing.checkpoints.Checkpointer;
import com.dssmp.agent.tailing.checkpoints.FileCheckpointStore;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
public final class AsyncPublisherService<R extends IRecord> extends AbstractExecutionThreadService  implements IHeartbeatProvider {
    private static final int NO_TIMEOUT = -1;
    private static final long SHUTDOWN_MARGIN_MILLIS = 500;
    private static final Logger LOGGER = Logging.getLogger(AsyncPublisherService.class);

    private final AsyncPublisher<R> publisher;
    private Thread serviceThread;

    /**
     *
     * @param agentContext
     * @param flow
     * @param checkpoints
     * @param sender
     * @param sendingExecutor The executor that will run the async send
     *        requests.
     */
    public AsyncPublisherService(
            AgentContext agentContext,
            FileFlow<R> flow,
            FileCheckpointStore checkpoints,
            ISender<R> sender,
            ExecutorService sendingExecutor) {
        this.publisher = new AsyncPublisher<R>(agentContext, flow, checkpoints, sender, sendingExecutor);
    }

    public boolean publishRecord(R record) {
        return publisher.publishRecord(record);
    }

    @Override
    protected void run() throws Exception {
        serviceThread = Thread.currentThread();
        LOGGER.trace("{}: Main loop started", serviceName());
        do {
            runOnce();
        } while (isRunning());
    }

    protected void runOnce() {
        publisher.backoff();
        if (isRunning()) {
            publisher.sendNextBufferAsync(true);
        }
    }

    public void flush() {
        publisher.flush();
    }

    @VisibleForTesting
    PublishingQueue<R> queue() {
        return publisher.queue;
    }

    @VisibleForTesting
    Checkpointer<R> checkpointer() {
        return publisher.checkpointer;
    }

    @Override
    protected String serviceName() {
        return publisher.name();
    }

    @Override
    protected void startUp() throws Exception {
        LOGGER.debug("{}: Starting up...", serviceName());
        super.startUp();
    }

    @Override
    protected void shutDown() throws Exception {
        LOGGER.debug("{}: Shutting down...", serviceName());
        super.shutDown();
    }

    @Override
    protected void triggerShutdown() {
        super.triggerShutdown();
        // At this time, isRunning() will return false so no more records can be published
        LOGGER.debug("{}: Shutdown triggered...", serviceName());
        publisher.close();

        if (serviceThread != null)
            serviceThread.interrupt();

        // Give the senders some time to complete before cancelling everything
        LOGGER.trace("{}: Shutdown timeout: {}ms", serviceName(), getShutdownTimeoutMillis());
        waitForIdle(getShutdownTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    protected long getShutdownTimeoutMillis() {
        return publisher.agentContext.shutdownTimeoutMillis() - SHUTDOWN_MARGIN_MILLIS;
    }

    /**
     * Initializes and starts the publisher. Cannot invoke {@link #publishRecord(IRecord)}
     * before calling this method.
     */
    public void startPublisher() {
        Preconditions.checkState(!isRunning(), "%s: Publisher already running.", serviceName());
        startAsync();
        awaitRunning();
    }

    /**
     * Terminates the publisher and performs any cleanup. The implementation
     * should make sure that any pending data is sent by the time this method
     * returns. After this method returns, {@link #isIdle()} should always
     * return {@code true}, and {@link #isRunning()} will always return
     * {@code false}.
     *
     * Cannot call {@link #publishRecord(IRecord)} after calling this method.
     */
    public void stopPublisher() {
        stopPublisherAsync();
        awaitTerminated();
    }

    /**
     * Starts the termination of the publisher in an asynchronous thread and
     * returns immediately. After this method returns {@link #isRunning()} will
     * always return {@code false}.
     *
     * Cannot call {@link #publishRecord(IRecord)} after calling this method.
     */
    public void stopPublisherAsync() {
        LOGGER.debug("{}: Stopping...", serviceName());
        Preconditions.checkState(isRunning(), "%s: Publisher already stopped.", serviceName());
        stopAsync();
    }

    public boolean isIdle() {
        return publisher.isIdle();
    }

    /**
     * Wait indefinitely for the publisher to reach idle state.
     */
    public void waitForIdle() {
        waitForIdle(NO_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public boolean waitForIdle(long timeout, TimeUnit unit) {
        return publisher.waitForIdle(timeout, unit);
    }

    @Override
    public Object heartbeat(AgentContext agent) {
        return publisher.heartbeat(agent);
    }

    // Use for debugging only please.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
                .append("(isRunning=").append(isRunning())
                .append(",publisher=").append(publisher)
                .append(")");
        return sb.toString();
    }

    public Map<String, Object> getMetrics() {
        return publisher.getMetrics();
    }
}