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

import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Operators for completing a stream.
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
 * @param <T> The type of the elements that the stream emits.
 */
public interface ConsumingOperators<T> {

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
    ProducesResult<Void> forEach(Consumer<? super T> action);

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
    ProducesResult<Void> ignore();

    /**
     * Cancels the stream as soon as it starts.
     * <p>
     * The returned {@link CompletionStage} will be immediately redeemed as soon as the stream starts.
     *
     * @return A new completion builder.
     */
    ProducesResult<Void> cancel();

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
    ProducesResult<T> reduce(T identity, BinaryOperator<T> accumulator);

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
    ProducesResult<Optional<T>> reduce(BinaryOperator<T> accumulator);

    /**
     * Find the first element emitted by the {@link Publisher}, and return it in a
     * {@link CompletionStage}.
     * <p>
     * <img src="doc-files/findFirst.png" alt="findFirst marble diagram">
     * <p>
     * If the stream is completed before a single element is emitted, then {@link Optional#empty()} will be emitted.
     *
     * @return A new completion builder.
     */
    ProducesResult<Optional<T>> findFirst();

    /**
     * Collect the elements emitted by this stream using the given {@link Collector}.
     * <p>
     * Since Reactive Streams are intrinsically sequential, only the accumulator of the collector will be used, the
     * combiner will not be used.
     *
     * @param collector The collector to collect the elements.
     * @param <R>       The result of the collector.
     * @param <A>       The accumulator type.
     * @return A new completion builder that emits the collected result.
     */
    <R, A> ProducesResult<R> collect(Collector<? super T, A, R> collector);

    /**
     * Collect the elements emitted by this stream using a {@link Collector} built from the given
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
     * @return A new completion builder that emits the collected result.
     */
    <R> ProducesResult<R> collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator);

    /**
     * Collect the elements emitted by this stream into a {@link List}.
     * <p>
     * <img src="doc-files/toList.png" alt="toList marble diagram">
     *
     * @return A new completion builder that emits the list.
     */
    ProducesResult<List<T>> toList();

}
