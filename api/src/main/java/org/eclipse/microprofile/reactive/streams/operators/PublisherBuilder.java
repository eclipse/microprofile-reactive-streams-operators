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

package org.eclipse.microprofile.reactive.streams.operators;

import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A builder for a {@link Publisher}.
 * <p>
 * The documentation for each operator uses marble diagrams to visualize how the operator functions. Each element
 * flowing in and out of the stream is represented as a coloured marble that has a value, with the operator
 * applying some transformation or some side effect, termination and error signals potentially being passed, and
 * for operators that subscribe to the stream, an output value being redeemed at the end.
 * <p>
 * Below is an example diagram labelling all the parts of the stream.
 * <p>
 * <img src="doc-files/example.png" alt="Example marble diagram">
 * <p>
 * Instances of this interface are immutable. Methods which return a {@code PublisherBuilder} will return a new instance.
 *
 * @param <T> The type of the elements that the publisher emits.
 * @see ReactiveStreams
 */
public interface PublisherBuilder<T> extends TransformingOperators<T>, FilteringOperators<T>, PeekingOperators<T>, ConsumingOperators<T>,
    ErrorHandlingOperators<T>, ConnectingOperators<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    <R> PublisherBuilder<R> map(Function<? super T, ? extends R> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> PublisherBuilder<S> flatMap(Function<? super T, ? extends PublisherBuilder<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> PublisherBuilder<S> flatMapRsPublisher(Function<? super T, ? extends Publisher<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> PublisherBuilder<S> flatMapCompletionStage(Function<? super T, ? extends CompletionStage<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> PublisherBuilder<S> flatMapIterable(Function<? super T, ? extends Iterable<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> filter(Predicate<? super T> predicate);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> distinct();

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> limit(long maxSize);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> skip(long n);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> takeWhile(Predicate<? super T> predicate);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> dropWhile(Predicate<? super T> predicate);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> peek(Consumer<? super T> consumer);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> onError(Consumer<Throwable> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> onTerminate(Runnable action);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> onComplete(Runnable action);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream.
     */
    @Override
    CompletionRunner<Void> forEach(Consumer<? super T> action);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream.
     */
    @Override
    CompletionRunner<Void> ignore();

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream.
     */
    @Override
    CompletionRunner<Void> cancel();

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream.
     */
    @Override
    CompletionRunner<T> reduce(T identity, BinaryOperator<T> accumulator);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream.
     */
    @Override
    CompletionRunner<Optional<T>> reduce(BinaryOperator<T> accumulator);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream.
     */
    @Override
    CompletionRunner<Optional<T>> findFirst();

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream,
     * R is the result type of the collector's reduction operation.
     */
    @Override
    <R, A> CompletionRunner<R> collect(Collector<? super T, A, R> collector);
    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream which emits the collected result.
     */
    @Override
    <R> CompletionRunner<R> collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the stream that emits the list.
     */
    @Override
    CompletionRunner<List<T>> toList();

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> onErrorResume(Function<Throwable, ? extends T> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> onErrorResumeWith(Function<Throwable, ? extends PublisherBuilder<? extends T>> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    PublisherBuilder<T> onErrorResumeWithRsPublisher(Function<Throwable, ? extends Publisher<? extends T>> errorHandler);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the composed stream.
     */
    @Override
    CompletionRunner<Void> to(Subscriber<? super T> subscriber);

    /**
     * {@inheritDoc}
     * @return A new {@link CompletionRunner} that can be used to run the composed stream.
     */
    @Override
    <R> CompletionRunner<R> to(SubscriberBuilder<? super T, ? extends R> subscriber);

    /**
     * {@inheritDoc}
     */
    @Override
    <R> PublisherBuilder<R> via(ProcessorBuilder<? super T, ? extends R> processor);

    /**
     * {@inheritDoc}
     */
    @Override
    <R> PublisherBuilder<R> via(Processor<? super T, ? extends R> processor);
    /**
     * Build this stream, using the first {@link ReactiveStreamsEngine} found by the {@link java.util.ServiceLoader}.
     *
     * @return A {@link Publisher} that will run this stream.
     */
    Publisher<T> buildRs();

    /**
     * Build this stream, using the supplied {@link ReactiveStreamsEngine}.
     *
     * @param engine The engine to run the stream with.
     * @return A {@link Publisher} that will run this stream.
     */
    Publisher<T> buildRs(ReactiveStreamsEngine engine);
}
