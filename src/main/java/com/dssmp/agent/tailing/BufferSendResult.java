package com.dssmp.agent.tailing;

import lombok.Getter;
import lombok.ToString;

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
public class BufferSendResult<R extends IRecord> {
    public static <R extends IRecord> BufferSendResult<R> succeeded(RecordBuffer<R> buffer) {
        return new BufferSendResult<R>(Status.SUCCESS, buffer, buffer.sizeRecords());
    }

    public static <R extends IRecord> BufferSendResult<R> succeeded_partially(RecordBuffer<R> retryBuffer, int originalRecordCount) {
        return new BufferSendResult<R>(Status.PARTIAL_SUCCESS, retryBuffer, originalRecordCount);
    }

    @Getter
    private final RecordBuffer<R> buffer;
    @Getter
    private final int originalRecordCount;
    @Getter
    private final Status status;

    private BufferSendResult(Status status, RecordBuffer<R> buffer, int originalRecordCount) {
        this.buffer = buffer;
        this.originalRecordCount = originalRecordCount;
        this.status = status;
    }

    public int sentRecordCount() {
        return status == Status.SUCCESS ? originalRecordCount : (originalRecordCount - buffer.sizeRecords());
    }

    public int remainingRecordCount() {
        return status == Status.SUCCESS ? 0 : buffer.sizeRecords();
    }

    public RecordBuffer<R> getBuffer() {
        return buffer;
    }

    public int getOriginalRecordCount() {
        return originalRecordCount;
    }

    public Status getStatus() {
        return status;
    }

    public static enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
    }
}
