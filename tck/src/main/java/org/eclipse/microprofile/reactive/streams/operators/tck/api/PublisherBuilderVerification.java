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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
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

import org.eclipse.microprofile.reactive.streams.operators.CompletionRunner;
import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreamsFactory;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.eclipse.microprofile.reactive.streams.operators.spi.SubscriberWithCompletionStage;
import org.eclipse.microprofile.reactive.streams.operators.spi.UnsupportedStageException;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.testng.annotations.Test;

/**
 * Verification for the {@link org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder} class.
 */
public class PublisherBuilderVerification extends AbstractReactiveStreamsApiVerification {

    public PublisherBuilderVerification(ReactiveStreamsFactory rs) {
        super(rs);
    }

    @Test
    public void map() {
        Graph graph = graphFor(builder().map(i -> i + 1));
        assertEquals(((Function) getAddedStage(Stage.Map.class, graph).getMapper()).apply(1), 2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void mapNullFunction() {
        builder().map(null);
    }

    @Test
    public void peek() {
        AtomicInteger peeked = new AtomicInteger();
        Graph graph = graphFor(builder().peek(peeked::set));
        ((Consumer) getAddedStage(Stage.Peek.class, graph).getConsumer()).accept(1);
        assertEquals(peeked.get(), 1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void peekNullConsumer() {
        builder().peek(null);
    }

    @Test
    public void filter() {
        Graph graph = graphFor(builder().filter(i -> i < 3));
        assertTrue(((Predicate) getAddedStage(Stage.Filter.class, graph).getPredicate()).test(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void filterNullPredicate() {
        builder().filter(null);
    }

    @Test
    public void distinct() {
        Graph graph = graphFor(builder().distinct());
        getAddedStage(Stage.Distinct.class, graph);
    }

    @Test
    public void flatMap() {
        Graph graph = graphFor(builder().flatMap(i -> rs.empty()));
        Function flatMap = getAddedStage(Stage.FlatMap.class, graph).getMapper();
        Object result = flatMap.apply(1);
        assertTrue(result instanceof Graph);
        Graph innerGraph = (Graph) result;
        assertEquals(innerGraph.getStages().size(), 1);
        assertEmptyStage(innerGraph.getStages().iterator().next());
    }

    @Test
    public void flatMapToBuilderFromDifferentReactiveStreamsImplementation() {
        Graph graph = graphFor(builder().flatMap(i -> Mocks.EMPTY_PUBLISHER_BUILDER));
        Function flatMap = getAddedStage(Stage.FlatMap.class, graph).getMapper();
        Object result = flatMap.apply(1);
        assertTrue(result instanceof Graph);
        assertSame(result, Mocks.EMPTY_PUBLISHER_GRAPH);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapNullMapper() {
        builder().flatMap(null);
    }

    @Test
    public void flatMapRsPublisher() {
        Graph graph = graphFor(builder().flatMapRsPublisher(i -> Mocks.PUBLISHER));
        Function flatMap = getAddedStage(Stage.FlatMap.class, graph).getMapper();
        Object result = flatMap.apply(1);
        assertTrue(result instanceof Graph);
        Graph innerGraph = (Graph) result;
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
        Graph graph = graphFor(builder().flatMapCompletionStage(i -> CompletableFuture.completedFuture(i + 1)));
        CompletionStage result =
                (CompletionStage) ((Function) getAddedStage(Stage.FlatMapCompletionStage.class, graph).getMapper())
                        .apply(1);
        assertEquals(result.toCompletableFuture().get(1, TimeUnit.SECONDS), 2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapCompletionStageNullMapper() {
        builder().flatMapCompletionStage(null);
    }

    @Test
    public void flatMapIterable() {
        Graph graph = graphFor(builder().flatMapIterable(i -> Arrays.asList(i, i + 1)));
        assertEquals(((Function) getAddedStage(Stage.FlatMapIterable.class, graph).getMapper()).apply(1),
                Arrays.asList(1, 2));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapIterableNullMapper() {
        builder().flatMapIterable(null);
    }

    @Test
    public void limit() {
        Graph graph = graphFor(builder().limit(3));
        assertEquals(getAddedStage(Stage.Limit.class, graph).getLimit(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void limitNegative() {
        builder().limit(-1);
    }

    @Test
    public void skip() {
        Graph graph = graphFor(builder().skip(3));
        assertEquals(getAddedStage(Stage.Skip.class, graph).getSkip(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void skipNegative() {
        builder().skip(-1);
    }

    @Test
    public void takeWhile() {
        Graph graph = graphFor(builder().takeWhile(i -> i < 3));
        assertTrue(((Predicate) getAddedStage(Stage.TakeWhile.class, graph).getPredicate()).test(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void takeWhileNullPredicate() {
        builder().takeWhile(null);
    }

    @Test
    public void dropWhile() {
        Graph graph = graphFor(builder().dropWhile(i -> i < 3));
        assertTrue(((Predicate) getAddedStage(Stage.DropWhile.class, graph).getPredicate()).test(1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void dropWhileNullPredicate() {
        builder().dropWhile(null);
    }

    @Test
    public void forEach() {
        AtomicInteger received = new AtomicInteger();
        Graph graph = graphFor(builder().forEach(received::set));
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
        Graph graph = graphFor(builder().ignore());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container = collector.supplier().get();
        collector.accumulator().accept(container, 1);
        assertNull(collector.finisher().apply(container));
    }

    @Test
    public void cancel() {
        Graph graph = graphFor(builder().cancel());
        getAddedStage(Stage.Cancel.class, graph);
    }

    @Test
    public void reduceWithIdentity() {
        Graph graph = graphFor(builder().reduce(1, (a, b) -> a - b));
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
        Graph graph = graphFor(builder().reduce((a, b) -> a - b));
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
        Graph graph = graphFor(builder().findFirst());
        getAddedStage(Stage.FindFirst.class, graph);
    }

    @Test
    public void collect() {
        Collector collector = Collectors.toList();
        Graph graph = graphFor(builder().collect(collector));
        assertSame(getAddedStage(Stage.Collect.class, graph).getCollector(), collector);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void collectNull() {
        builder().collect(null);
    }

    @Test
    public void collectComponents() {
        Supplier supplier = () -> null;
        BiConsumer accumulator = (a, b) -> {
        };
        Graph graph = graphFor(builder().collect(supplier, accumulator));
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        assertSame(collector.supplier(), supplier);
        assertSame(collector.accumulator(), accumulator);
        // Finisher must be identity function
        Object myObject = new Object();
        assertSame(collector.finisher().apply(myObject), myObject);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void collectComponentsSupplierNull() {
        builder().collect(null, (a, b) -> {
        });
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void collectComponentsAccumulatorNull() {
        builder().collect(() -> null, null);
    }

    @Test
    public void toList() {
        Graph graph = graphFor(builder().toList());
        Collector collector = getAddedStage(Stage.Collect.class, graph).getCollector();
        Object container = collector.supplier().get();
        collector.accumulator().accept(container, 1);
        collector.accumulator().accept(container, 2);
        collector.accumulator().accept(container, 3);
        assertEquals(collector.finisher().apply(container), Arrays.asList(1, 2, 3));
    }

    @Test
    public void toSubscriber() {
        Graph graph = graphFor(builder().to(Mocks.SUBSCRIBER));
        assertSame(getAddedStage(Stage.SubscriberStage.class, graph).getRsSubscriber(), Mocks.SUBSCRIBER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void toSubscriberNull() {
        builder().to((Subscriber) null);
    }

    @Test
    public void to() {
        Graph graph = graphFor(builder().to(rs.fromSubscriber(Mocks.SUBSCRIBER)));
        assertSame(getAddedStage(Stage.SubscriberStage.class, graph).getRsSubscriber(), Mocks.SUBSCRIBER);
    }

    @Test
    public void toBuilderFromDifferentReactiveStreamsImplementation() {
        Graph graph = graphFor(builder().to(Mocks.SUBSCRIBER_BUILDER));
        assertEquals(graph.getStages().size(), 3);
        Iterator<Stage> stages = graph.getStages().iterator();
        assertTrue(stages.next() instanceof Stage.Of);
        assertTrue(stages.next() instanceof Stage.Distinct);
        assertTrue(stages.next() instanceof Stage.Cancel);
    }

    @Test
    public void toMultipleStages() {
        Graph graph = graphFor(builder().to(
                rs.<Integer>builder().map(Function.identity()).cancel()));
        assertEquals(graph.getStages().size(), 3);
        Iterator<Stage> stages = graph.getStages().iterator();
        assertTrue(stages.next() instanceof Stage.Of);
        assertTrue(stages.next() instanceof Stage.Map);
        assertTrue(stages.next() instanceof Stage.Cancel);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void toNull() {
        builder().to((SubscriberBuilder) null);
    }

    @Test
    public void viaProcessor() {
        Graph graph = graphFor(builder().via(Mocks.PROCESSOR));
        assertSame(getAddedStage(Stage.ProcessorStage.class, graph).getRsProcessor(), Mocks.PROCESSOR);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void viaProcessorNull() {
        builder().via((Processor) null);
    }

    @Test
    public void via() {
        Graph graph = graphFor(builder().via(rs.fromProcessor(Mocks.PROCESSOR)));
        assertSame(getAddedStage(Stage.ProcessorStage.class, graph).getRsProcessor(), Mocks.PROCESSOR);
    }

    @Test
    public void viaBuilderFromDifferentReactiveStreamsImplementation() {
        Graph graph = graphFor(builder().via(Mocks.PROCESSOR_BUILDER));
        assertEquals(graph.getStages().size(), 3);
        Iterator<Stage> stages = graph.getStages().iterator();
        assertTrue(stages.next() instanceof Stage.Of);
        assertTrue(stages.next() instanceof Stage.Distinct);
        assertTrue(stages.next() instanceof Stage.Limit);
    }

    @Test
    public void viaEmpty() {
        Graph graph = graphFor(builder().via(rs.builder()));
        assertEquals(graph.getStages().size(), 1);
        assertTrue(graph.getStages().iterator().next() instanceof Stage.Of);
    }

    @Test
    public void viaMultipleStages() {
        Graph graph = graphFor(builder().via(
                rs.<Integer>builder().map(Function.identity()).filter(t -> true)));
        assertEquals(graph.getStages().size(), 3);
        Iterator<Stage> stages = graph.getStages().iterator();
        assertTrue(stages.next() instanceof Stage.Of);
        assertTrue(stages.next() instanceof Stage.Map);
        assertTrue(stages.next() instanceof Stage.Filter);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void viaNull() {
        builder().via((ProcessorBuilder) null);
    }

    @Test
    public void onError() {
        Consumer consumer = t -> {
        };
        Graph graph = graphFor(builder().onError(consumer));
        assertSame(getAddedStage(Stage.OnError.class, graph).getConsumer(), consumer);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorNullConsumer() {
        builder().onError(null);
    }

    @Test
    public void onErrorResume() {
        Graph graph = graphFor(builder().onErrorResume(t -> 2));
        assertEquals(getAddedStage(Stage.OnErrorResume.class, graph).getFunction().apply(new RuntimeException()), 2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorResumeNull() {
        builder().onErrorResume(null);
    }

    @Test
    public void onErrorResumeWith() {
        Graph graph = graphFor(builder().onErrorResumeWith(t -> rs.empty()));
        Graph resumeWith =
                getAddedStage(Stage.OnErrorResumeWith.class, graph).getFunction().apply(new RuntimeException());
        assertEquals(resumeWith.getStages().size(), 1);
        assertEmptyStage(resumeWith.getStages().iterator().next());
    }

    @Test
    public void onErrorResumeWithToBuilderFromDifferentReactiveStreamsImplementation() {
        Graph graph = graphFor(builder().onErrorResumeWith(t -> Mocks.EMPTY_PUBLISHER_BUILDER));
        Graph resumeWith =
                getAddedStage(Stage.OnErrorResumeWith.class, graph).getFunction().apply(new RuntimeException());
        assertSame(resumeWith, Mocks.EMPTY_PUBLISHER_GRAPH);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorResumeWithNull() {
        builder().onErrorResumeWith(null);
    }

    @Test
    public void onErrorResumeWithRsPublisher() {
        Graph graph = graphFor(builder().onErrorResumeWithRsPublisher(t -> Mocks.PUBLISHER));
        Graph resumeWith =
                getAddedStage(Stage.OnErrorResumeWith.class, graph).getFunction().apply(new RuntimeException());
        assertEquals(resumeWith.getStages().size(), 1);
        assertSame(((Stage.PublisherStage) resumeWith.getStages().iterator().next()).getRsPublisher(), Mocks.PUBLISHER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onErrorResumeWithRsPublisherNull() {
        builder().onErrorResumeWithRsPublisher(null);
    }

    @Test
    public void onTerminate() {
        Runnable action = () -> {
        };
        Graph graph = graphFor(builder().onTerminate(action));
        assertSame(getAddedStage(Stage.OnTerminate.class, graph).getAction(), action);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onTerminateNull() {
        builder().onTerminate(null);
    }

    @Test
    public void onComplete() {
        Runnable action = () -> {
        };
        Graph graph = graphFor(builder().onComplete(action));
        assertSame(getAddedStage(Stage.OnComplete.class, graph).getAction(), action);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void onCompleteNull() {
        builder().onComplete(null);
    }

    @Test
    public void buildRs() {
        AtomicReference<Graph> builtGraph = new AtomicReference<>();
        Publisher publisher = builder().distinct().buildRs(new ReactiveStreamsEngine() {
            @Override
            public <T> Publisher<T> buildPublisher(Graph graph) throws UnsupportedStageException {
                builtGraph.set(graph);
                return Mocks.PUBLISHER;
            }
            @Override
            public <T, R> SubscriberWithCompletionStage<T, R> buildSubscriber(Graph graph)
                    throws UnsupportedStageException {
                throw new RuntimeException("Wrong method invoked");
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

        assertSame(publisher, Mocks.PUBLISHER);
        getAddedStage(Stage.Distinct.class, builtGraph.get());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void buildRsNull() {
        builder().buildRs(null);
    }

    @Test
    public void builderShouldBeImmutable() {
        PublisherBuilder<Integer> builder = builder();
        PublisherBuilder<Integer> mapped = builder.map(Function.identity());
        PublisherBuilder<Integer> distinct = builder.distinct();
        CompletionRunner<Void> cancelled = builder.cancel();
        getAddedStage(Stage.Map.class, graphFor(mapped));
        getAddedStage(Stage.Distinct.class, graphFor(distinct));
        getAddedStage(Stage.Cancel.class, graphFor(cancelled));
    }

    private PublisherBuilder<Integer> builder() {
        return rs.of(1);
    }

    private <S extends Stage> S getAddedStage(Class<S> clazz, Graph graph) {
        assertEquals(graph.getStages().size(), 2, "Graph does not have two stages");
        Iterator<Stage> stages = graph.getStages().iterator();
        Stage first = stages.next();
        assertTrue(first instanceof Stage.Of, "First stage " + first + " is not a " + Stage.Of.class);
        Stage second = stages.next();
        assertTrue(clazz.isInstance(second), "Second stage " + second + " is not a " + clazz);
        return clazz.cast(second);
    }

}
