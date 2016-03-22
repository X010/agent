package com.dssmp.agent.metrics;

import com.beust.jcommander.internal.Nullable;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
public class CompositeMetricsFactory implements IMetricsFactory {

    private final Collection<IMetricsFactory> factories;

    /**
     * @param factories
     */
    public CompositeMetricsFactory(IMetricsFactory... factories) {
        this(Arrays.asList(factories));
    }

    /**
     * @param factories
     */
    public CompositeMetricsFactory(Collection<IMetricsFactory> factories) {
        this.factories = new ArrayList<>(factories);
    }

    /**
     * @return a {@link CompositeMetricsScope} containing a scope for each
     *         of the factories backing this composite.
     */
    @Override
    public IMetricsScope createScope() {
        Collection<IMetricsScope> scopes = Collections2.transform(
                this.factories, new Function<IMetricsFactory, IMetricsScope>() {
                    @Override
                    public IMetricsScope apply(IMetricsFactory input) {
                        return input.createScope();
                    }
                });
        return new CompositeMetricsScope(scopes);

    }

}
