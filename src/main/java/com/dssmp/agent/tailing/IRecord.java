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
public interface IRecord {
    public ByteBuffer data();
    public long dataLength();
    public long lengthWithOverhead();
    public long length();
    public TrackedFile file();
    public long endOffset();
    public long startOffset();

    /**
     * This method should make sure the truncated record has the appropriate
     * terminator (e.g. newline).
     */
    public void truncate();

    /**
     * @return A string representation of the data in this record, encoded
     *         with UTF-8; use for debugging only please.
     */
    @Override
    public String toString();
}
