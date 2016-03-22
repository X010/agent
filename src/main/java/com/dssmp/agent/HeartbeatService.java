package com.dssmp.agent;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Getter;

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
public abstract class HeartbeatService extends AbstractScheduledService {
    private final long period;
    private final TimeUnit periodUnit;
    private final AgentContext agent;
    @Getter
    private Object lastResult;

    public HeartbeatService(AgentContext agent, long period, TimeUnit periodUnit) {
        super();
        this.period = period;
        this.periodUnit = periodUnit;
        this.agent = agent;
    }

    @Override
    protected void runOneIteration() throws Exception {
        lastResult = heartbeat(agent);
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(period, period, periodUnit);
    }

    protected abstract Object heartbeat(AgentContext agent);
}
