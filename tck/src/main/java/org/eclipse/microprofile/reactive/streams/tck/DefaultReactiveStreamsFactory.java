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

package org.eclipse.microprofile.reactive.streams.tck;

import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.ReactiveStreamsFactory;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Implementation of the {@link ReactiveStreamsFactory} that delegates to {@link ReactiveStreams} static factory
 * methods.
 */
public class DefaultReactiveStreamsFactory implements ReactiveStreamsFactory {

    @Override
    public <T> PublisherBuilder<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveStreams.fromPublisher(publisher);
    }

    @Override
    public <T> PublisherBuilder<T> of(T t) {
        return ReactiveStreams.of(t);
    }

    @Override
    public <T> PublisherBuilder<T> of(T... ts) {
        return ReactiveStreams.of(ts);
    }

    @Override
    public <T> PublisherBuilder<T> empty() {
        return ReactiveStreams.empty();
    }

    @Override
    public <T> PublisherBuilder<T> ofNullable(T t) {
        return ReactiveStreams.ofNullable(t);
    }

    @Override
    public <T> PublisherBuilder<T> fromIterable(Iterable<? extends T> ts) {
        return ReactiveStreams.fromIterable(ts);
    }

    @Override
    public <T> PublisherBuilder<T> failed(Throwable t) {
        return ReactiveStreams.failed(t);
    }

    @Override
    public <T> ProcessorBuilder<T, T> builder() {
        return ReactiveStreams.builder();
    }

    @Override
    public <T, R> ProcessorBuilder<T, R> fromProcessor(Processor<? super T, ? extends R> processor) {
        return ReactiveStreams.fromProcessor(processor);
    }

    @Override
    public <T> SubscriberBuilder<T, Void> fromSubscriber(Subscriber<? extends T> subscriber) {
        return ReactiveStreams.fromSubscriber(subscriber);
    }

    @Override
    public <T> PublisherBuilder<T> iterate(T seed, UnaryOperator<T> f) {
        return ReactiveStreams.iterate(seed, f);
    }

    @Override
    public <T> PublisherBuilder<T> generate(Supplier<? extends T> s) {
        return ReactiveStreams.generate(s);
    }

    @Override
    public <T> PublisherBuilder<T> concat(PublisherBuilder<? extends T> a, PublisherBuilder<? extends T> b) {
        return ReactiveStreams.concat(a, b);
    }

    @Override
    public <T> PublisherBuilder<T> fromCompletionStage(CompletionStage<? extends T> completionStage) {
        return ReactiveStreams.fromCompletionStage(completionStage);
    }

    @Override
    public <T> PublisherBuilder<T> fromCompletionStageNullable(CompletionStage<? extends T> completionStage) {
        return ReactiveStreams.fromCompletionStageNullable(completionStage);
    }
}
