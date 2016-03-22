package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

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
public class MetricDatumWithKey<KeyType> {
    public KeyType key;
    public MetricDatum datum;

    /**
     * @param key an object that stores relevant information about a MetricDatum (e.g. MetricName, accountId,
     *        TimeStamp)
     * @param datum data point
     */

    public MetricDatumWithKey(KeyType key, MetricDatum datum) {
        this.key = key;
        this.datum = datum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, datum);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetricDatumWithKey<?> other = (MetricDatumWithKey<?>) obj;
        return Objects.equals(other.key, key) && Objects.equals(other.datum, datum);
    }

}

