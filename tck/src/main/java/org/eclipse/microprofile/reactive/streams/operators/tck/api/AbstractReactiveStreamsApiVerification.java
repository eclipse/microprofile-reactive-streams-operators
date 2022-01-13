/*******************************************************************************
 * Copyright (c) 2018, 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.reactive.streams.operators.tck.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.eclipse.microprofile.reactive.streams.operators.CompletionRunner;
import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreamsFactory;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.eclipse.microprofile.reactive.streams.operators.spi.ToGraphable;

public abstract class AbstractReactiveStreamsApiVerification {

    protected final ReactiveStreamsFactory rs;

    public AbstractReactiveStreamsApiVerification(ReactiveStreamsFactory rs) {
        this.rs = rs;
    }

    protected Graph graphFor(PublisherBuilder<?> pb) {
        return objGraphFor(pb);
    }

    protected Graph graphFor(SubscriberBuilder<?, ?> sb) {
        return objGraphFor(sb);
    }

    protected Graph graphFor(ProcessorBuilder<?, ?> pb) {
        return objGraphFor(pb);
    }

    protected Graph graphFor(CompletionRunner<?> cr) {
        return objGraphFor(cr);
    }

    private Graph objGraphFor(Object o) {
        return ((ToGraphable) o).toGraph();
    }

    protected void assertEmptyStage(Stage stage) {
        assertTrue(stage instanceof Stage.Of);
        assertEquals(((Stage.Of) stage).getElements(), Collections.emptyList());
    }
}
