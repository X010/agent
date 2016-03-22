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
public interface ISplitter {
    /**
     * Advances the buffer to the beginning of the next record, or to
     * the end of the buffer if a new record was not found.
     *
     * @param buffer
     * @return The position of the next record in the buffer, or {@code -1}
     * if the beginning of the record was not found before the end of
     * the buffer.
     */
    public int locateNextRecord(ByteBuffer buffer);
}
