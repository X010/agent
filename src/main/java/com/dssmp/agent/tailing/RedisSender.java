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
public class RedisSender extends AbstractSender<RedisRecord> {

    @Override
    protected long getMaxSendBatchSizeBytes() {
        return 0;
    }

    @Override
    protected int getMaxSendBatchSizeRecords() {
        return 0;
    }

    @Override
    protected BufferSendResult<RedisRecord> attemptSend(RecordBuffer<RedisRecord> buffer) {
        return null;
    }

    @Override
    public AgentContext getAgentContext() {
        return null;
    }

    @Override
    public String getDestination() {
        return null;
    }

    @Override
    public Map<String, Object> getMetrics() {
        return null;
    }
}
