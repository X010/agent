package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.StatisticSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public abstract class AccumulatingMetricsScope extends AbstractMetricsScope {

    protected final Set<Dimension> dimensions = new HashSet<Dimension>();
    protected final Map<String, MetricDatum> data = new HashMap<String, MetricDatum>();
    protected final long startTime = System.currentTimeMillis();

    @Override
    protected void realAddData(String name, double value, StandardUnit unit) {
        MetricDatum datum = data.get(name);
        if (datum == null) {
            data.put(name,
                    new MetricDatum().withMetricName(name)
                            .withUnit(unit)
                            .withStatisticValues(new StatisticSet().withMaximum(value)
                                    .withMinimum(value)
                                    .withSampleCount(1.0)
                                    .withSum(value)));
        } else {
            if (!datum.getUnit().equals(unit.name())) {
                throw new IllegalArgumentException("Cannot add to existing metric with different unit");
            }
            StatisticSet statistics = datum.getStatisticValues();
            statistics.setMaximum(Math.max(value, statistics.getMaximum()));
            statistics.setMinimum(Math.min(value, statistics.getMinimum()));
            statistics.setSampleCount(statistics.getSampleCount() + 1);
            statistics.setSum(statistics.getSum() + value);
        }
    }

    @Override
    protected void realAddDimension(String name, String value) {
        dimensions.add(new Dimension().withName(name).withValue(value));
    }

    @Override
    protected Set<Dimension> realGetDimensions() {
        return dimensions;
    }
}
