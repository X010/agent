package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;

import java.util.Map;

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
public interface ISender<R extends IRecord> {
    /**
     * Sends the buffer to the implementation-specific destination.
     * The buffer will be modified by this method and after it returns it will
     * have all the records that were succeesfully sent to be removed, and what
     * remains are records that were not successfully committed to destination.
     * More specifically, if the status of the {@link BufferSendResult}
     * is:
     * <ul>
     * <li>{@code SUCEESS}: then the buffer is expected to be empty after the
     * call.</li>
     * <li>{@code ERROR}: then the buffer is expected to be unchanged after
     * the call, and it would contain the same records as before.</li>
     * <li>{@code PARTIAL_FAILURE}: then the buffer is expected to have some
     * records left in it, but the records that were committed are
     * removed.</li>
     * </ul>
     * Note that the {@link RecordBuffer#checkpointFile() buffer.checkpointFile()}
     * and {@link RecordBuffer#checkpointOffset() buffer.checkpointOffset()} will
     * not be changed in any of the cases above.
     * <p>
     * These semantics make it safe to retry a buffer by calling this method
     * again with the same instance (which is also referenced in
     * {@link BufferSendResult#getBuffer() result.getBuffer()}).
     *
     * @param buffer The buffer to send to destination. Will be modified by this
     *               call by removing all records that were succeeffully committed to
     *               the destination.
     * @return The result of the send operation. The
     * {@link BufferSendResult#getBuffer() result.getBuffer()} is a
     * reference to the input parameter.
     * @throws IllegalArgumentException
     */
    BufferSendResult<R> sendBuffer(RecordBuffer<R> buffer);

    /**
     * @return The agent context for this sender.
     */
    AgentContext getAgentContext();

    /**
     * @return The destination where the sender is targeting
     */
    String getDestination();

    Map<String, Object> getMetrics();
}

