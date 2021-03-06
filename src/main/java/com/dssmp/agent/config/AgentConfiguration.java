package com.dssmp.agent.config;

import com.amazonaws.*;

import java.nio.file.Path;
import java.nio.file.Paths;
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
public class AgentConfiguration extends Configuration {

    static final String[] VALID_LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};
    static final long DEFAULT_SENDING_THREADS_KEEPALIVE_MILLIS = 60_000L;
    static final int DEFAULT_SENDING_THREADS_MAX_QUEUE_SIZE = 100;
    static final String DEFAULT_CHECKPOINTS_FILE = "/var/run/agent/checkpoints";
    static final int DEFAULT_MAX_SENDING_THREADS_PER_CORE = 12;

    static final int DEFAULT_CW_QUEUE_SIZE = 10_000;
    static final boolean DEFAULT_CW_EMIT_METRICS = true;
    static final long DEFAULT_CW_BUFFER_TIME_MILLIS = 30_000L;
    static final boolean DEFAULT_LOG_EMIT_METRICS = false;
    static final boolean DEFAULT_LOG_EMIT_INTERNAL_METRICS = false;
    static final int DEFAULT_LOG_STATUS_REPORTING_PERIOD_SECONDS = 30;
    static final int DEFAULT_CHECKPOINT_TTL_DAYS = 7;

    // NOTE: If changing the default make sure to change SHUTDOWN_TIME variable in `bin/aws-kinesis-agent` as well...
    static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 10_000L;

    private static final String CW_DEFAULT_NAMESPACE = "AWSKinesisAgent";
    public static final String CONFIG_ACCESS_KEY = "awsAccessKeyId";
    public static final String CONFIG_SECRET_KEY = "awsSecretAccessKey";
    public static final String SHUTDOWN_TIMEOUT_MILLIS_KEY = "shutdownTimeoutMillis";
    public static final String ENDPOINT_KEY = "endpoint";
    static final String LOG_FILE_KEY = "log.file";
    static final String LOG_LEVEL_KEY = "log.level";
    static final String LOG_MAX_BACKUP_INDEX_KEY = "log.maxBackupIndex";
    static final String LOG_MAX_FILE_SIZE_KEY = "log.maxFileSize";

    public AgentConfiguration(Map<String, Object> config) {
        super(config);
    }

    public long shutdownTimeoutMillis() {
        return readLong(SHUTDOWN_TIMEOUT_MILLIS_KEY, DEFAULT_SHUTDOWN_TIMEOUT_MILLIS);
    }

    public String accessKeyId() {
        return this.readString(CONFIG_ACCESS_KEY, null);
    }

    public String secretKey() {
        return this.readString(CONFIG_SECRET_KEY, null);
    }

    public AgentConfiguration(Configuration config) {
        this(config.getConfigMap());
    }

    public String logLevel() {
        return readString(LOG_LEVEL_KEY, null);
    }

    public Path logFile() {
        return readPath(LOG_FILE_KEY, null);
    }

    public int logMaxBackupIndex() {
        return readInteger(LOG_MAX_BACKUP_INDEX_KEY, -1);
    }

    public long logMaxFileSize() {
        return readLong(LOG_MAX_FILE_SIZE_KEY, -1L);
    }

    public boolean logEmitMetrics() {
        return this.readBoolean("log.emitMetrics",
                DEFAULT_LOG_EMIT_METRICS);
    }

    public boolean logEmitInternalMetrics() {
        return this.readBoolean("log.emitInternalMetrics",
                DEFAULT_LOG_EMIT_INTERNAL_METRICS);
    }

    public int logStatusReportingPeriodSeconds() {
        return this.readInteger("log.statusReportingPeriodSeconds",
                DEFAULT_LOG_STATUS_REPORTING_PERIOD_SECONDS);
    }

    public boolean cloudwatchEmitMetrics() {
        return this.readBoolean("cloudwatch.emitMetrics",
                DEFAULT_CW_EMIT_METRICS);
    }

    public long cloudwatchMetricsBufferTimeMillis() {
        return this.readLong(
                "cloudwatch.metricsBufferTimeMillis",
                DEFAULT_CW_BUFFER_TIME_MILLIS);
    }

    public String cloudwatchNamespace() {
        return this.readString("cloudwatch.namespace", CW_DEFAULT_NAMESPACE);
    }

    public int cloudwatchMetricsQueueSize() {
        return this.readInteger("cloudwatch.metricsQueueSize",
                DEFAULT_CW_QUEUE_SIZE);
    }

    public int maxSendingThreads() {
        return this.readInteger("maxSendingThreads",
                defaultMaxSendingThreads());
    }

    public long sendingThreadsKeepAliveMillis() {
        return this.readLong("sendingThreadsKeepAliveMillis",
                DEFAULT_SENDING_THREADS_KEEPALIVE_MILLIS);
    }

    public int sendingThreadsMaxQueueSize() {
        return this.readInteger("sendingThreadsMaxQueueSize",
                DEFAULT_SENDING_THREADS_MAX_QUEUE_SIZE);
    }

    public int maxSendingThreadsPerCore() {
        return readInteger("maxSendingThreadsPerCore", DEFAULT_MAX_SENDING_THREADS_PER_CORE);
    }

    public int defaultMaxSendingThreads() {
        return Math.max(2,
                maxSendingThreadsPerCore() * Runtime.getRuntime().availableProcessors());
    }

    public Path checkpointFile() {
        return this.readPath("checkpointFile",
                Paths.get(DEFAULT_CHECKPOINTS_FILE));
    }

    public int checkpointTimeToLiveDays() {
        return this.readInteger("checkpointTimeToLiveDays",
                DEFAULT_CHECKPOINT_TTL_DAYS);
    }

    public String kinesisEndpoint() {
        return this.readString("kinesis." + ENDPOINT_KEY, null);
    }

    public String firehoseEndpoint() {
        return this.readString("firehose." + ENDPOINT_KEY, null);
    }

    public String cloudwatchEndpoint() {
        return this.readString("cloudwatch." + ENDPOINT_KEY, null);
    }

    public int maxConnections() {
        return readInteger("maxConnections", defaultMaxConnections());
    }

    public int defaultMaxConnections() {
        return Math.max(com.amazonaws.ClientConfiguration.DEFAULT_MAX_CONNECTIONS, maxSendingThreads());
    }

    public int connectionTimeoutMillis() {
        return readInteger("connectionTimeoutMillis", com.amazonaws.ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT);
    }

    public long connectionTTLMillis() {
        return readLong("connectionTTLMillis", com.amazonaws.ClientConfiguration.DEFAULT_CONNECTION_TTL);
    }

    public int socketTimeoutMillis() {
        return readInteger("socketTimeoutMillis", com.amazonaws.ClientConfiguration.DEFAULT_SOCKET_TIMEOUT);
    }

    public boolean useTcpKeepAlive() {
        return readBoolean("useTcpKeepAlive", com.amazonaws.ClientConfiguration.DEFAULT_TCP_KEEP_ALIVE);
    }

    public boolean useHttpGzip() {
        return readBoolean("useHttpGzip", com.amazonaws.ClientConfiguration.DEFAULT_USE_GZIP);
    }

}
