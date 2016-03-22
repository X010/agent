package com.dssmp.agent.tailing;

import com.dssmp.agent.ByteBuffers;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
public abstract class AbstractRecord implements IRecord {
    protected final ByteBuffer data;
    protected final TrackedFile file;
    protected final long startOffset;

    public AbstractRecord(TrackedFile file, long offset, ByteBuffer data) {
        Preconditions.checkArgument(offset >= 0,
                "The offset of a record (%s) must be a non-negative integer (File: %s)",
                offset, file);
        this.data = data;
        this.file = file;
        this.startOffset = offset;
    }

    public AbstractRecord(TrackedFile file, long offset, byte[] data) {
        this(file, offset, ByteBuffer.wrap(data));
    }

    @Override
    public long dataLength() {
        return data.remaining();
    }

    @Override
    public long length() {
        return dataLength();
    }

    @Override
    public long endOffset() {
        return startOffset + dataLength();
    }

    @Override
    public long startOffset() {
        return startOffset;
    }

    @Override
    public ByteBuffer data() {
        return data;
    }

    @Override
    public TrackedFile file() {
        return file;
    }

    @Override
    public void truncate() {
        if (length() > file.getFlow().getMaxRecordSizeBytes()) {
            byte[] terminatorBytes = file.getFlow().getRecordTerminatorBytes();
            int originalPosition = data.position();
            data.limit(originalPosition + getMaxDataSize());
            // go to the position where we want to put the terminator
            data.position(originalPosition + getMaxDataSize() - terminatorBytes.length);
            // put the terminator
            // TODO:
            // We might have to handle the case where the last character of the truncated record contains
            // multiple bytes. In this case, the terminator itself might not be decoded as intended.
            data.put(terminatorBytes);
            data.position(originalPosition);
        }
    }

    /**
     * NOTE: Use for debugging only please.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String strData = ByteBuffers.toString(data, StandardCharsets.UTF_8).replace("\n", "\\n").replace(")", "\\)");
        if (strData.length() > 50)
            strData = strData.substring(0, 47) + "...";
        sb.append(getClass().getSimpleName())
                .append("(file=").append(file)
                .append(",startOffset=").append(startOffset)
                .append(",endOffset=").append(endOffset())
                .append(",length=").append(length())
                .append(",lengthIncludingOverhead=").append(lengthWithOverhead())
                .append(",data=(").append(strData).append(")")
                .append(")");
        return sb.toString();
    }

    /**
     * This size limit is used by {@link AbstractRecord#truncate()}
     * to truncate the data of the record to the limit
     *
     * @return max size of the data blob
     */
    protected abstract int getMaxDataSize();
}
