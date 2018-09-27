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

package org.eclipse.microprofile.reactive.streams;

import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
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
 *
 * @param <T> The type of the elements that the publisher emits.
 * @see ReactiveStreams
 */
public interface PublisherBuilder<T> {

    /**
     * Map the elements emitted by this publisher using the {@code mapper} function.
     * <p>
     * <img src="doc-files/map.png" alt="map marbles diagram">
     *
     * @param mapper The function to use to map the elements.
     * @param <R>    The type of elements that the {@code mapper} function emits.
     * @return A new publisher builder that emits the mapped elements.
     */
    <R> PublisherBuilder<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action on each
     * element.
     * <p>
     * <img src="doc-files/peek.png" alt="peek marbles diagram">
     *
     * @param consumer The function called for every element.
     * @return A new processor builder that consumes elements of type <code>T</code> and emits the same elements. In between,
     * the given function is called for each element.
     */
    PublisherBuilder<T> peek(Consumer<? super T> consumer);

    /**
     * Filter elements emitted by this publisher using the given {@link Predicate}.
     * <p>
     * Any elements that return {@code true} when passed to the {@link Predicate} will be emitted, all other
     * elements will be dropped.
     * <p>
     * <img src="doc-files/filter.png" alt="filter marbles diagram">
     *
     * @param predicate The predicate to apply to each element.
     * @return A new publisher builder.
     */
    PublisherBuilder<T> filter(Predicate<? super T> predicate);

    /**
     * Creates a stream consisting of the distinct elements (according to {@link Object#equals(Object)}) of this stream.
     * <p>
     * <img src="doc-files/distinct.png" alt="distinct marbles diagram">
     *
     * @return A new publisher builder emitting the distinct elements from this stream.
     */
    PublisherBuilder<T> distinct();

    /**
     * Map the elements to publishers, and flatten so that the elements emitted by publishers produced by the
     * {@code mapper} function are emitted from this stream.
     * <p>
     * <img src="doc-files/flatMap.png" alt="flatMap marbles diagram">
     * <p>
     * This method operates on one publisher at a time. The result is a concatenation of elements emitted from all the
     * publishers produced by the mapper function.
     * <p>
     * Unlike {@link #flatMapRsPublisher(Function)}}, the mapper function returns a {@link PublisherBuilder} instead of a
     * {@link Publisher}.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new publisher.
     * @return A new publisher builder.
     */
    <S> PublisherBuilder<S> flatMap(Function<? super T, PublisherBuilder<? extends S>> mapper);

    /**
     * Map the elements to publishers, and flatten so that the elements emitted by publishers produced by the
     * {@code mapper} function are emitted from this stream.
     * <p>
     * <img src="doc-files/flatMapRsPublisher.png" alt="flatMapRsPublisher marble diagram">
     * <p>
     * This method operates on one publisher at a time. The result is a concatenation of elements emitted from all the
     * publishers produced by the mapper function.
     * <p>
     * Unlike {@link #flatMap(Function)}, the mapper function returns a {@link Publisher} instead of a
     * {@link PublisherBuilder}.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new publisher.
     * @return A new publisher builder.
     */
    <S> PublisherBuilder<S> flatMapRsPublisher(Function<? super T, Publisher<? extends S>> mapper);

    /**
     * Map the elements to {@link CompletionStage}, and flatten so that the elements the values redeemed by each
     * {@link CompletionStage} are emitted from this publisher.
     * <p>
     * <img src="doc-files/flatMapCompletionStage.png" alt="flatMapCompletionStage marble diagram">
     * <p>
     * This method only works with one element at a time. When an element is received, the {@code mapper} function is
     * executed, and the next element is not consumed or passed to the {@code mapper} function until the previous
     * {@link CompletionStage} is redeemed. Hence this method also guarantees that ordering of the stream is maintained.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new publisher.
     * @return A new publisher builder.
     */
    <S> PublisherBuilder<S> flatMapCompletionStage(Function<? super T, ? extends CompletionStage<? extends S>> mapper);

    /**
     * Map the elements to {@link Iterable}'s, and flatten so that the elements contained in each iterable are
     * emitted by this stream.
     * <p>
     * <img src="doc-files/flatMapIterable.png" alt="flatMapIterable marble diagram">
     * <p>
     * This method operates on one iterable at a time. The result is a concatenation of elements contain in all the
     * iterables returned by the {@code mapper} function.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new publisher.
     * @return A new publisher builder.
     */
    <S> PublisherBuilder<S> flatMapIterable(Function<? super T, ? extends Iterable<? extends S>> mapper);

    /**
     * Truncate this stream, ensuring the stream is no longer than {@code maxSize} elements in length.
     * <p>
     * <img src="doc-files/limit.png" alt="limit marble diagram">
     * <p>
     * If {@code maxSize} is reached, the stream will be completed, and upstream will be cancelled. Completion of the
     * stream will occur immediately when the element that satisfies the {@code maxSize} is received.
     *
     * @param maxSize The maximum size of the returned stream.
     * @return A new publisher builder.
     * @throws IllegalArgumentException If {@code maxSize} is less than zero.
     */
    PublisherBuilder<T> limit(long maxSize);

    /**
     * Discard the first {@code n} of this stream. If this stream contains fewer than {@code n} elements, this stream will
     * effectively be an empty stream.
     * <p>
     * <img src="doc-files/skip.png" alt="skip marble diagram">
     *
     * @param n The number of elements to discard.
     * @return A new publisher builder.
     * @throws IllegalArgumentException If {@code n} is less than zero.
     */
    PublisherBuilder<T> skip(long n);

    /**
     * Take the longest prefix of elements from this stream that satisfy the given {@code predicate}.
     * <p>
     * <img src="doc-files/takeWhile.png" alt="takeWhile marble diagram">
     * <p>
     * When the {@code predicate} returns false, the stream will be completed, and upstream will be cancelled.
     *
     * @param predicate The predicate.
     * @return A new publisher builder.
     */
    PublisherBuilder<T> takeWhile(Predicate<? super T> predicate);

    /**
     * Drop the longest prefix of elements from this stream that satisfy the given {@code predicate}.
     * <p>
     * <img src="doc-files/dropWhile.png" alt="dropWhile marble diagram">
     * <p>
     * As long as the {@code predicate} returns true, no elements will be emitted from this stream. Once the first element
     * is encountered for which the {@code predicate} returns false, all subsequent elements will be emitted, and the
     * {@code predicate} will no longer be invoked.
     *
     * @param predicate The predicate.
     * @return A new publisher builder.
     */
    PublisherBuilder<T> dropWhile(Predicate<? super T> predicate);

    /**
     * Performs an action for each element on this stream.
     * <p>
     * <img src="doc-files/forEach.png" alt="forEach marble diagram">
     * <p>
     * The returned {@link CompletionStage} will be redeemed when the stream completes, either successfully if the stream
     * completes normally, or with an error if the stream completes with an error or if the action throws an exception.
     *
     * @param action The action.
     * @return A new completion builder.
     */
    CompletionRunner<Void> forEach(Consumer<? super T> action);

    /**
     * Ignores each element of this stream.
     * <p>
     * <img src="doc-files/ignore.png" alt="ignore marble diagram">
     * <p>
     * The returned {@link CompletionStage} will be redeemed when the stream completes, either successfully if the
     * stream completes normally, or with an error if the stream completes with an error or if the action throws an
     * exception.
     *
     * @return A new completion builder.
     */
    CompletionRunner<Void> ignore();

    /**
     * Cancels the stream as soon as it starts.
     * <p>
     * The returned {@link CompletionStage} will be immediately redeemed as soon as the stream starts.
     *
     * @return A new completion builder.
     */
    CompletionRunner<Void> cancel();

    /**
     * Perform a reduction on the elements of this stream, using the provided identity value and the accumulation
     * function.
     * <p>
     * <img src="doc-files/reduce-identity.png" alt="reduce marble diagram">
     * <p>
     * The result of the reduction is returned in the {@link CompletionStage}.
     *
     * @param identity    The identity value.
     * @param accumulator The accumulator function.
     * @return A new completion builder.
     */
    CompletionRunner<T> reduce(T identity, BinaryOperator<T> accumulator);

    /**
     * Perform a reduction on the elements of this stream, using provided the accumulation function.
     * <p>
     * <img src="doc-files/reduce.png" alt="reduce marble diagram">
     * <p>
     * The result of the reduction is returned in the {@link CompletionStage}. If there are no elements in this stream,
     * empty will be returned.
     *
     * @param accumulator The accumulator function.
     * @return A new completion builder.
     */
    CompletionRunner<Optional<T>> reduce(BinaryOperator<T> accumulator);

    /**
     * Find the first element emitted by the {@link Publisher}, and return it in a
     * {@link CompletionStage}.
     * <p>
     * <img src="doc-files/findFirst.png" alt="findFirst marble diagram">
     * <p>
     * If the stream is completed before a single element is emitted, then {@link Optional#empty()} will be emitted.
     *
     * @return A {@link CompletionRunner} that emits the element when found.
     */
    CompletionRunner<Optional<T>> findFirst();

    /**
     * Collect the elements emitted by this publisher builder using the given {@link Collector}.
     * <p>
     * Since Reactive Streams are intrinsically sequential, only the accumulator of the collector will be used, the
     * combiner will not be used.
     *
     * @param collector The collector to collect the elements.
     * @param <R>       The result of the collector.
     * @param <A>       The accumulator type.
     * @return A {@link CompletionRunner} that emits the collected result.
     */
    <R, A> CompletionRunner<R> collect(Collector<? super T, A, R> collector);

    /**
     * Collect the elements emitted by this processor builder using a {@link Collector} built from the given
     * {@link Supplier supplier} and {@link BiConsumer accumulator}.
     * <p>
     * <img src="doc-files/collect.png" alt="collect marble diagram">
     * <p>
     * Since Reactive Streams are intrinsically sequential, the combiner will not be used. This is why this method does not
     * accept a <em>combiner</em> method.
     *
     * @param supplier    a function that creates a new result container. It creates objects of type {@code <A>}.
     * @param accumulator an associative, non-interfering, stateless function for incorporating an additional element into a
     *                    result
     * @param <R>         The result of the collector.
     * @return A {@link CompletionRunner} that emits the collected result.
     */
    <R> CompletionRunner<R> collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator);

    /**
     * Collect the elements emitted by this publisher builder into a {@link List}
     * <p>
     * <img src="doc-files/toList.png" alt="toList marble diagram">
     *
     * @return A {@link CompletionRunner} that emits the list.
     */
    CompletionRunner<List<T>> toList();

    /**
     * Connect the outlet of the {@link Publisher} built by this builder to the given {@link Subscriber}.
     *
     * @param subscriber The subscriber to connect.
     * @return A {@link CompletionRunner} that completes when the stream completes.
     */
    CompletionRunner<Void> to(Subscriber<T> subscriber);

    /**
     * Connect the outlet of this publisher builder to the given {@link SubscriberBuilder} graph.
     *
     * @param subscriber The subscriber builder to connect.
     * @return A {@link CompletionRunner} that completes when the stream completes.
     */
    <R> CompletionRunner<R> to(SubscriberBuilder<T, R> subscriber);

    /**
     * Connect the outlet of the {@link Publisher} built by this builder to the given {@link ProcessorBuilder}.
     *
     * @param processor The processor to connect.
     * @return A {@link PublisherBuilder} that represents the passed in processors outlet.
     */
    <R> PublisherBuilder<R> via(ProcessorBuilder<T, R> processor);

    /**
     * Connect the outlet of this publisher builder to the given {@link Processor} graph.
     *
     * @param processor The processor builder to connect.
     * @return A {@link PublisherBuilder} that represents the passed in processor builders outlet.
     */
    <R> PublisherBuilder<R> via(Processor<T, R> processor);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action if this
     * stream conveys an error. The given consumer is called with the failure.
     * <p>
     * <img src="doc-files/onError.png" alt="onError marble diagram">
     *
     * @param errorHandler The function called with the failure.
     * @return A new processor builder that consumes elements of type <code>T</code> and emits the same elements. If the
     * stream conveys a failure, the given error handler is called.
     */
    PublisherBuilder<T> onError(Consumer<Throwable> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream. Additionally, in the case of failure, rather than
     * invoking {@link #onError(Consumer)}, it invokes the given method and emits the result as final event of the stream.
     * <p>
     * <img src="doc-files/onErrorResume.png" alt="onErrorResume marble diagram">
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its subscriber,
     * the stream (publisher) invokes its subscriber's <code>onError</code> method, and then terminate without invoking
     * any more of its subscriber's methods. This operator changes this behavior. If the current stream encounters an
     * error, instead of invoking its subscriber's <code>onError</code> method, it will instead emit the return value of
     * the passed function. This operator prevents errors from propagating or to supply fallback data should errors be
     * encountered.
     *
     * @param errorHandler the function returning the value that need to be emitting instead of the error.
     *                     The function must not return {@code null}
     * @return The new publisher
     */
    PublisherBuilder<T> onErrorResume(Function<Throwable, T> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream. Additionally, in the case of failure, rather than
     * invoking {@link #onError(Consumer)}, it invokes the given method and emits the returned {@link PublisherBuilder}
     * instead.
     * <p>
     * <img src="doc-files/onErrorResumeWith.png" alt="onErrorResumeWith marble diagram">
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its subscriber,
     * the stream (publisher) invokes its subscriber's <code>onError</code> method, and then terminate without invoking
     * any more of its subscriber's methods. This operator changes this behavior. If the current stream encounters an
     * error, instead of invoking its subscriber's <code>onError</code> method, it will instead relinquish control to the
     * {@link PublisherBuilder} returned from given function, which invoke the subscriber's <code>onNext</code> method if
     * it is able to do so. In such a case, because no publisher necessarily invokes <code>onError</code>, the subscriber
     * may never know that an error happened.
     *
     * @param errorHandler the function returning the stream that need to be emitting instead of the error.
     *                     The function must not return {@code null}
     * @return The new publisher
     */
    PublisherBuilder<T> onErrorResumeWith(Function<Throwable, PublisherBuilder<T>> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream. Additionally, in the case of failure, rather than
     * invoking {@link #onError(Consumer)}, it invokes the given method and emits the returned {@link PublisherBuilder}
     * instead.
     * <p>
     * <img src="doc-files/onErrorResumeWithRsPublisher.png" alt="onErrorResumeWithRsPublisher marble diagram">
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its subscriber,
     * the stream (publisher) invokes its subscriber's <code>onError</code> method, and then terminate without invoking
     * any more of its subscriber's methods. This operator changes this behavior. If the current stream encounters an
     * error, instead of invoking its subscriber's <code>onError</code> method, it will instead relinquish control to the
     * {@link PublisherBuilder} returned from given function, which invoke the subscriber's <code>onNext</code> method if
     * it is able to do so. In such a case, because no publisher necessarily invokes <code>onError</code>, the subscriber
     * may never know that an error happened.
     *
     * @param errorHandler the function returning the stream that need to be emitting instead of the error.
     *                     The function must not return {@code null}
     * @return The new publisher
     */
    <S> PublisherBuilder<S> onErrorResumeWithRsPublisher(Function<Throwable, Publisher<? extends S>> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action when this
     * stream completes or failed. The given action does not know if the stream failed or completed. If you need to
     * distinguish use {@link #onError(Consumer)} and {@link #onComplete(Runnable)}. In addition, the action is called if
     * the stream is cancelled downstream.
     * <p>
     * <img src="doc-files/onTerminate.png" alt="onTerminate marble diagram">
     *
     * @param action The function called when the stream completes or failed.
     * @return A new processor builder that consumes elements of type <code>T</code> and emits the same elements. The given
     * action is called when the stream completes or fails.
     */
    PublisherBuilder<T> onTerminate(Runnable action);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action when this
     * stream completes.
     * <p>
     * <img src="doc-files/onComplete.png" alt="onComplete marble diagram">
     *
     * @param action The function called when the stream completes.
     * @return A new processor builder that consumes elements of type <code>T</code> and emits the same elements. The given
     * action is called when the stream completes.
     */
    PublisherBuilder<T> onComplete(Runnable action);

    /**
     * Build this stream, using the first {@link ReactiveStreamsEngine} found by the {@link java.util.ServiceLoader}.
     *
     * @return A {@link Processor} that will run this stream.
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
