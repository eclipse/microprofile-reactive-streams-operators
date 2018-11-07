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

import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
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

import static org.eclipse.microprofile.reactive.streams.core.ReactiveStreamsEngineResolver.instance;

final class ProcessorBuilderImpl<T, R> extends ReactiveStreamsGraphBuilder implements ProcessorBuilder<T, R> {

    ProcessorBuilderImpl(Stage stage, ReactiveStreamsGraphBuilder previous) {
        super(stage, previous);
    }

    @Override
    public <S> ProcessorBuilder<T, S> map(Function<? super R, ? extends S> mapper) {
        return addStage(new Stages.Map(mapper));
    }

    @Override
    public ProcessorBuilder<T, R> peek(Consumer<? super R> consumer) {
        return addStage(new Stages.Peek(consumer));
    }

    @Override
    public ProcessorBuilder<T, R> filter(Predicate<? super R> predicate) {
        return addStage(new Stages.Filter(predicate));
    }

    @Override
    public ProcessorBuilder<T, R> distinct() {
        return addStage(Stages.Distinct.INSTANCE);
    }

    @Override
    public <S> ProcessorBuilder<T, S> flatMap(Function<? super R, ? extends PublisherBuilder<? extends S>> mapper) {
        return addStage(new Stages.FlatMap(mapper.andThen(ReactiveStreamsGraphBuilder::rsBuilderToGraph)));
    }

    @Override
    public <S> ProcessorBuilder<T, S> flatMapRsPublisher(Function<? super R, ? extends Publisher<? extends S>> mapper) {
        return addStage(new Stages.FlatMap(mapper.andThen(ReactiveStreamsGraphBuilder::publisherToGraph)));
    }

    @Override
    public <S> ProcessorBuilder<T, S> flatMapCompletionStage(Function<? super R, ? extends CompletionStage<? extends S>> mapper) {
        return addStage(new Stages.FlatMapCompletionStage((Function) mapper));
    }

    @Override
    public <S> ProcessorBuilder<T, S> flatMapIterable(Function<? super R, ? extends Iterable<? extends S>> mapper) {
        return addStage(new Stages.FlatMapIterable((Function) mapper));
    }

    @Override
    public ProcessorBuilder<T, R> limit(long maxSize) {
        return addStage(new Stages.Limit(maxSize));
    }

    @Override
    public ProcessorBuilder<T, R> skip(long n) {
        return addStage(new Stages.Skip(n));
    }

    @Override
    public ProcessorBuilder<T, R> takeWhile(Predicate<? super R> predicate) {
        return addStage(new Stages.TakeWhile(predicate));
    }

    @Override
    public ProcessorBuilder<T, R> dropWhile(Predicate<? super R> predicate) {
        return addStage(new Stages.DropWhile(predicate));
    }

    @Override
    public SubscriberBuilder<T, Void> forEach(Consumer<? super R> action) {
        Objects.requireNonNull(action, "Action must not be null");
        return collect(Collector.<R, Void, Void>of(
            () -> null,
            (n, r) -> action.accept(r),
            (v1, v2) -> null,
            v -> null
        ));
    }

    @Override
    public SubscriberBuilder<T, Void> ignore() {
        return forEach(r -> {
        });
    }

    @Override
    public SubscriberBuilder<T, Void> cancel() {
        return addTerminalStage(Stages.Cancel.INSTANCE);
    }

    @Override
    public SubscriberBuilder<T, R> reduce(R identity, BinaryOperator<R> accumulator) {
        return addTerminalStage(new Stages.Collect(Reductions.reduce(identity, accumulator)));
    }

    @Override
    public SubscriberBuilder<T, Optional<R>> reduce(BinaryOperator<R> accumulator) {
        return addTerminalStage(new Stages.Collect(Reductions.reduce(accumulator)));
    }

    @Override
    public <S, A> SubscriberBuilder<T, S> collect(Collector<? super R, A, S> collector) {
        return addTerminalStage(new Stages.Collect(collector));
    }

    @Override
    public <S> SubscriberBuilder<T, S> collect(Supplier<S> supplier, BiConsumer<S, ? super R> accumulator) {
        // The combiner is not used, so the used, but should not be null
        return addTerminalStage(new Stages.Collect(Collector.of(supplier, accumulator, (a, b) -> a)));
    }

    @Override
    public SubscriberBuilder<T, List<R>> toList() {
        return collect(Collectors.toList());
    }

    @Override
    public SubscriberBuilder<T, Optional<R>> findFirst() {
        return addTerminalStage(Stages.FindFirst.INSTANCE);
    }

    @Override
    public SubscriberBuilder<T, Void> to(Subscriber<? super R> subscriber) {
        return addTerminalStage(new Stages.SubscriberStage(subscriber));
    }

    @Override
    public <S> SubscriberBuilder<T, S> to(SubscriberBuilder<? super R, ? extends S> subscriber) {
        return addTerminalStage(InternalStages.nested(subscriber));
    }

    @Override
    public ProcessorBuilder<T, R> onError(Consumer<Throwable> errorHandler) {
        return addStage(new Stages.OnError(errorHandler));
    }

    @Override
    public ProcessorBuilder<T, R> onErrorResume(Function<Throwable, ? extends R> errorHandler) {
        return addStage(new Stages.OnErrorResume(errorHandler));
    }

    @Override
    public ProcessorBuilder<T, R> onErrorResumeWith(Function<Throwable, ? extends PublisherBuilder<? extends R>> errorHandler) {
        return addStage(new Stages.OnErrorResumeWith(errorHandler.andThen(ReactiveStreamsGraphBuilder::rsBuilderToGraph)));
    }

    @Override
    public ProcessorBuilder<T, R> onErrorResumeWithRsPublisher(Function<Throwable, ? extends Publisher<? extends R>> errorHandler) {
        return addStage(new Stages.OnErrorResumeWith(errorHandler.andThen(ReactiveStreamsGraphBuilder::publisherToGraph)));
    }

    @Override
    public ProcessorBuilder<T, R> onTerminate(Runnable action) {
        return addStage(new Stages.OnTerminate(action));
    }

    @Override
    public ProcessorBuilder<T, R> onComplete(Runnable action) {
        return addStage(new Stages.OnComplete(action));
    }

    @Override
    public <S> ProcessorBuilder<T, S> via(ProcessorBuilder<? super R, ? extends S> processor) {
        return addStage(InternalStages.nested(processor));
    }

    @Override
    public <S> ProcessorBuilder<T, S> via(Processor<? super R, ? extends S> processor) {
        return addStage(new Stages.ProcessorStage(processor));
    }

    @Override
    public Processor<T, R> buildRs() {
        return buildRs(instance());
    }

    @Override
    public Processor<T, R> buildRs(ReactiveStreamsEngine engine) {
        Objects.requireNonNull(engine, "Engine must not be null");
        return engine.buildProcessor(toGraph());
    }

    private <S> ProcessorBuilder<T, S> addStage(Stage stage) {
        return new ProcessorBuilderImpl<>(stage, this);
    }

    private <S> SubscriberBuilder<T, S> addTerminalStage(Stage stage) {
        return new SubscriberBuilderImpl<>(stage, this);
    }
}
