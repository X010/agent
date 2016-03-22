package com.dssmp.agent.metrics;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

import java.util.ArrayList;
import java.util.List;

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
public class CWMetricsScope extends AccumulatingMetricsScope implements IMetricsScope {

    private CWPublisherRunnable<CWMetricKey> publisher;

    /**
     * Each CWMetricsScope takes a publisher which contains the logic of when to publish metrics.
     *
     * @param publisher publishing logic
     */

    public CWMetricsScope(CWPublisherRunnable<CWMetricKey> publisher) {
        super();
        this.publisher = publisher;
    }

    /*
     * Once we call this method, all MetricDatums added to the scope will be enqueued to the publisher runnable.
     * We enqueue MetricDatumWithKey because the publisher will aggregate similar metrics (i.e. MetricDatum with the
     * same metricName) in the background thread. Hence aggregation using MetricDatumWithKey will be especially useful
     * when aggregating across multiple MetricScopes.
     */
    @Override
    protected void realCommit() {
        if (!data.isEmpty()) {
            List<MetricDatumWithKey<CWMetricKey>> dataWithKeys = new ArrayList<MetricDatumWithKey<CWMetricKey>>();
            for (MetricDatum datum : data.values()) {
                datum.setDimensions(getDimensions());
                dataWithKeys.add(new MetricDatumWithKey<CWMetricKey>(new CWMetricKey(datum), datum));
            }
            publisher.enqueue(dataWithKeys);
        }
    }
}

