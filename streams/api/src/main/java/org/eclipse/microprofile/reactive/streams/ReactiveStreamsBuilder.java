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

package org.eclipse.microprofile.reactive.streams;

import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.*;

/**
 * Superclass of all reactive streams builders.
 *
 * @see ReactiveStreams
 */
public abstract class ReactiveStreamsBuilder {

  private final Stage stage;
  private final ReactiveStreamsBuilder previous;

  ReactiveStreamsBuilder(Stage stage, ReactiveStreamsBuilder previous) {
    this.stage = stage;
    this.previous = previous;
  }

  protected ReactiveStreamsEngine defaultEngine() {
    Iterator<ReactiveStreamsEngine> engines = ServiceLoader.load(ReactiveStreamsEngine.class).iterator();

    if (engines.hasNext()) {
      return engines.next();
    }
    else {
      throw new IllegalStateException("No implementation of ReactiveStreamsEngine service could be found.");
    }
  }

  Graph toGraph(boolean expectInlet, boolean expectOutlet) {
    ArrayDeque<Stage> deque = new ArrayDeque<>();
    flatten(deque);
    Graph graph = new Graph(Collections.unmodifiableCollection(deque));

    if (expectInlet) {
      if (!graph.hasInlet()) {
        throw new IllegalStateException("Expected to build a graph with an inlet, but no inlet was found: " + graph);
      }
    }
    else if (graph.hasInlet()) {
      throw new IllegalStateException("Expected to build a graph with no inlet, but an inlet was found: " + graph);
    }

    if (expectOutlet) {
      if (!graph.hasOutlet()) {
        throw new IllegalStateException("Expected to build a graph with an outlet, but no outlet was found: " + graph);
      }
    }
    else if (graph.hasOutlet()) {
      throw new IllegalStateException("Expected to build a graph with no outlet, but an outlet was found: " + graph);
    }

    return graph;
  }

  private void flatten(Deque<Stage> stages) {
    if (stage == InternalStages.Identity.INSTANCE) {
      // Ignore, no need to add an identity stage
    }
    else if (stage instanceof InternalStages.Nested) {
      ((InternalStages.Nested) stage).getBuilder().flatten(stages);
    }
    else {
      stages.addFirst(stage);
    }

    if (previous != null) {
      previous.flatten(stages);
    }
  }

}
