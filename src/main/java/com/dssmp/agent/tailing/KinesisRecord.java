package com.dssmp.agent.tailing;

import com.dssmp.agent.tailing.KinesisConstants.PartitionKeyOption;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

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
public class KinesisRecord extends AbstractRecord {
    protected final String partitionKey;

    public KinesisRecord(TrackedFile file, long offset, ByteBuffer data) {
        super(file, offset, data);
        Preconditions.checkNotNull(file);
        partitionKey = generatePartitionKey(((KinesisFileFlow) file.getFlow()).getPartitionKeyOption());
    }

    public KinesisRecord(TrackedFile file, long offset, byte[] data) {
        super(file, offset, data);
        Preconditions.checkNotNull(file);
        partitionKey = generatePartitionKey(((KinesisFileFlow) file.getFlow()).getPartitionKeyOption());
    }

    public String partitionKey() {
        return partitionKey;
    }

    @Override
    public long lengthWithOverhead() {
        return length() + KinesisConstants.PER_RECORD_OVERHEAD_BYTES;
    }

    @Override
    public long length() {
        return dataLength() + partitionKey.length();
    }

    @Override
    protected int getMaxDataSize() {
        return KinesisConstants.MAX_RECORD_SIZE_BYTES - partitionKey.length();
    }

    @VisibleForTesting
    String generatePartitionKey(PartitionKeyOption option) {
        Preconditions.checkNotNull(option);

        if (option == PartitionKeyOption.DETERMINISTIC) {
            Hasher hasher = Hashing.md5().newHasher();
            hasher.putBytes(data.array());
            return hasher.hash().toString();
        }
        if (option == PartitionKeyOption.RANDOM)
            return "" + ThreadLocalRandom.current().nextDouble(1000000);

        return null;
    }
}