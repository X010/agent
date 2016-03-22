package com.dssmp.agent.tailing;

import java.nio.ByteBuffer;

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
public class KafkaRecord extends AbstractRecord {


    public KafkaRecord(TrackedFile file, long offset, ByteBuffer data) {
        super(file, offset, data);
    }

    public KafkaRecord(TrackedFile file, long offset, byte[] data) {
        super(file, offset, data);
    }

    @Override
    protected int getMaxDataSize() {
        return 0;
    }

    @Override
    public long lengthWithOverhead() {
        return 0;
    }
}
