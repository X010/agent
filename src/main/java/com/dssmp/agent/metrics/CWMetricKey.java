package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;

import java.util.List;
import java.util.Objects;

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
public class CWMetricKey {
    private List<Dimension> dimensions;
    private String metricName;

    /**
     * @param datum data point
     */

    public CWMetricKey(MetricDatum datum) {
        this.dimensions = datum.getDimensions();
        this.metricName = datum.getMetricName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensions, metricName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CWMetricKey other = (CWMetricKey) obj;
        return Objects.equals(other.dimensions, dimensions) && Objects.equals(other.metricName, metricName);
    }
}
