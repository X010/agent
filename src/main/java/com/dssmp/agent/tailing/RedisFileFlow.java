package com.dssmp.agent.tailing;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.config.Configuration;
import com.dssmp.agent.tailing.checkpoints.FileCheckpointStore;
import com.google.common.collect.Range;
import lombok.ToString;

import java.io.IOException;
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
@ToString(callSuper = true)
public class RedisFileFlow extends FileFlow<KinesisRecord>  {

    protected RedisFileFlow(AgentContext context, Configuration config) {
        super(context, config);
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getDestination() {
        return null;
    }

    @Override
    public int getMaxRecordSizeBytes() {
        return 0;
    }

    @Override
    public int getPerRecordOverheadBytes() {
        return 0;
    }

    @Override
    public int getPerBufferOverheadBytes() {
        return 0;
    }

    @Override
    protected FileTailer<KinesisRecord> createNewTailer(FileCheckpointStore checkpoints, ExecutorService sendingExecutor) throws IOException {
        return null;
    }

    @Override
    protected AsyncPublisherService<KinesisRecord> getPublisher(FileCheckpointStore checkpoints, ExecutorService sendingExecutor) {
        return null;
    }

    @Override
    protected SourceFileTracker buildSourceFileTracker() throws IOException {
        return null;
    }

    @Override
    protected IParser<KinesisRecord> buildParser() {
        return null;
    }

    @Override
    protected ISender<KinesisRecord> buildSender() {
        return null;
    }

    @Override
    public int getParserBufferSize() {
        return 0;
    }

    @Override
    protected Range<Long> getWaitOnEmptyPublishQueueMillisValidRange() {
        return null;
    }

    @Override
    protected long getDefaultWaitOnEmptyPublishQueueMillis() {
        return 0;
    }

    @Override
    protected Range<Long> getWaitOnPublishQueueMillisValidRange() {
        return null;
    }

    @Override
    protected long getDefaultWaitOnPublishQueueMillis() {
        return 0;
    }

    @Override
    protected Range<Integer> getMaxBufferSizeBytesValidRange() {
        return null;
    }

    @Override
    protected int getDefaultMaxBufferSizeBytes() {
        return 0;
    }

    @Override
    protected Range<Integer> getBufferSizeRecordsValidRange() {
        return null;
    }

    @Override
    protected int getDefaultBufferSizeRecords() {
        return 0;
    }

    @Override
    protected Range<Long> getMaxBufferAgeMillisValidRange() {
        return null;
    }

    @Override
    protected long getDefaultMaxBufferAgeMillis() {
        return 0;
    }

    @Override
    protected long getDefaultRetryInitialBackoffMillis() {
        return 0;
    }

    @Override
    protected long getDefaultRetryMaxBackoffMillis() {
        return 0;
    }

    @Override
    protected int getDefaultPublishQueueCapacity() {
        return 0;
    }
}
