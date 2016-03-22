package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import java.util.*;

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
public class CompositeMetricsScope extends AbstractMetricsScope {
    private final List<IMetricsScope> scopes;

    /**
     * @param scopes
     */
    public CompositeMetricsScope(IMetricsScope... scopes) {
        this(Arrays.asList(scopes));
    }

    /**
     * @param scopes
     */
    public CompositeMetricsScope(Collection<IMetricsScope> scopes) {
        this.scopes = new ArrayList<>(scopes);
    }

    @Override
    protected void realAddData(String name, double value, StandardUnit unit) {
        for(IMetricsScope scope : this.scopes)
            scope.addData(name, value, unit);
    }

    @Override
    protected void realAddDimension(String name, String value) {
        for(IMetricsScope scope : this.scopes)
            scope.addDimension(name, value);
    }

    @Override
    protected void realCommit() {
        for(IMetricsScope scope : this.scopes)
            scope.commit();
    }

    @Override
    protected void realCancel() {
        for(IMetricsScope scope : this.scopes)
            scope.cancel();
    }

    @Override
    protected Set<Dimension> realGetDimensions() {
        return scopes.isEmpty() ? Collections.<Dimension> emptySet() : scopes.get(0).getDimensions();
    }
}
