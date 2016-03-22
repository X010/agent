package com.dssmp.agent.tailing;

import com.dssmp.agent.Constants;

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
public class FirehoseConstants extends Constants {
    public static final String DESTINATION_KEY = "deliveryStream";

    public static final int PER_RECORD_OVERHEAD_BYTES = 0;
    public static final int MAX_RECORD_SIZE_BYTES = 40_960;
    public static final int PER_BUFFER_OVERHEAD_BYTES = 0;
    public static final int MAX_BATCH_PUT_SIZE_RECORDS = 500;
    public static final int MAX_BATCH_PUT_SIZE_BYTES = 4 * 1024 * 1024;
    public static final int MAX_BUFFER_SIZE_RECORDS = MAX_BATCH_PUT_SIZE_RECORDS;
    public static final int MAX_BUFFER_SIZE_BYTES = MAX_BATCH_PUT_SIZE_BYTES;
    public static final int DEFAULT_PARSER_BUFFER_SIZE_BYTES = MAX_BATCH_PUT_SIZE_BYTES;
}
