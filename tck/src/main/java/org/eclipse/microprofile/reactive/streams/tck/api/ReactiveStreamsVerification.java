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

import org.eclipse.microprofile.reactive.streams.ReactiveStreamsFactory;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Verification for the {@link ReactiveStreams} class.
 */
public class ReactiveStreamsVerification extends AbstractReactiveStreamsApiVerification {

    public ReactiveStreamsVerification(ReactiveStreamsFactory rs) {
        super(rs);
    }

    @Test
    public void fromPublisher() {
        Graph graph = graphFor(rs.fromPublisher(Mocks.PUBLISHER));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertSame(getStage(Stage.PublisherStage.class, graph).getRsPublisher(), Mocks.PUBLISHER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromPublisherNull() {
        rs.fromPublisher(null);
    }

    @Test
    public void ofSingle() {
        Graph graph = graphFor(rs.of("foo"));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(getStage(Stage.Of.class, graph).getElements(), Collections.singletonList("foo"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void ofSingleNull() {
        rs.of((Object) null);
    }

    @Test
    public void ofVarArgs() {
        Graph graph = graphFor(rs.of("a", "b", "c"));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(getStage(Stage.Of.class, graph).getElements(), Arrays.asList("a", "b", "c"));
    }

    @Test
    public void empty() {
        Graph graph = graphFor(rs.empty());
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEmptyStage(getStage(Stage.Of.class, graph));
    }

    @Test
    public void ofNullableNull() {
        Graph graph = graphFor(rs.ofNullable(null));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEmptyStage(getStage(Stage.Of.class, graph));
    }

    @Test
    public void ofNullableNonNull() {
        Graph graph = graphFor(rs.ofNullable("foo"));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(getStage(Stage.Of.class, graph).getElements(), Collections.singletonList("foo"));
    }

    @Test
    public void fromIterable() {
        Graph graph = graphFor(rs.fromIterable(Arrays.asList("a", "b", "c")));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(getStage(Stage.Of.class, graph).getElements(), Arrays.asList("a", "b", "c"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromIterableNull() {
        rs.fromIterable(null);
    }

    @Test
    public void failed() {
        Graph graph = graphFor(rs.failed(new Exception("failed")));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(getStage(Stage.Failed.class, graph).getError().getMessage(), "failed");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void failedNull() {
        rs.failed(null);
    }

    @Test
    public void builder() {
        Graph graph = graphFor(rs.builder());
        assertTrue(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(graph.getStages(), Collections.emptyList(), "Identity builder should have an empty list of stages");
    }

    @Test
    public void fromProcessor() {
        Graph graph = graphFor(rs.fromProcessor(Mocks.PROCESSOR));
        assertTrue(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertSame(getStage(Stage.ProcessorStage.class, graph).getRsProcessor(), Mocks.PROCESSOR);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromProcessorNull() {
        rs.fromProcessor(null);
    }

    @Test
    public void fromSubscriber() {
        Graph graph = graphFor(rs.fromSubscriber(Mocks.SUBSCRIBER));
        assertTrue(graph.hasInlet());
        assertFalse(graph.hasOutlet());
        assertSame(getStage(Stage.SubscriberStage.class, graph).getRsSubscriber(), Mocks.SUBSCRIBER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromSubscriberNull() {
        rs.fromSubscriber(null);
    }

    @Test
    public void iterate() {
        Graph graph = graphFor(rs.iterate(1, i -> i + 1));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        Iterator iter = getStage(Stage.Of.class, graph).getElements().iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 1);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 2);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 3);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void iterateNullOperator() {
        rs.iterate(1, null);
    }

    @Test
    public void generate() {
        Graph graph = graphFor(rs.generate(() -> 1));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        Iterator iter = getStage(Stage.Of.class, graph).getElements().iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 1);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 1);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void generateNullSupplier() {
        rs.generate(null);
    }

    @Test
    public void concat() {
        Graph graph = graphFor(rs.concat(rs.empty(), rs.of(1)));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        Stage.Concat concat = getStage(Stage.Concat.class, graph);
        assertEmptyStage(getStage(Stage.Of.class, concat.getFirst()));
        assertEquals(getStage(Stage.Of.class, concat.getSecond()).getElements(), Collections.singletonList(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void concatFirstNull() {
        rs.concat(null, rs.empty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void concatSecondNull() {
        rs.concat(rs.empty(), null);
    }

    @Test
    public void fromCompletionStage() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);
        Graph graph = graphFor(rs.fromCompletionStage(future));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        Stage.FromCompletionStage fromCompletionStage = getStage(Stage.FromCompletionStage.class, graph);
        assertSame(fromCompletionStage.getCompletionStage(), future);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromCompletionStageNull() {
        rs.fromCompletionStage(null);
    }

    @Test
    public void fromCompletionStageNullable() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);
        Graph graph = graphFor(rs.fromCompletionStageNullable(future));
        assertFalse(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        Stage.FromCompletionStageNullable fromCompletionStage = getStage(Stage.FromCompletionStageNullable.class, graph);
        assertSame(fromCompletionStage.getCompletionStage(), future);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromCompletionStageNullableNull() {
        rs.fromCompletionStageNullable(null);
    }

    private <S extends Stage> S getStage(Class<S> clazz, Graph graph) {
        assertEquals(graph.getStages().size(), 1, "Graph does not have a single stage");
        Stage s = graph.getStages().iterator().next();
        assertTrue(clazz.isInstance(s), "Stage " + s + " is not a " + clazz);
        return clazz.cast(s);
    }
}
