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

package org.eclipse.microprofile.reactive.streams.tck.api;

import org.eclipse.microprofile.reactive.streams.CompletionSubscriber;
import org.eclipse.microprofile.reactive.streams.ReactiveStreamsFactory;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.UnsupportedStageException;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Verification for the {@link org.eclipse.microprofile.reactive.streams.SubscriberBuilder} class.
 */
public class SubscriberBuilderVerification extends AbstractReactiveStreamsApiVerification {

    public SubscriberBuilderVerification(ReactiveStreamsFactory rs) {
        super(rs);
    }

    @Test
    public void build() {
        AtomicReference<Graph> builtGraph = new AtomicReference<>();
        CompletionSubscriber expected = CompletionSubscriber.of(Mocks.SUBSCRIBER, new CompletableFuture());
        CompletionSubscriber returned = rs.builder().cancel().build(new ReactiveStreamsEngine() {
            @Override
            public <T> Publisher<T> buildPublisher(Graph graph) throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
            }

            @Override
            public <T, R> org.eclipse.microprofile.reactive.streams.spi.CompletionSubscriber<T, R>
            buildSubscriber(Graph graph) throws UnsupportedStageException {
                builtGraph.set(graph);
                return (org.eclipse.microprofile.reactive.streams.spi.CompletionSubscriber) expected;
            }

            @Override
            public <T, R> Processor<T, R> buildProcessor(Graph graph) throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
            }

            @Override
            public <T> CompletionStage<T> buildCompletion(Graph graph) throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
            }
        });

        assertSame(returned, expected);
        assertTrue(builtGraph.get().hasInlet());
        assertFalse(builtGraph.get().hasOutlet());
        assertEquals(builtGraph.get().getStages().size(), 1);
        assertTrue(builtGraph.get().getStages().iterator().next() instanceof Stage.Cancel);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void buildNull() {
        rs.builder().cancel().build(null);
    }
}
