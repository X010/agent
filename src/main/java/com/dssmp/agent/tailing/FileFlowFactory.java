package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.config.Configuration;
import com.dssmp.agent.config.ConfigurationException;

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
public class FileFlowFactory {
    /**
     *
     * @param config
     * @return
     * @throws ConfigurationException If the configuration does not correspond
     *         to a known {@link FileFlow} type.
     */
    public FileFlow<?> getFileFlow(AgentContext context, Configuration config) throws ConfigurationException {
        if(config.containsKey(FirehoseConstants.DESTINATION_KEY)) {
            return getFirehoseFileflow(context, config);
        }
        if(config.containsKey(KinesisConstants.DESTINATION_KEY)) {
            return getKinesisFileflow(context, config);
        }
        throw new ConfigurationException("Could not create flow from the given configuration. Could not recognize flow type.");
    }

    protected FirehoseFileFlow getFirehoseFileflow(AgentContext context, Configuration config) {
        return new FirehoseFileFlow(context, config);
    }

    protected KinesisFileFlow getKinesisFileflow(AgentContext context, Configuration config) {
        return new KinesisFileFlow(context, config);
    }
}
