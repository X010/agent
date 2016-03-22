package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.Constants;
import com.dssmp.agent.config.Configuration;
import com.dssmp.agent.config.ConfigurationException;
import com.dssmp.agent.tailing.checkpoints.FileCheckpointStore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

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
@ToString
public abstract class FileFlow<R extends IRecord> extends Configuration {
    private static final String MAX_TIME_BETWEEN_FILE_TRACKER_REFRESH_MILLIS_KEY = "maxTimeBetweenFileTrackerRefreshMillis";
    private static final long DEFAULT_MIN_TIME_BETWEEN_FILE_POLLS_MILLIS = 100L;
    private static final String MIN_TIME_BETWEEN_FILE_POLLS_MILLIS_KEY = "minTimeBetweenFilePollsMillis";
    private static final long DEFAULT_MAX_TIME_BETWEEN_FILE_TRACKER_REFRESH_MILLIS = 10_000L;
    public static final String FILE_PATTERN_KEY = "filePattern";
    public static final String MAX_BUFFER_SIZE_BYTES_KEY = "maxBufferSizeBytes";
    public static final String MAX_BUFFER_SIZE_RECORDS_KEY = "maxBufferSizeRecords";
    public static final String MAX_BUFFER_AGE_MILLIS_KEY = "maxBufferAgeMillis";
    public static final String WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS_KEY = "waitOnFullPublishQueueMillis";
    public static final String WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS_KEY = "waitOnEmptyPublishQueueMillis";
    public static final String WAIT_ON_FULL_RETRY_QUEUE_MILLIS_KEY = "waitOnFullRetryQueueMillis";
    public static final String INITIAL_POSITION_KEY = "initialPosition";
    public static final String DEFAULT_TRUNCATED_RECORD_TERMINATOR = String.valueOf(Constants.NEW_LINE);

    @Getter
    protected final AgentContext agentContext;
    @Getter
    protected final SourceFile sourceFile;
    @Getter
    protected final int maxBufferSizeRecords;
    @Getter
    protected final int maxBufferSizeBytes;
    @Getter
    protected final long maxBufferAgeMillis;
    @Getter
    protected final long waitOnFullPublishQueueMillis;
    @Getter
    protected final long waitOnEmptyPublishQueueMillis;
    @Getter
    protected final InitialPosition initialPosition;
    @Getter
    protected final int skipHeaderLines;
    @Getter
    protected FileTailer<R> tailer;
    @Getter
    protected final byte[] recordTerminatorBytes;
    @Getter
    protected ISplitter recordSplitter;
    @Getter
    protected final long retryInitialBackoffMillis;
    @Getter
    protected final long retryMaxBackoffMillis;
    @Getter
    protected final int publishQueueCapacity;

    protected FileFlow(AgentContext context, Configuration config) {
        super(config);
        this.agentContext = context;

        sourceFile = buildSourceFile();

        maxBufferAgeMillis = readLong(MAX_BUFFER_AGE_MILLIS_KEY, getDefaultMaxBufferAgeMillis());
        Configuration.validateRange(maxBufferAgeMillis, getMaxBufferAgeMillisValidRange(), MAX_BUFFER_AGE_MILLIS_KEY);
        maxBufferSizeRecords = readInteger(MAX_BUFFER_SIZE_RECORDS_KEY, getDefaultBufferSizeRecords());
        Configuration.validateRange(maxBufferSizeRecords, getBufferSizeRecordsValidRange(), MAX_BUFFER_SIZE_RECORDS_KEY);
        maxBufferSizeBytes = readInteger(MAX_BUFFER_SIZE_BYTES_KEY, getDefaultMaxBufferSizeBytes());
        Configuration.validateRange(maxBufferSizeBytes, getMaxBufferSizeBytesValidRange(), MAX_BUFFER_SIZE_BYTES_KEY);

        waitOnFullPublishQueueMillis = readLong(WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS_KEY, getDefaultWaitOnPublishQueueMillis());
        Configuration.validateRange(waitOnFullPublishQueueMillis, getWaitOnPublishQueueMillisValidRange(), WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS_KEY);

        waitOnEmptyPublishQueueMillis = readLong(WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS_KEY, getDefaultWaitOnEmptyPublishQueueMillis());
        Configuration.validateRange(waitOnEmptyPublishQueueMillis, getWaitOnEmptyPublishQueueMillisValidRange(), WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS_KEY);

        initialPosition = readEnum(InitialPosition.class, INITIAL_POSITION_KEY, InitialPosition.END_OF_FILE);

        // TODO: Add validation interval to following values
        retryInitialBackoffMillis = readLong("retryInitialBackoffMillis", getDefaultRetryInitialBackoffMillis());
        retryMaxBackoffMillis = readLong("retryMaxBackoffMillis", getDefaultRetryMaxBackoffMillis());
        publishQueueCapacity = readInteger("publishQueueCapacity", getDefaultPublishQueueCapacity());

        skipHeaderLines = readInteger("skipHeaderLines", 0);

        String pattern = readString("multiLineStartPattern", null);
        recordSplitter = Strings.isNullOrEmpty(pattern) ? new SingleLineSplitter() : new RegexSplitter(pattern);

        String terminatorConfig = readString("truncatedRecordTerminator", DEFAULT_TRUNCATED_RECORD_TERMINATOR);
        if (terminatorConfig == null || terminatorConfig.getBytes(StandardCharsets.UTF_8).length >= getMaxRecordSizeBytes()) {
            throw new ConfigurationException("Record terminator not specified or exceeds the maximum record size");
        }
        recordTerminatorBytes = terminatorConfig.getBytes(StandardCharsets.UTF_8);
    }

    public synchronized FileTailer<R> createTailer(FileCheckpointStore checkpoints, ExecutorService sendingExecutor) throws IOException {
        Preconditions.checkState(tailer == null, "Tailer for this flow is already initialized.");
        return tailer = createNewTailer(checkpoints, sendingExecutor);
    }

    public boolean logEmitInternalMetrics() {
        return this.readBoolean("log.emitInternalMetrics",
                false);
    }

    public long minTimeBetweenFilePollsMillis() {
        return readLong(MIN_TIME_BETWEEN_FILE_POLLS_MILLIS_KEY,
                DEFAULT_MIN_TIME_BETWEEN_FILE_POLLS_MILLIS);
    }

    public long maxTimeBetweenFileTrackerRefreshMillis() {
        return readLong(MAX_TIME_BETWEEN_FILE_TRACKER_REFRESH_MILLIS_KEY,
                DEFAULT_MAX_TIME_BETWEEN_FILE_TRACKER_REFRESH_MILLIS);
    }

    public abstract String getId();

    public abstract String getDestination();

    public abstract int getMaxRecordSizeBytes();

    public abstract int getPerRecordOverheadBytes();

    public abstract int getPerBufferOverheadBytes();

    protected abstract FileTailer<R> createNewTailer(
            FileCheckpointStore checkpoints,
            ExecutorService sendingExecutor) throws IOException;

    protected abstract AsyncPublisherService<R> getPublisher(
            FileCheckpointStore checkpoints,
            ExecutorService sendingExecutor);

    protected SourceFile buildSourceFile() {
        return new SourceFile(this, readString(FILE_PATTERN_KEY));
    }

    protected abstract SourceFileTracker buildSourceFileTracker() throws IOException;

    protected abstract IParser<R> buildParser();

    protected abstract ISender<R> buildSender();

    public abstract int getParserBufferSize();

    // TODO: Instead of this plethora of abstract getters, consider using
    //       tables for defaults and validation ranges.
    protected abstract Range<Long> getWaitOnEmptyPublishQueueMillisValidRange();

    protected abstract long getDefaultWaitOnEmptyPublishQueueMillis();

    protected abstract Range<Long> getWaitOnPublishQueueMillisValidRange();

    protected abstract long getDefaultWaitOnPublishQueueMillis();

    protected abstract Range<Integer> getMaxBufferSizeBytesValidRange();

    protected abstract int getDefaultMaxBufferSizeBytes();

    protected abstract Range<Integer> getBufferSizeRecordsValidRange();

    protected abstract int getDefaultBufferSizeRecords();

    protected abstract Range<Long> getMaxBufferAgeMillisValidRange();

    protected abstract long getDefaultMaxBufferAgeMillis();

    protected abstract long getDefaultRetryInitialBackoffMillis();

    protected abstract long getDefaultRetryMaxBackoffMillis();

    protected abstract int getDefaultPublishQueueCapacity();


    public AgentContext getAgentContext() {
        return agentContext;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public int getMaxBufferSizeRecords() {
        return maxBufferSizeRecords;
    }

    public int getMaxBufferSizeBytes() {
        return maxBufferSizeBytes;
    }

    public long getMaxBufferAgeMillis() {
        return maxBufferAgeMillis;
    }

    public long getWaitOnFullPublishQueueMillis() {
        return waitOnFullPublishQueueMillis;
    }

    public long getWaitOnEmptyPublishQueueMillis() {
        return waitOnEmptyPublishQueueMillis;
    }

    public InitialPosition getInitialPosition() {
        return initialPosition;
    }

    public int getSkipHeaderLines() {
        return skipHeaderLines;
    }

    public FileTailer<R> getTailer() {
        return tailer;
    }

    public void setTailer(FileTailer<R> tailer) {
        this.tailer = tailer;
    }

    public byte[] getRecordTerminatorBytes() {
        return recordTerminatorBytes;
    }

    public ISplitter getRecordSplitter() {
        return recordSplitter;
    }

    public void setRecordSplitter(ISplitter recordSplitter) {
        this.recordSplitter = recordSplitter;
    }

    public long getRetryInitialBackoffMillis() {
        return retryInitialBackoffMillis;
    }

    public long getRetryMaxBackoffMillis() {
        return retryMaxBackoffMillis;
    }

    public int getPublishQueueCapacity() {
        return publishQueueCapacity;
    }

    public static enum InitialPosition {
        START_OF_FILE,
        END_OF_FILE
    }
}
