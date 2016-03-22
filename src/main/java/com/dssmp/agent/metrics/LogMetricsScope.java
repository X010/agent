package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StatisticSet;
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
public class LogMetricsScope extends AccumulatingMetricsScope {
    public static final Logger LOGGER = Logging.getLogger(LogMetricsScope.class);

    public LogMetricsScope() {
        super();
    }

    @Override
    protected void realCommit() {
        if(!data.values().isEmpty()) {
            StringBuilder output = new StringBuilder();
            output.append("Metrics:\n");

            output.append("Dimensions: ");
            boolean needsComma = false;
            for (Dimension dimension : getDimensions()) {
                output.append(String.format("%s[%s: %s]", needsComma ? ", " : "", dimension.getName(), dimension.getValue()));
                needsComma = true;
            }
            output.append("\n");

            for (MetricDatum datum : data.values()) {
                StatisticSet statistics = datum.getStatisticValues();
                output.append(String.format("Name=%50s\tMin=%.2f\tMax=%.2f\tCount=%.2f\tSum=%.2f\tAvg=%.2f\tUnit=%s\n",
                        datum.getMetricName(),
                        statistics.getMinimum(),
                        statistics.getMaximum(),
                        statistics.getSampleCount(),
                        statistics.getSum(),
                        statistics.getSum() / statistics.getSampleCount(),
                        datum.getUnit()));
            }
            LOGGER.debug(output.toString());
        }
    }
}

