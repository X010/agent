package com.dssmp.agent.metrics;

import com.dssmp.agent.AgentContext;
import com.dssmp.agent.tailing.FirehoseConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
public class Metrics implements IMetricsContext {
    // TODO: uncouple from FirehoseConstants
    public static final int BYTES_BEHIND_INFO_LEVEL = 5 * FirehoseConstants.DEFAULT_PARSER_BUFFER_SIZE_BYTES;
    public static final int BYTES_BEHIND_WARN_LEVEL = 10 * FirehoseConstants.DEFAULT_PARSER_BUFFER_SIZE_BYTES;

    public static final Pattern SENDER_TOTAL_RECORDS_SENT_METRIC = Pattern.compile("^.*Sender.TotalRecordsSent$");
    public static final Pattern PARSER_TOTAL_BYTES_CONSUMED_METRIC = Pattern.compile("^.*Parser.TotalBytesConsumed$");
    public static final Pattern FILE_TAILER_FILES_BEHIND_METRIC = Pattern.compile("^.*FileTailer.FilesBehind$");
    public static final Pattern FILE_TAILER_BYTES_BEHIND_METRIC = Pattern.compile("^.*FileTailer.BytesBehind$");
    public static final Pattern PARSER_TOTAL_RECORDS_PARSED_METRIC = Pattern.compile("^.*Parser.TotalRecordsParsed$");

    public static final String DESTINATION_DIMENSION = "Destination";

    private IMetricsFactory factory;

    public Metrics(AgentContext context) {
        List<IMetricsFactory> factories = new ArrayList<>();
        if (context.logEmitMetrics() && LogMetricsScope.LOGGER.isDebugEnabled()) {
            factories.add(new LogMetricsFactory());
        }
        if (context.cloudwatchEmitMetrics()) {
            final IMetricsFactory cwFactory = new CWMetricsFactory(
                    context.getCloudWatchClient(), context.cloudwatchNamespace(),
                    context.cloudwatchMetricsBufferTimeMillis(),
                    context.cloudwatchMetricsQueueSize());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    // TODO: CWMetricsFactory does not wait for shutdown to
                    //       complete. Need to fix that.
                    ((CWMetricsFactory) cwFactory).shutdown();
                }
            });
            factories.add(cwFactory);
        }

        if(factories.size() == 0) {
            factory = new NullMetricsFactory();
        } else if(factories.size() == 1) {
            factory = factories.get(0);
        } else {
            factory = new CompositeMetricsFactory(factories);
        }
    }

    @Override
    public IMetricsScope beginScope() {
        IMetricsScope scope = factory.createScope();
        return scope;
    }


    @SuppressWarnings("unchecked")
    public static <T> T getMetric(Map<String, Object> metrics, Pattern key, T fallback) {
        T val = null;
        for (String metricKey : metrics.keySet()) {
            if (key.matcher(metricKey).matches()) {
                val = (T) metrics.get(metricKey);
                break;
            }
        }
        if (val != null)
            return val;
        else
            return fallback;
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T getMetric(Map<String, Object> metrics, String key, T fallback) {
        T val = (T) metrics.get(key);
        if (val != null)
            return val;
        else
            return fallback;
    }
}