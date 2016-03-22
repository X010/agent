package com.dssmp.agent;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.dssmp.agent.config.AgentConfiguration;
import com.google.common.base.Strings;
import org.slf4j.Logger;

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
public class  AgentAWSCredentialsProvider implements AWSCredentialsProvider {


    private static final Logger LOGGER = Logging.getLogger(AgentAWSCredentialsProvider.class);
    private final AgentConfiguration config;

    public AgentAWSCredentialsProvider(AgentConfiguration config) {
        this.config = config;
    }

    public AWSCredentials getCredentials() {
        String accessKeyId = config.accessKeyId();
        String secretKey = config.secretKey();

        if (!Strings.isNullOrEmpty(accessKeyId) && !Strings.isNullOrEmpty(secretKey)) {
            LOGGER.debug("Loading credentials from agent config");
            return new BasicAWSCredentials(accessKeyId, secretKey);
        }

        throw new AmazonClientException("Unable to load credentials from agent config. Missing entries: " +
                (Strings.isNullOrEmpty(accessKeyId) ? AgentConfiguration.CONFIG_ACCESS_KEY : "") +
                " " + (Strings.isNullOrEmpty(secretKey) ? AgentConfiguration.CONFIG_SECRET_KEY : ""));
    }

    public void refresh() {}

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
