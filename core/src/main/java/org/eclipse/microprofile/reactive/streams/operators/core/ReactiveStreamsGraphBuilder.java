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

package org.eclipse.microprofile.reactive.streams.operators.core;

import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.eclipse.microprofile.reactive.streams.operators.spi.ToGraphable;
import org.reactivestreams.Publisher;

import java.util.*;

/**
 * Builds graphs of reactive streams.
 */
abstract class ReactiveStreamsGraphBuilder implements ToGraphable {

    private final Stage stage;
    private final ReactiveStreamsGraphBuilder previous;

    ReactiveStreamsGraphBuilder(Stage stage, ReactiveStreamsGraphBuilder previous) {
        this.stage = stage;
        this.previous = previous;
    }

    @Override
    public Graph toGraph() {
        ArrayDeque<Stage> deque = new ArrayDeque<>();
        flatten(deque);
        return new GraphImpl(Collections.unmodifiableCollection(deque));
    }

    private void flatten(Deque<Stage> stages) {
        ReactiveStreamsGraphBuilder thisStage = this;
        while (thisStage != null) {
            if (thisStage.stage == InternalStages.Identity.INSTANCE) {
                // Ignore, no need to add an identity stage
            }
            else if (thisStage.stage instanceof InternalStages.Nested) {
                ((InternalStages.Nested) thisStage.stage).getBuilder().flatten(stages);
            }
            else if (thisStage.stage instanceof InternalStages.NestedGraph) {
                // need to prepend to front in reverse order
                Collection<Stage> nestedStages = ((InternalStages.NestedGraph) thisStage.stage).getGraph().getStages();
                ListIterator<Stage> iter;
                if (nestedStages instanceof List) {
                    iter = ((List<Stage>) nestedStages).listIterator(nestedStages.size());
                }
                else {
                    iter = new ArrayList<>(nestedStages).listIterator(nestedStages.size());
                }
                while (iter.hasPrevious()) {
                    stages.addFirst(iter.previous());
                }
            }
            else {
                stages.addFirst(thisStage.stage);
            }
            thisStage = thisStage.previous;
        }
    }

    static Graph rsBuilderToGraph(Object obj) {
        Objects.requireNonNull(obj);
        if (obj instanceof ToGraphable) {
            return (((ToGraphable) obj).toGraph());
        }
        else {
            throw new IllegalArgumentException(obj + " is not an instance of ToGraphable and so can't participate " +
                "in this graph");
        }
    }

    static Graph publisherToGraph(Publisher<?> publisher) {
        return new GraphImpl(Collections.singleton(new Stages.PublisherStage(publisher)));
    }

}
