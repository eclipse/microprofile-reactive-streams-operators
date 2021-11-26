/*******************************************************************************
 * Copyright (c) 2018, 2021 Contributors to the Eclipse Foundation
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

import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * A builder for a {@link Processor}.
 * <p>
 * The documentation for each operator uses marble diagrams to visualize how the operator functions. Each element
 * flowing in and out of the stream is represented as a coloured marble that has a value, with the operator applying
 * some transformation or some side effect, termination and error signals potentially being passed, and for operators
 * that subscribe to the stream, an output value being redeemed at the end.
 * <p>
 * Below is an example diagram labelling all the parts of the stream.
 * <p>
 * <img src="doc-files/example.png" alt="Example marble diagram">
 * <p>
 * Instances of this interface are immutable. Methods which return a {@code ProcessorBuilder} will return a new
 * instance.
 *
 * @param <T>
 *            The type of the elements that the processor consumes.
 * @param <R>
 *            The type of the elements that the processor emits.
 * @see ReactiveStreams
 */
public interface ProcessorBuilder<T, R>
        extends
            TransformingOperators<R>,
            FilteringOperators<R>,
            PeekingOperators<R>,
            ConsumingOperators<R>,
            ErrorHandlingOperators<R>,
            ConnectingOperators<R> {

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> map(Function<? super R, ? extends S> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> flatMap(Function<? super R, ? extends PublisherBuilder<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> flatMapRsPublisher(Function<? super R, ? extends Publisher<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> flatMapCompletionStage(
            Function<? super R, ? extends CompletionStage<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> flatMapIterable(Function<? super R, ? extends Iterable<? extends S>> mapper);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> filter(Predicate<? super R> predicate);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> distinct();

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> limit(long maxSize);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> skip(long n);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> takeWhile(Predicate<? super R> predicate);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> dropWhile(Predicate<? super R> predicate);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> peek(Consumer<? super R> consumer);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> onError(Consumer<Throwable> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> onTerminate(Runnable action);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> onComplete(Runnable action);

    /**
     * {@inheritDoc}
     * 
     * @return A {@link SubscriberBuilder} that will invoke the action for each element of the stream.
     */
    @Override
    SubscriberBuilder<T, Void> forEach(Consumer<? super R> action);

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} for the stream.
     */
    @Override
    SubscriberBuilder<T, Void> ignore();

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} for the stream.
     */
    @Override
    SubscriberBuilder<T, Void> cancel();

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} for the reduction.
     */
    @Override
    SubscriberBuilder<T, R> reduce(R identity, BinaryOperator<R> accumulator);

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} for the reduction.
     */
    @Override
    SubscriberBuilder<T, Optional<R>> reduce(BinaryOperator<R> accumulator);

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} that will emit the collected result.
     */
    @Override
    <S, A> SubscriberBuilder<T, S> collect(Collector<? super R, A, S> collector);

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} that will emit the collected result.
     */
    @Override
    <S> SubscriberBuilder<T, S> collect(Supplier<S> supplier, BiConsumer<S, ? super R> accumulator);

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder} that will emit the list.
     */
    @Override
    SubscriberBuilder<T, List<R>> toList();

    /**
     * {@inheritDoc}
     * 
     * @return A new {@link SubscriberBuilder}.
     */
    @Override
    SubscriberBuilder<T, Optional<R>> findFirst();

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> onErrorResume(Function<Throwable, ? extends R> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> onErrorResumeWith(Function<Throwable, ? extends PublisherBuilder<? extends R>> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    ProcessorBuilder<T, R> onErrorResumeWithRsPublisher(
            Function<Throwable, ? extends Publisher<? extends R>> errorHandler);

    /**
     * {@inheritDoc}
     */
    @Override
    SubscriberBuilder<T, Void> to(Subscriber<? super R> subscriber);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> SubscriberBuilder<T, S> to(SubscriberBuilder<? super R, ? extends S> subscriber);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> via(ProcessorBuilder<? super R, ? extends S> processor);

    /**
     * {@inheritDoc}
     */
    @Override
    <S> ProcessorBuilder<T, S> via(Processor<? super R, ? extends S> processor);

    /**
     * Build this stream, using the first {@link ReactiveStreamsEngine} found by the {@link java.util.ServiceLoader}.
     *
     * @return A {@link Processor} that will run this stream.
     */
    Processor<T, R> buildRs();

    /**
     * Build this stream, using the supplied {@link ReactiveStreamsEngine}.
     *
     * @param engine
     *            The engine to run the stream with.
     * @return A {@link Processor} that will run this stream.
     */
    Processor<T, R> buildRs(ReactiveStreamsEngine engine);
}
