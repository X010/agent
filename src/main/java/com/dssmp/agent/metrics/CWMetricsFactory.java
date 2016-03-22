package com.dssmp.agent.metrics;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

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
public class CWMetricsFactory implements IMetricsFactory {

    /*
     * If the CWPublisherRunnable accumulates more than FLUSH_SIZE distinct metrics, it will call CloudWatch
     * immediately instead of waiting for the next scheduled call.
     */
    private static final int FLUSH_SIZE = 200;
    private final CWPublisherRunnable<CWMetricKey> runnable;
    private final Thread publicationThread;

    /**
     * Constructor.
     *
     * @param credentialsProvider client credentials for CloudWatch
     * @param namespace the namespace under which the metrics will appear in the CloudWatch console
     * @param bufferTimeMillis time to buffer metrics before publishing to CloudWatch
     * @param maxQueueSize maximum number of metrics that we can have in a queue
     */
    public CWMetricsFactory(AWSCredentialsProvider credentialsProvider,
                            String namespace,
                            long bufferTimeMillis,
                            int maxQueueSize) {
        this(new AmazonCloudWatchClient(credentialsProvider), namespace, bufferTimeMillis, maxQueueSize);
    }

    /**
     * Constructor.
     *
     * @param credentialsProvider client credentials for CloudWatch
     * @param clientConfig Configuration to use with the AmazonCloudWatchClient
     * @param namespace the namespace under which the metrics will appear in the CloudWatch console
     * @param bufferTimeMillis time to buffer metrics before publishing to CloudWatch
     * @param maxQueueSize maximum number of metrics that we can have in a queue
     */
    public CWMetricsFactory(AWSCredentialsProvider credentialsProvider,
                            ClientConfiguration clientConfig,
                            String namespace,
                            long bufferTimeMillis,
                            int maxQueueSize) {
        this(new AmazonCloudWatchClient(credentialsProvider, clientConfig), namespace, bufferTimeMillis, maxQueueSize);
    }

    /**
     * Constructor.
     *
     * @param cloudWatchClient Client used to make CloudWatch requests
     * @param namespace the namespace under which the metrics will appear in the CloudWatch console
     * @param bufferTimeMillis time to buffer metrics before publishing to CloudWatch
     * @param maxQueueSize maximum number of metrics that we can have in a queue
     */
    public CWMetricsFactory(AmazonCloudWatch cloudWatchClient,
                            String namespace,
                            long bufferTimeMillis,
                            int maxQueueSize) {
        DefaultCWMetricsPublisher metricPublisher = new DefaultCWMetricsPublisher(cloudWatchClient, namespace);

        this.runnable =
                new CWPublisherRunnable<CWMetricKey>(metricPublisher, bufferTimeMillis, maxQueueSize, FLUSH_SIZE);

        this.publicationThread = new Thread(runnable);
        publicationThread.setName("cw-metrics-publisher");
        publicationThread.start();
    }

    @Override
    public IMetricsScope createScope() {
        return new CWMetricsScope(runnable);
    }

    public void shutdown() {
        runnable.shutdown();
    }

}

