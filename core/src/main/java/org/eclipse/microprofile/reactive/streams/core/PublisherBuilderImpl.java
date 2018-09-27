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

import org.eclipse.microprofile.reactive.streams.CompletionRunner;
import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

final class PublisherBuilderImpl<T> extends ReactiveStreamsGraphBuilder implements PublisherBuilder<T> {

    PublisherBuilderImpl(Stage stage, ReactiveStreamsGraphBuilder previous) {
        super(stage, previous);
    }

    PublisherBuilderImpl(Stage stage) {
        super(stage, null);
    }

    @Override
    public <R> PublisherBuilder<R> map(Function<? super T, ? extends R> mapper) {
        return addStage(new Stages.Map(mapper));
    }

    @Override
    public PublisherBuilder<T> peek(Consumer<? super T> consumer) {
        return addStage(new Stages.Peek(consumer));
    }

    @Override
    public PublisherBuilder<T> filter(Predicate<? super T> predicate) {
        return addStage(new Stages.Filter(predicate));
    }

    @Override
    public PublisherBuilder<T> distinct() {
        return addStage(Stages.Distinct.INSTANCE);
    }

    @Override
    public <S> PublisherBuilder<S> flatMap(Function<? super T, PublisherBuilder<? extends S>> mapper) {
        return addStage(new Stages.FlatMap(mapper.andThen(ReactiveStreamsGraphBuilder::rsBuilderToGraph)));
    }

    @Override
    public <S> PublisherBuilder<S> flatMapRsPublisher(Function<? super T, Publisher<? extends S>> mapper) {
        return addStage(new Stages.FlatMap(mapper.andThen(ReactiveStreamsGraphBuilder::publisherToGraph)));
    }

    @Override
    public <S> PublisherBuilder<S> flatMapCompletionStage(Function<? super T, ? extends CompletionStage<? extends S>> mapper) {
        return addStage(new Stages.FlatMapCompletionStage((Function) mapper));
    }

    @Override
    public <S> PublisherBuilder<S> flatMapIterable(Function<? super T, ? extends Iterable<? extends S>> mapper) {
        return addStage(new Stages.FlatMapIterable((Function) mapper));
    }

    @Override
    public PublisherBuilder<T> limit(long maxSize) {
        return addStage(new Stages.Limit(maxSize));
    }

    @Override
    public PublisherBuilder<T> skip(long n) {
        return addStage(new Stages.Skip(n));
    }

    @Override
    public PublisherBuilder<T> takeWhile(Predicate<? super T> predicate) {
        return addStage(new Stages.TakeWhile(predicate));
    }

    @Override
    public PublisherBuilder<T> dropWhile(Predicate<? super T> predicate) {
        return addStage(new Stages.DropWhile(predicate));
    }

    @Override
    public CompletionRunner<Void> forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "Action must not be null");
        return collect(Collector.<T, Void, Void>of(
            () -> null,
            (n, t) -> action.accept(t),
            (v1, v2) -> null,
            v -> null
        ));
    }

    @Override
    public CompletionRunner<Void> ignore() {
        return forEach(r -> {
        });
    }

    @Override
    public CompletionRunner<Void> cancel() {
        return addTerminalStage(Stages.Cancel.INSTANCE);
    }

    @Override
    public CompletionRunner<T> reduce(T identity, BinaryOperator<T> accumulator) {
        return addTerminalStage(new Stages.Collect(Reductions.reduce(identity, accumulator)));
    }

    @Override
    public CompletionRunner<Optional<T>> reduce(BinaryOperator<T> accumulator) {
        return addTerminalStage(new Stages.Collect(Reductions.reduce(accumulator)));
    }

    @Override
    public CompletionRunner<Optional<T>> findFirst() {
        return addTerminalStage(Stages.FindFirst.INSTANCE);
    }

    @Override
    public <R, A> CompletionRunner<R> collect(Collector<? super T, A, R> collector) {
        return addTerminalStage(new Stages.Collect(collector));
    }

    @Override
    public <R> CompletionRunner<R> collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator) {
        // The combiner is not used, so the used, but should not be null
        return addTerminalStage(new Stages.Collect(Collector.of(supplier, accumulator, (a, b) -> a)));
    }

    @Override
    public CompletionRunner<List<T>> toList() {
        return collect(Collectors.toList());
    }

    @Override
    public CompletionRunner<Void> to(Subscriber<T> subscriber) {
        return addTerminalStage(new Stages.SubscriberStage(subscriber));
    }

    @Override
    public <R> CompletionRunner<R> to(SubscriberBuilder<T, R> subscriber) {
        Objects.requireNonNull(subscriber, "Subscriber must not be null");
        return addTerminalStage(InternalStages.nested(subscriber));
    }

    @Override
    public <R> PublisherBuilder<R> via(ProcessorBuilder<T, R> processor) {
        return addStage(InternalStages.nested(processor));
    }

    @Override
    public <R> PublisherBuilder<R> via(Processor<T, R> processor) {
        return addStage(new Stages.ProcessorStage(processor));
    }

    @Override
    public PublisherBuilder<T> onError(Consumer<Throwable> errorHandler) {
        return addStage(new Stages.OnError(errorHandler));
    }

    @Override
    public PublisherBuilder<T> onErrorResume(Function<Throwable, T> errorHandler) {
        return addStage(new Stages.OnErrorResume(errorHandler));
    }

    @Override
    public PublisherBuilder<T> onErrorResumeWith(Function<Throwable, PublisherBuilder<T>> errorHandler) {
        return addStage(new Stages.OnErrorResumeWith(errorHandler.andThen(ReactiveStreamsGraphBuilder::rsBuilderToGraph)));
    }

    @Override
    public <S> PublisherBuilder<S> onErrorResumeWithRsPublisher(Function<Throwable, Publisher<? extends S>> errorHandler) {
        return addStage(new Stages.OnErrorResumeWith(errorHandler.andThen(ReactiveStreamsGraphBuilder::publisherToGraph)));
    }

    @Override
    public PublisherBuilder<T> onTerminate(Runnable action) {
        return addStage(new Stages.OnTerminate(action));
    }

    @Override
    public PublisherBuilder<T> onComplete(Runnable action) {
        return addStage(new Stages.OnComplete(action));
    }

    public Graph toGraph() {
        return build(false, true);
    }

    @Override
    public Publisher<T> buildRs() {
        return buildRs(defaultEngine());
    }

    @Override
    public Publisher<T> buildRs(ReactiveStreamsEngine engine) {
        Objects.requireNonNull(engine, "Engine must not be null");
        return engine.buildPublisher(toGraph());
    }

    private <R> PublisherBuilder<R> addStage(Stage stage) {
        return new PublisherBuilderImpl<>(stage, this);
    }

    private <R> CompletionRunner<R> addTerminalStage(Stage stage) {
        return new CompletionRunnerImpl<>(stage, this);
    }
}
