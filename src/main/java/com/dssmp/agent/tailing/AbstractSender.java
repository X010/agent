package com.dssmp.agent.tailing;

import com.dssmp.agent.Logging;
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
public abstract class AbstractSender<R extends IRecord> implements ISender<R> {
    protected final Logger logger;

    public AbstractSender() {
        this.logger = Logging.getLogger(getClass());
    }

    @Override
    public BufferSendResult<R> sendBuffer(RecordBuffer<R> buffer) {
        if (getMaxSendBatchSizeRecords() > 0 && buffer.sizeRecords() > getMaxSendBatchSizeRecords()) {
            throw new IllegalArgumentException("Buffer is too large for service call: " + buffer.sizeRecords() + " records vs. allowed maximum of " + getMaxSendBatchSizeRecords());
        }
        if (getMaxSendBatchSizeBytes() > 0 && buffer.sizeBytesWithOverhead() > getMaxSendBatchSizeBytes()) {
            throw new IllegalArgumentException("Buffer is too large for service call: " + buffer.sizeBytesWithOverhead() + " bytes vs. allowed maximum of " + getMaxSendBatchSizeBytes());
        }
        return attemptSend(buffer);
    }

    protected abstract long getMaxSendBatchSizeBytes();

    protected abstract int getMaxSendBatchSizeRecords();

    protected abstract BufferSendResult<R> attemptSend(RecordBuffer<R> buffer);
}
