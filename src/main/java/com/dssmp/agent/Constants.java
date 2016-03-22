package com.dssmp.agent;

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
public class Constants {
    public static final char NEW_LINE = '\n';
    public static final long DEFAULT_RETRY_INITIAL_BACKOFF_MILLIS = 100;
    public static final long DEFAULT_RETRY_MAX_BACKOFF_MILLIS = 10_000;
    public static final int DEFAULT_PUBLISH_QUEUE_CAPACITY = 100;
    public static final long DEFAULT_MAX_BUFFER_AGE_MILLIS = TimeUnit.MINUTES.toMillis(1);
    public static final long DEFAULT_WAIT_ON_FULL_PUBLISH_QUEUE_MILLIS = TimeUnit.MINUTES.toMillis(1);
    public static final long DEFAULT_WAIT_ON_EMPTY_PUBLISH_QUEUE_MILLIS = TimeUnit.MINUTES.toMillis(1);
}
