/*******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.eclipse.microprofile.reactive.streams.core;

import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Collection;

final class GraphImpl implements Graph {
    private final Collection<Stage> stages;

    /**
     * Create a graph from the given stages.
     * <p>
     * If the first stage has an inlet, then this graph has an inlet, and can therefore be represented as a
     * {@link org.reactivestreams.Subscriber}. If the last stage has an outlet, then this graph has an outlet, and
     * therefore can be represented as a {@link org.reactivestreams.Publisher}.
     *
     * @param stages The stages.
     */
    GraphImpl(Collection<Stage> stages) {
        this.stages = stages;
    }

    @Override
    public Collection<Stage> getStages() {
        return stages;
    }

    @Override
    public String toString() {
        return "Graph{" +
            "stages=" + stages +
            '}';
    }
}
