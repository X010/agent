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
public class NestedMetricsScope  implements IMetricsScope {
    private final IMetricsScope delegate;

    public NestedMetricsScope(IMetricsScope delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addData(String name, double value, StandardUnit unit) {
        delegate.addData(name, value, unit);
    }

    @Override
    public void addCount(String name, long amount) {
        delegate.addCount(name, amount);
    }

    @Override
    public void addTimeMillis(String name, long duration) {
        delegate.addTimeMillis(name, duration);
    }

    @Override
    public void addDimension(String name, String value) {
        throw new UnsupportedOperationException("Cannot add dimensions for nested metrics.");
    }

    @Override
    public void commit() {
        // TODO: Implement reference counting
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Cannot cancel nested metrics.");
    }

    @Override
    public boolean closed() {
        return delegate.closed();
    }

    @Override
    public Set<Dimension> getDimensions() {
        return delegate.getDimensions();
    }
}