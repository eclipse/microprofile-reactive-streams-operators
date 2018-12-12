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

package org.eclipse.microprofile.reactive.streams.operators;

import java.util.function.Predicate;

/**
 * Operations for transforming a stream.
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
public interface FilteringOperators<T> {

    /**
     * Filter elements emitted by this publisher using the given {@link Predicate}.
     * <p>
     * Any elements that return {@code true} when passed to the {@link Predicate} will be emitted, all other
     * elements will be dropped.
     * <p>
     * <img src="doc-files/filter.png" alt="filter marbles diagram">
     *
     * @param predicate The predicate to apply to each element.
     * @return A new stream builder.
     */
    FilteringOperators<T> filter(Predicate<? super T> predicate);

    /**
     * Creates a stream consisting of the distinct elements (according to {@link Object#equals(Object)}) of this stream.
     * <p>
     * <img src="doc-files/distinct.png" alt="distinct marbles diagram">
     *
     * @return A new stream builder emitting the distinct elements from this stream.
     */
    FilteringOperators<T> distinct();

    /**
     * Truncate this stream, ensuring the stream is no longer than {@code maxSize} elements in length.
     * <p>
     * <img src="doc-files/limit.png" alt="limit marble diagram">
     * <p>
     * If {@code maxSize} is reached, the stream will be completed, and upstream will be cancelled. Completion of the
     * stream will occur immediately when the element that satisfies the {@code maxSize} is received.
     *
     * @param maxSize The maximum size of the returned stream.
     * @return A new stream builder.
     * @throws IllegalArgumentException If {@code maxSize} is less than zero.
     */
    FilteringOperators<T> limit(long maxSize);

    /**
     * Discard the first {@code n} of this stream. If this stream contains fewer than {@code n} elements, this stream will
     * effectively be an empty stream.
     * <p>
     * <img src="doc-files/skip.png" alt="skip marble diagram">
     *
     * @param n The number of elements to discard.
     * @return A new stream builder.
     * @throws IllegalArgumentException If {@code n} is less than zero.
     */
    FilteringOperators<T> skip(long n);

    /**
     * Take the longest prefix of elements from this stream that satisfy the given {@code predicate}.
     * <p>
     * <img src="doc-files/takeWhile.png" alt="takeWhile marble diagram">
     * <p>
     * When the {@code predicate} returns false, the stream will be completed, and upstream will be cancelled.
     *
     * @param predicate The predicate.
     * @return A new stream builder.
     */
    FilteringOperators<T> takeWhile(Predicate<? super T> predicate);

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
     * @return A new stream builder.
     */
    FilteringOperators<T> dropWhile(Predicate<? super T> predicate);
}
