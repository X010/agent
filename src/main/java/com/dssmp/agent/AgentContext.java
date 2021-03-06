package com.dssmp.agent;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.dssmp.agent.config.AgentConfiguration;
import com.dssmp.agent.config.Configuration;
import com.dssmp.agent.config.ConfigurationException;
import com.dssmp.agent.metrics.IMetricsContext;
import com.dssmp.agent.metrics.IMetricsScope;
import com.dssmp.agent.metrics.Metrics;
import com.dssmp.agent.tailing.FileFlow;
import com.dssmp.agent.tailing.FileFlowFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
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
public class AgentContext  extends AgentConfiguration implements IMetricsContext {

    private static final Logger LOGGER = Logging.getLogger(AgentContext.class);

    @VisibleForTesting
    static final String DEFAULT_USER_AGENT = "aws-kinesis-agent";

    @VisibleForTesting
    public final FileFlowFactory fileFlowFactory;

    /** The listing of flows, ordered in order of appearance in configuration */
    private final Map<String, FileFlow<?>> flows = new LinkedHashMap<>();
    private AmazonKinesisFirehose firehoseClient;
    private AmazonKinesisClient kinesisClient;
    private AmazonCloudWatch cloudwatchClient;
    private IMetricsContext metrics;
    /**
     *
     * @param configuration
     * @throws ConfigurationException
     */
    public AgentContext(Configuration configuration) {
        this(configuration, new FileFlowFactory());
    }

    /**
     * @param configuration
     * @param fileFlowFactory
     * @throws ConfigurationException
     */
    public AgentContext(Configuration configuration, FileFlowFactory fileFlowFactory) {
        super(configuration);
        this.fileFlowFactory = fileFlowFactory;
        if (containsKey("flows")) {
            for (Configuration c : readList("flows", Configuration.class)) {
                FileFlow<?> flow = fileFlowFactory.getFileFlow(this, c);
                if (flows.containsKey(flow.getId()))
                    throw new ConfigurationException("Duplicate flow: " + flow.getId());
                flows.put(flow.getId(), flow);
            }
        }
    }


    /**
     * @return the version of this build.
     */
    public String version() {
        final String VERSION_INFO_FILE = "versionInfo.properties";
        try (InputStream versionInfoStream = Logging.class.getResourceAsStream(VERSION_INFO_FILE)) {
            Properties versionInfo = new Properties();
            versionInfo.load(versionInfoStream);
            return versionInfo.getProperty("version");
        } catch (IOException e) {
            LOGGER.error("Failed to read agent version from " + VERSION_INFO_FILE, e);
            return "x.x";
        }
    }

    /**
     * @return A new instance of a threadpool executor for sending data to
     *         destination.
     */
    public ThreadPoolExecutor createSendingExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("sender-%d").build();
        ThreadPoolExecutor tp = new ThreadPoolExecutor(maxSendingThreads(),
                maxSendingThreads(), sendingThreadsKeepAliveMillis(), TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(sendingThreadsMaxQueueSize()), threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        tp.allowCoreThreadTimeOut(true);
        return tp;
    }

    /**
     * @param config
     * @return the user agent component for this build.
     */
    public String userAgent(ClientConfiguration config) {
        if (containsKey("userAgentOverride")) {
            return readString("userAgentOverride");
        } else {
            String userAgentString = DEFAULT_USER_AGENT + "/" + version();
            if (config != null) {
                userAgentString += " " + config.getUserAgent();
            }
            String customAgent = readString("userAgent", null);
            if (customAgent != null && !customAgent.trim().isEmpty()) {
                userAgentString = customAgent.trim() + " " + userAgentString;
            }
            return userAgentString;
        }
    }

    @VisibleForTesting
    public synchronized AmazonKinesisFirehose getFirehoseClient() {
        if (firehoseClient == null) {
            firehoseClient = new AmazonKinesisFirehoseClient(
                    getAwsCredentialsProvider(), getAwsClientConfiguration());
            if (!Strings.isNullOrEmpty(firehoseEndpoint()))
                firehoseClient.setEndpoint(firehoseEndpoint());
        }
        return firehoseClient;
    }

    public synchronized AmazonKinesisClient getKinesisClient() {
        if (kinesisClient == null) {
            kinesisClient = new AmazonKinesisClient(
                    getAwsCredentialsProvider(), getAwsClientConfiguration());
            if (!Strings.isNullOrEmpty(kinesisEndpoint()))
                kinesisClient.setEndpoint(kinesisEndpoint());
        }
        return kinesisClient;
    }

    private synchronized IMetricsContext getMetricsContext() {
        if(metrics == null) {
            metrics = new Metrics(this);
        }
        return metrics;
    }

    public AmazonCloudWatch getCloudWatchClient() {
        if (cloudwatchClient == null) {
            cloudwatchClient = new AmazonCloudWatchClient(
                    getAwsCredentialsProvider(), getAwsClientConfiguration());
            if (!Strings.isNullOrEmpty(cloudwatchEndpoint()))
                cloudwatchClient.setEndpoint(cloudwatchEndpoint());
        }
        return cloudwatchClient;
    }

    public AWSCredentialsProvider getAwsCredentialsProvider() {
        return new AgentAWSCredentialsProviderChain(this);
    }

    public ClientConfiguration getAwsClientConfiguration() {
        ClientConfiguration config = new ClientConfiguration();
        config.setUserAgent(userAgent(config));
        config.setMaxConnections(maxConnections());
        config.setConnectionTimeout(connectionTimeoutMillis());
        config.setSocketTimeout(socketTimeoutMillis());
        config.setUseTcpKeepAlive(useTcpKeepAlive());
        config.setConnectionTTL(connectionTTLMillis());
        config.setUseGzip(useHttpGzip());
        return config;
    }

    public synchronized FileFlow<?> flow(String flowId) {
        return flows.get(flowId);
    }

    public synchronized List<FileFlow<?>> flows() {
        return new ArrayList<>(flows.values());
    }

    @Override
    public IMetricsScope beginScope() {
        return getMetricsContext().beginScope();
    }
}
