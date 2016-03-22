package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.config.Configuration;
import com.dssmp.agent.tailing.checkpoints.FileCheckpointStore;
import com.google.common.collect.Range;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
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
@ToString(callSuper = true)
public class FirehoseFileFlow extends FileFlow<FirehoseRecord> {
    public static final Range<Long> VALID_MAX_BUFFER_AGE_RANGE_MILLIS = Range.closed(
            TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(15));
    public static final Range<Integer> VALID_MAX_BUFFER_SIZE_RECORDS_RANGE = Range.closed(1, FirehoseConstants.MAX_BUFFER_SIZE_RECORDS);
    public static final Range<Integer> VALID_MAX_BUFFER_SIZE_BYTES_RANGE = Range.closed(1, FirehoseConstants.MAX_BUFFER_SIZE_BYTES);
    public static final Range<Long> VALID_WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS_RANGE = Range.closed(
            TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(15));
    public static final Range<Long> VALID_WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS_RANGE = Range.closed(
            TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(15));

    @Getter
    protected final String id;
    @Getter
    protected final String destination;

    public FirehoseFileFlow(AgentContext context, Configuration config) {
        super(context, config);
        destination = readString(FirehoseConstants.DESTINATION_KEY);
        id = "fh:" + destination + ":" + sourceFile.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDestination() {
        return this.destination;
    }

    @Override
    public int getPerRecordOverheadBytes() {
        return FirehoseConstants.PER_RECORD_OVERHEAD_BYTES;
    }

    @Override
    public int getMaxRecordSizeBytes() {
        return FirehoseConstants.MAX_RECORD_SIZE_BYTES;
    }

    @Override
    public int getPerBufferOverheadBytes() {
        return FirehoseConstants.PER_BUFFER_OVERHEAD_BYTES;
    }

    @Override
    protected FileTailer<FirehoseRecord> createNewTailer(
            FileCheckpointStore checkpoints,
            ExecutorService sendingExecutor) throws IOException {
        SourceFileTracker fileTracker = buildSourceFileTracker();
        AsyncPublisherService<FirehoseRecord> publisher = getPublisher(checkpoints, sendingExecutor);
        return new FileTailer<FirehoseRecord>(
                agentContext, this, fileTracker,
                publisher, buildParser(), checkpoints);
    }

    @Override
    protected SourceFileTracker buildSourceFileTracker() throws IOException {
        return new SourceFileTracker(agentContext, this);
    }

    @Override
    protected AsyncPublisherService<FirehoseRecord> getPublisher(
            FileCheckpointStore checkpoints,
            ExecutorService sendingExecutor) {
        return new AsyncPublisherService<>(agentContext, this, checkpoints,
                buildSender(), sendingExecutor);
    }

    @Override
    protected IParser<FirehoseRecord> buildParser() {
        return new FirehoseParser(this, getParserBufferSize());
    }

    @Override
    protected ISender<FirehoseRecord> buildSender() {
        return new FirehoseSender(agentContext, this);
    }

    @Override
    public int getParserBufferSize() {
        return FirehoseConstants.DEFAULT_PARSER_BUFFER_SIZE_BYTES;
    }

    @Override
    protected Range<Long> getWaitOnEmptyPublishQueueMillisValidRange() {
        return VALID_WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS_RANGE;
    }

    @Override
    protected long getDefaultWaitOnEmptyPublishQueueMillis() {
        return FirehoseConstants.DEFAULT_WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS;
    }

    @Override
    protected Range<Long> getWaitOnPublishQueueMillisValidRange() {
        return VALID_WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS_RANGE;
    }

    @Override
    protected long getDefaultWaitOnPublishQueueMillis() {
        return FirehoseConstants.DEFAULT_WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS;
    }

    @Override
    protected Range<Integer> getMaxBufferSizeBytesValidRange() {
        return VALID_MAX_BUFFER_SIZE_BYTES_RANGE;
    }

    @Override
    protected int getDefaultMaxBufferSizeBytes() {
        return FirehoseConstants.MAX_BUFFER_SIZE_BYTES;
    }

    @Override
    protected Range<Integer> getBufferSizeRecordsValidRange() {
        return VALID_MAX_BUFFER_SIZE_RECORDS_RANGE;
    }

    @Override
    protected int getDefaultBufferSizeRecords() {
        return FirehoseConstants.MAX_BUFFER_SIZE_RECORDS;
    }

    @Override
    protected Range<Long> getMaxBufferAgeMillisValidRange() {
        return VALID_MAX_BUFFER_AGE_RANGE_MILLIS;
    }

    @Override
    protected long getDefaultMaxBufferAgeMillis() {
        return FirehoseConstants.DEFAULT_MAX_BUFFER_AGE_MILLIS;
    }

    @Override
    public long getDefaultRetryInitialBackoffMillis() {
        return FirehoseConstants.DEFAULT_RETRY_INITIAL_BACKOFF_MILLIS;
    }

    @Override
    public long getDefaultRetryMaxBackoffMillis() {
        return FirehoseConstants.DEFAULT_RETRY_MAX_BACKOFF_MILLIS;
    }

    @Override
    public int getDefaultPublishQueueCapacity() {
        return FirehoseConstants.DEFAULT_PUBLISH_QUEUE_CAPACITY;
    }
}