/*******************************************************************************
 * Copyright (c) 2018, 2020 Contributors to the Eclipse Foundation
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

import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreamsFactory;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ReactiveStreamsFactoryImpl implements ReactiveStreamsFactory {

    @Override
    public <T> PublisherBuilder<T> fromPublisher(Publisher<? extends T> publisher) {
        return new PublisherBuilderImpl<>(new Stages.PublisherStage(publisher));
    }

    @Override
    public <T> PublisherBuilder<T> of(T t) {
        return new PublisherBuilderImpl<>(new Stages.Of(Collections.singletonList(
            Objects.requireNonNull(t, "Reactive Streams does not support null elements"))));
    }

    @Override
    public <T> PublisherBuilder<T> of(T... ts) {
        return fromIterable(Arrays.asList(ts));
    }

    @Override
    public <T> PublisherBuilder<T> empty() {
        return new PublisherBuilderImpl<>(Stages.Of.EMPTY);
    }

    @Override
    public <T> PublisherBuilder<T> ofNullable(T t) {
        return t == null ? empty() : of(t);
    }

    @Override
    public <T> PublisherBuilder<T> fromIterable(Iterable<? extends T> ts) {
        return new PublisherBuilderImpl<>(new Stages.Of(ts));
    }

    @Override
    public <T> PublisherBuilder<T> failed(Throwable t) {
        return new PublisherBuilderImpl<>(new Stages.Failed(t));
    }

    @Override
    public <T> ProcessorBuilder<T, T> builder() {
        return new ProcessorBuilderImpl<>(InternalStages.Identity.INSTANCE, null);
    }

    @Override
    public <T, R> ProcessorBuilder<T, R> fromProcessor(Processor<? super T, ? extends R> processor) {
        return new ProcessorBuilderImpl<>(new Stages.ProcessorStage(processor), null);
    }

    @Override
    public <T> SubscriberBuilder<T, Void> fromSubscriber(Subscriber<? super T> subscriber) {
        return new SubscriberBuilderImpl<>(new Stages.SubscriberStage(subscriber), null);
    }

    @Override
    public <T> PublisherBuilder<T> iterate(T seed, UnaryOperator<T> f) {
        Objects.requireNonNull(f, "Operator must not be null");
        return fromIterable(() -> Stream.iterate(seed, f).iterator());
    }

    @Override
    public <T> PublisherBuilder<T> generate(Supplier<? extends T> s) {
        Objects.requireNonNull(s, "Supplier must not be null");
        return fromIterable(() -> Stream.<T>generate((Supplier) s).iterator());
    }

    @Override
    public <T> PublisherBuilder<T> concat(PublisherBuilder<? extends T> a,
                                                 PublisherBuilder<? extends T> b) {
        return new PublisherBuilderImpl<>(new Stages.Concat(
            ReactiveStreamsGraphBuilder.rsBuilderToGraph(a),
            ReactiveStreamsGraphBuilder.rsBuilderToGraph(b)
        ));
    }

    @Override
    public <T> PublisherBuilder<T> fromCompletionStage(CompletionStage<? extends T> completionStage) {
        return new PublisherBuilderImpl<>(new Stages.FromCompletionStage(completionStage));
    }

    @Override
    public <T> PublisherBuilder<T> fromCompletionStageNullable(CompletionStage<? extends T> completionStage) {
        return new PublisherBuilderImpl<>(new Stages.FromCompletionStageNullable(completionStage));
    }

    @Override
    public <T, R> ProcessorBuilder<T, R> coupled(SubscriberBuilder<? super T, ?> subscriber,
        PublisherBuilder<? extends R> publisher) {
        Graph sGraph = ReactiveStreamsGraphBuilder.rsBuilderToGraph(Objects.requireNonNull(subscriber,
            "Subscriber must not be null"));
        Graph pGraph = ReactiveStreamsGraphBuilder.rsBuilderToGraph(Objects.requireNonNull(publisher,
            "Publisher must not be null"));
        return new ProcessorBuilderImpl<>(new Stages.Coupled(sGraph, pGraph), null);
    }

    @Override
    public <T, R> ProcessorBuilder<T, R> coupled(Subscriber<? super T> subscriber, Publisher<? extends R> publisher) {
        return coupled(fromSubscriber(subscriber), fromPublisher(publisher));
    }
}
