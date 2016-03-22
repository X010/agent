package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

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
public interface IMetricsScope {

    /**
     * Adds a data point to this scope.
     *
     * @param name  data point name
     * @param value data point value
     * @param unit  unit of data point
     */
    public void addData(String name, double value, StandardUnit unit);

    /**
     * @param name   @see {@link #addData(String, double, StandardUnit)}
     * @param amount the amount to increment this counter.
     */
    public void addCount(String name, long amount);

    /**
     * @param name
     * @param duration duration of the tiumer in milliseconds
     */
    public void addTimeMillis(String name, long duration);

    /**
     * Adds a dimension that applies to all metrics in this IMetricsScope.
     *
     * @param name  dimension name
     * @param value dimension value
     */
    public void addDimension(String name, String value);

    /**
     * Flushes the data from this scope and makes it unusable.
     */
    public void commit();

    /**
     * Cancels this scope and discards any data.
     */
    public void cancel();

    /**
     * @return <code>true</code> if {@link #commit()} or {@link #cancel()} have
     * been called on this instance, otherwise <code>false</code>.
     */
    public boolean closed();

    /**
     * @return a set of dimensions for an IMetricsScope
     */
    public Set<Dimension> getDimensions();
}
