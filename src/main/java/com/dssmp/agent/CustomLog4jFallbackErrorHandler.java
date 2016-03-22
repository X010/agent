package com.dssmp.agent;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.FallbackErrorHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

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
public class CustomLog4jFallbackErrorHandler extends FallbackErrorHandler {
    private static StringBuilder errorHeader = new StringBuilder();

    /**
     * @return A string describing the error that triggered the fallback, or
     *         {@code null} if no errors occurred.
     */
    public static String getErrorHeader() {
        return errorHeader.length() == 0 ? null : errorHeader.toString();
    }

    public static String getFallbackLogFile() {
        return "/tmp/fallback-aws-kinesis-agent.log";
    }

    @Override
    public void error(String message, Exception e, int errorCode, LoggingEvent event) {
        errorHeader.append("# ********************************************************************************").append('\n');
        errorHeader.append("# ").append(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date())).append('\n');
        errorHeader.append("# The following error was reported while initializing logging:").append('\n');
        errorHeader.append("#    " + message).append('\n');
        errorHeader.append("#    " + e.getClass().getName() + ": " + e.getMessage()).append('\n');
        errorHeader.append("# Please fix the problem and restart the application.").append('\n');
        errorHeader.append("# Logs will be redirected to " + getFallbackLogFile() + " in the meantime.").append('\n');
        errorHeader.append("# ********************************************************************************").append('\n');
        System.err.println(errorHeader.toString());
        super.error(message, e, errorCode, event);
    }
}
