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
import org.eclipse.microprofile.reactive.streams.GraphAccessor;
import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.UnsupportedStageException;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Verification for the {@link ProcessorBuilder} class.
 */
public class ProcessorBuilderVerification {

    @Test
    public void map() {
        Graph graph = GraphAccessor.buildGraphFor(builder().map(i -> i + 1));
        assertTrue(graph.hasOutlet());
        assertEquals(((Function) getAddedStage(Stage.Map.class, graph).getMapper()).apply(1), 2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void mapNullFunction() {
        builder().map(null);
    }

    @Test
    public void peek() {
        AtomicInteger peeked = new AtomicInteger();
        Graph graph = GraphAccessor.buildGraphFor(builder().peek(peeked::set));
        assertTrue(graph.hasOutlet());
        ((Consumer) getAddedStage(Stage.Peek.class, graph).getConsumer()).accept(1);
        assertEquals(peeked.get(), 1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void peekNullConsumer() {
        builder().peek(null);
    }

    @Test
    public void filter() {
        Graph graph = GraphAccessor.buildGraphFor(builder().filter(i -> i < 3));
        assertTrue(graph.hasOutlet());
        assertTrue(((Predicate) getAddedStage(Stage.Filter.class, graph).getPredicate()).test(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void filterNullPredicate() {
        builder().filter(null);
    }

    @Test
    public void distinct() {
        Graph graph = GraphAccessor.buildGraphFor(builder().distinct());
        assertTrue(graph.hasOutlet());
        assertSame(getAddedStage(Stage.Distinct.class, graph), Stage.Distinct.INSTANCE);
    }

    @Test
    public void flatMap() {
        Graph graph = GraphAccessor.buildGraphFor(builder().flatMap(i -> ReactiveStreams.empty()));
        assertTrue(graph.hasOutlet());
        Function flatMap = getAddedStage(Stage.FlatMap.class, graph).getMapper();
        Object result = flatMap.apply(1);
        assertTrue(result instanceof Graph);
        Graph innerGraph = (Graph) result;
        assertFalse(innerGraph.hasInlet());
        assertTrue(innerGraph.hasOutlet());
        assertEquals(innerGraph.getStages(), Collections.singletonList(Stage.Of.EMPTY));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapNullMapper() {
        builder().flatMap(null);
    }

    @Test
    public void flatMapRsPublisher() {
        Graph graph = GraphAccessor.buildGraphFor(builder().flatMapRsPublisher(i -> Mocks.PUBLISHER));
        assertTrue(graph.hasOutlet());
        Function flatMap = getAddedStage(Stage.FlatMap.class, graph).getMapper();
        Object result = flatMap.apply(1);
        assertTrue(result instanceof Graph);
        Graph innerGraph = (Graph) result;
        assertFalse(innerGraph.hasInlet());
        assertTrue(innerGraph.hasOutlet());
        assertEquals(innerGraph.getStages().size(), 1);
        Stage inner = innerGraph.getStages().iterator().next();
        assertTrue(inner instanceof Stage.PublisherStage);
        assertEquals(((Stage.PublisherStage) inner).getRsPublisher(), Mocks.PUBLISHER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapRsPublisherNullMapper() {
        builder().flatMapRsPublisher(null);
    }

    @Test
    public void flatMapCompletionStage() throws Exception {
        Graph graph = GraphAccessor.buildGraphFor(builder().flatMapCompletionStage(i -> CompletableFuture.completedFuture(i + 1)));
        assertTrue(graph.hasOutlet());
        CompletionStage result = (CompletionStage) ((Function) getAddedStage(Stage.FlatMapCompletionStage.class, graph).getMapper()).apply(1);
        assertEquals(result.toCompletableFuture().get(1, TimeUnit.SECONDS), 2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapCompletionStageNullMapper() {
        builder().flatMapCompletionStage(null);
    }

    @Test
    public void flatMapIterable() {
        Graph graph = GraphAccessor.buildGraphFor(builder().flatMapIterable(i -> Arrays.asList(i, i + 1)));
        assertTrue(graph.hasOutlet());
        assertEquals(((Function) getAddedStage(Stage.FlatMapIterable.class, graph).getMapper()).apply(1), Arrays.asList(1, 2));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapIterableNullMapper() {
        builder().flatMapIterable(null);
    }

    @Test
    public void limit() {
        Graph graph = GraphAccessor.buildGraphFor(builder().limit(3));
        assertTrue(graph.hasOutlet());
        assertEquals(getAddedStage(Stage.Limit.class, graph).getLimit(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void limitNegative() {
        builder().limit(-1);
    }

    @Test
    public void skip() {
        Graph graph = GraphAccessor.buildGraphFor(builder().skip(3));
        assertTrue(graph.hasOutlet());
        assertEquals(getAddedStage(Stage.Skip.class, graph).getSkip(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void skipNegative() {
        builder().skip(-1);
    }

    @Test
    public void takeWhile() {
        Graph graph = GraphAccessor.buildGraphFor(builder().takeWhile(i -> i < 3));
        assertTrue(graph.hasOutlet());
        assertTrue(((Predicate) getAddedStage(Stage.TakeWhile.class, graph).getPredicate()).test(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void takeWhileNullPredicate() {
        builder().takeWhile(null);
    }

    @Test
    public void dropWhile() {
        Graph graph = GraphAccessor.buildGraphFor(builder().dropWhile(i -> i < 3));
        assertTrue(graph.hasOutlet());
        assertTrue(((Predicate) getAddedStage(Stage.DropWhile.class, graph).getPredicate()).test(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void dropWhileNullPredicate() {
        builder().dropWhile(null);
    }

    @Test
    public void forEach() {
        AtomicInteger received = new AtomicInteger();
        Graph graph = GraphAccessor.buildGraphFor(builder().forEach(received::set));
        assertFalse(graph.hasOutlet());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container = collector.supplier().get();
        collector.accumulator().accept(container, 1);
        assertEquals(received.get(), 1);
        assertNull(collector.finisher().apply(container));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void forEachNullConsumer() {
        builder().forEach(null);
    }

    @Test
    public void ignore() {
        Graph graph = GraphAccessor.buildGraphFor(builder().ignore());
        assertFalse(graph.hasOutlet());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container = collector.supplier().get();
        collector.accumulator().accept(container, 1);
        assertNull(collector.finisher().apply(container));
    }

    @Test
    public void cancel() {
        Graph graph = GraphAccessor.buildGraphFor(builder().cancel());
        assertFalse(graph.hasOutlet());
        assertSame(getAddedStage(Stage.Cancel.class, graph), Stage.Cancel.INSTANCE);
    }

    @Test
    public void reduceWithIdentity() {
        Graph graph = GraphAccessor.buildGraphFor(builder().reduce(1, (a, b) -> a - b));
        assertFalse(graph.hasOutlet());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container1 = collector.supplier().get();
        assertEquals(collector.finisher().apply(container1), 1);
        // Create a new container because we don't necessarily want to require that the container be reusable after
        // the finishers has been applied to it.
        Object container2 = collector.supplier().get();
        collector.accumulator().accept(container2, 3);
        assertEquals(collector.finisher().apply(container2), -2);
    }

    @Test
    public void reduceWithIdentityNullIdentityAllowed() {
        builder().reduce(null, (a, b) -> a);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void reduceWithIdentityNullAccumulator() {
        builder().reduce(1, null);
    }

    @Test
    public void reduce() {
        Graph graph = GraphAccessor.buildGraphFor(builder().reduce((a, b) -> a - b));
        assertFalse(graph.hasOutlet());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container1 = collector.supplier().get();
        assertEquals(collector.finisher().apply(container1), Optional.empty());
        Object container2 = collector.supplier().get();
        collector.accumulator().accept(container2, 2);
        assertEquals(collector.finisher().apply(container2), Optional.of(2));
        Object container3 = collector.supplier().get();
        collector.accumulator().accept(container3, 5);
        collector.accumulator().accept(container3, 2);
        assertEquals(collector.finisher().apply(container3), Optional.of(3));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void reduceNullAccumulator() {
        builder().reduce(null);
    }

    @Test
    public void findFirst() {
        Graph graph = GraphAccessor.buildGraphFor(builder().findFirst());
        assertFalse(graph.hasOutlet());
        assertSame(getAddedStage(Stage.FindFirst.class, graph), Stage.FindFirst.INSTANCE);
    }

    @Test
    public void collect() {
        Collector collector = Collectors.toList();
        Graph graph = GraphAccessor.buildGraphFor(builder().collect(collector));
        assertFalse(graph.hasOutlet());
        assertSame(getAddedStage(Stage.Collect.class, graph).getCollector(), collector);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void collectNull() {
        builder().collect(null);
    }

    @Test
    public void collectComponents() {
        Supplier supplier = () -> null;
        BiConsumer accumulator = (a, b) -> {};
        Graph graph = GraphAccessor.buildGraphFor(builder().collect(supplier, accumulator));
        assertFalse(graph.hasOutlet());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        assertSame(collector.supplier(), supplier);
        assertSame(collector.accumulator(), accumulator);
        // Finisher must be identity function
        Object myObject = new Object();
        assertSame(collector.finisher().apply(myObject), myObject);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void collectComponentsSupplierNull() {
        builder().collect(null, (a, b) -> {});
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void collectComponentsAccumulatorNull() {
        builder().collect(() -> null, null);
    }

    @Test
    public void toList() {
        Graph graph = GraphAccessor.buildGraphFor(builder().toList());
        assertFalse(graph.hasOutlet());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container = collector.supplier().get();
        collector.accumulator().accept(container, 1);
        collector.accumulator().accept(container, 2);
        collector.accumulator().accept(container, 3);
        assertEquals(collector.finisher().apply(container), Arrays.asList(1, 2, 3));
    }

    @Test
    public void toSubscriber() {
        Graph graph = GraphAccessor.buildGraphFor(builder().to(Mocks.SUBSCRIBER));
        assertFalse(graph.hasOutlet());
        assertSame(getAddedStage(Stage.SubscriberStage.class, graph).getRsSubscriber(), Mocks.SUBSCRIBER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void toSubscriberNull() {
        builder().to((Subscriber) null);
    }

    @Test
    public void to() {
        Graph graph = GraphAccessor.buildGraphFor(builder().to(ReactiveStreams.fromSubscriber(Mocks.SUBSCRIBER)));
        assertFalse(graph.hasOutlet());
        assertSame(getAddedStage(Stage.SubscriberStage.class, graph).getRsSubscriber(), Mocks.SUBSCRIBER);
    }

    @Test
    public void toMultipleStages() {
        Graph graph = GraphAccessor.buildGraphFor(builder().to(
            ReactiveStreams.<Integer>builder().map(Function.identity()).cancel()));
        assertTrue(graph.hasInlet());
        assertFalse(graph.hasOutlet());
        assertEquals(graph.getStages().size(), 3);
        Iterator<Stage> stages = graph.getStages().iterator();
        assertTrue(stages.next() instanceof Stage.Map);
        assertTrue(stages.next() instanceof Stage.Map);
        assertSame(stages.next(), Stage.Cancel.INSTANCE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void toNull() {
        builder().to((SubscriberBuilder) null);
    }

    @Test
    public void viaProcessor() {
        Graph graph = GraphAccessor.buildGraphFor(builder().via(Mocks.PROCESSOR));
        assertTrue(graph.hasOutlet());
        assertSame(getAddedStage(Stage.ProcessorStage.class, graph).getRsProcessor(), Mocks.PROCESSOR);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void viaProcessorNull() {
        builder().via((Processor) null);
    }

    @Test
    public void via() {
        Graph graph = GraphAccessor.buildGraphFor(builder().via(ReactiveStreams.fromProcessor(Mocks.PROCESSOR)));
        assertTrue(graph.hasOutlet());
        assertSame(getAddedStage(Stage.ProcessorStage.class, graph).getRsProcessor(), Mocks.PROCESSOR);
    }

    @Test
    public void viaEmpty() {
        Graph graph = GraphAccessor.buildGraphFor(builder().via(ReactiveStreams.builder()));
        assertTrue(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(graph.getStages().size(), 1);
        assertTrue(graph.getStages().iterator().next() instanceof Stage.Map);
    }

    @Test
    public void viaMultipleStages() {
        Graph graph = GraphAccessor.buildGraphFor(builder().via(
            ReactiveStreams.<Integer>builder().map(Function.identity()).filter(t -> true)));
        assertTrue(graph.hasInlet());
        assertTrue(graph.hasOutlet());
        assertEquals(graph.getStages().size(), 3);
        Iterator<Stage> stages = graph.getStages().iterator();
        assertTrue(stages.next() instanceof Stage.Map);
        assertTrue(stages.next() instanceof Stage.Map);
        assertTrue(stages.next() instanceof Stage.Filter);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void viaNull() {
        builder().via((ProcessorBuilder) null);
    }

    @Test
    public void onError() {
        Consumer consumer = t -> {};
        Graph graph = GraphAccessor.buildGraphFor(builder().onError(consumer));
        assertTrue(graph.hasOutlet());
        assertSame(getAddedStage(Stage.OnError.class, graph).getConsumer(), consumer);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorNullConsumer() {
        builder().onError(null);
    }

    @Test
    public void onErrorResume() {
        Graph graph = GraphAccessor.buildGraphFor(builder().onErrorResume(t -> 2));
        assertTrue(graph.hasOutlet());
        assertEquals(getAddedStage(Stage.OnErrorResume.class, graph).getFunction().apply(new RuntimeException()), 2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorResumeNull() {
        builder().onErrorResume(null);
    }

    @Test
    public void onErrorResumeWith() {
        Graph graph = GraphAccessor.buildGraphFor(builder().onErrorResumeWith(t -> ReactiveStreams.empty()));
        assertTrue(graph.hasOutlet());
        Graph resumeWith = getAddedStage(Stage.OnErrorResumeWith.class, graph).getFunction().apply(new RuntimeException());
        assertEquals(resumeWith.getStages(), Arrays.asList(Stage.Of.EMPTY));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorResumeWithNull() {
        builder().onErrorResumeWith(null);
    }

    @Test
    public void onErrorResumeWithRsPublisher() {
        Graph graph = GraphAccessor.buildGraphFor(builder().onErrorResumeWithRsPublisher(t -> Mocks.PUBLISHER));
        assertTrue(graph.hasOutlet());
        Graph resumeWith = getAddedStage(Stage.OnErrorResumeWith.class, graph).getFunction().apply(new RuntimeException());
        assertEquals(resumeWith.getStages().size(), 1);
        assertSame(((Stage.PublisherStage) resumeWith.getStages().iterator().next()).getRsPublisher(), Mocks.PUBLISHER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorResumeWithRsPublisherNull() {
        builder().onErrorResumeWithRsPublisher(null);
    }

    @Test
    public void onTerminate() {
        Runnable action = () -> {};
        Graph graph = GraphAccessor.buildGraphFor(builder().onTerminate(action));
        assertTrue(graph.hasOutlet());
        assertSame(getAddedStage(Stage.OnTerminate.class, graph).getAction(), action);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onTerminateNull() {
        builder().onTerminate(null);
    }

    @Test
    public void onComplete() {
        Runnable action = () -> {};
        Graph graph = GraphAccessor.buildGraphFor(builder().onComplete(action));
        assertTrue(graph.hasOutlet());
        assertSame(getAddedStage(Stage.OnComplete.class, graph).getAction(), action);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onCompleteNull() {
        builder().onComplete(null);
    }

    @Test
    public void buildRs() {
        AtomicReference<Graph> builtGraph = new AtomicReference<>();
        Processor processor = builder().distinct().buildRs(new ReactiveStreamsEngine() {
            @Override
            public <T> Publisher<T> buildPublisher(Graph graph) throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
            }
            @Override
            public <T, R> CompletionSubscriber<T, R> buildSubscriber(Graph graph) throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
            }

            @Override
            public <T, R> Processor<T, R> buildProcessor(Graph graph) throws UnsupportedStageException {
                builtGraph.set(graph);
                return Mocks.PROCESSOR;
            }

            @Override
            public <T> CompletionStage<T> buildCompletion(Graph graph) throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
            }
        });

        assertSame(processor, Mocks.PROCESSOR);
        assertSame(getAddedStage(Stage.Distinct.class, builtGraph.get()), Stage.Distinct.INSTANCE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void buildRsNull() {
        builder().buildRs(null);
    }

    @Test
    public void builderShouldBeImmutable() {
        ProcessorBuilder<Integer, Integer> builder = builder();
        ProcessorBuilder<Integer, Integer> mapped = builder.map(Function.identity());
        ProcessorBuilder<Integer, Integer> distinct = builder.distinct();
        SubscriberBuilder<Integer, Void> cancelled = builder.cancel();
        getAddedStage(Stage.Map.class, GraphAccessor.buildGraphFor(mapped));
        getAddedStage(Stage.Distinct.class, GraphAccessor.buildGraphFor(distinct));
        getAddedStage(Stage.Cancel.class, GraphAccessor.buildGraphFor(cancelled));
    }

    private ProcessorBuilder<Integer, Integer> builder() {
        return ReactiveStreams.<Integer>builder().map(Function.identity());
    }

    private <S extends Stage> S getAddedStage(Class<S> clazz, Graph graph) {
        assertTrue(graph.hasInlet(), "Graph doesn't have inlet but should because it's meant to be a processor: " + graph);
        assertEquals(graph.getStages().size(), 2, "Graph does not have two stages");
        Iterator<Stage> stages = graph.getStages().iterator();
        Stage first = stages.next();
        assertTrue(first instanceof Stage.Map, "First stage " + first + " is not a " + Stage.Map.class);
        Stage second = stages.next();
        assertTrue(clazz.isInstance(second), "Second stage " + second + " is not a " + clazz);
        return clazz.cast(second);
    }

}
