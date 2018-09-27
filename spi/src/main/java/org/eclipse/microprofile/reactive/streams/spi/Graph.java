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

package org.eclipse.microprofile.reactive.streams.spi;

import java.util.Collection;

/**
 * A graph.
 * <p>
 * Reactive Streams engines are required to convert the stages of this graph into a stream with interfaces according
 * to the shape. The shape is governed by whether the graph has an inlet, an outlet, neither or both.
 */
public interface Graph {
    /**
     * Get the stages of this graph.
     */
    Collection<Stage> getStages();

    /**
     * Returns true if this graph has an inlet, ie, if this graph can be turned into a
     * {@link org.reactivestreams.Subscriber}.
     */
    boolean hasInlet();

    /**
     * Returns true if this graph has an outlet, ie, if this graph can be turned into a
     * {@link org.reactivestreams.Publisher}.
     */
    boolean hasOutlet();
}
