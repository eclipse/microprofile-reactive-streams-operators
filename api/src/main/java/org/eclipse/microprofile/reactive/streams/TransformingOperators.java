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

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

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
 * @param <T> The type of the elements that this stream emits.
 */
public interface TransformingOperators<T> {

    /**
     * Map the elements emitted by this stream using the {@code mapper} function.
     * <p>
     * <img src="doc-files/map.png" alt="map marbles diagram">
     *
     * @param mapper The function to use to map the elements.
     * @param <R>    The type of elements that the {@code mapper} function emits.
     * @return A new stream builder that emits the mapped elements.
     */
    <R> TransformingOperators<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Map the elements to publishers, and flatten so that the elements emitted by publishers produced by the
     * {@code mapper} function are emitted from this stream.
     * <p>
     * <img src="doc-files/flatMap.png" alt="flatMap marbles diagram">
     * <p>
     * This method operates on one publisher at a time. The result is a concatenation of elements emitted from all the
     * publishers produced by the mapper function.
     * <p>
     * Unlike {@link #flatMapRsPublisher(Function)}}, the mapper function returns a 
     * {@link org.eclipse.microprofile.reactive.streams} type instead of an 
     * {@link org.reactivestreams} type.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new stream.
     * @return A new stream builder.
     */
    <S> TransformingOperators<S> flatMap(Function<? super T, ? extends PublisherBuilder<? extends S>> mapper);

    /**
     * Map the elements to publishers, and flatten so that the elements emitted by publishers produced by the
     * {@code mapper} function are emitted from this stream.
     * <p>
     * <img src="doc-files/flatMapRsPublisher.png" alt="flatMapRsPublisher marble diagram">
     * <p>
     * This method operates on one publisher at a time. The result is a concatenation of elements emitted from all the
     * publishers produced by the mapper function.
     * <p>
     * Unlike {@link #flatMap(Function)}, the mapper function returns a {@link org.eclipse.microprofile.reactive.streams} 
     * builder instead of an {@link org.reactivestreams} type.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new stream.
     * @return A new stream builder.
     */
    <S> TransformingOperators<S> flatMapRsPublisher(Function<? super T, ? extends Publisher<? extends S>> mapper);

    /**
     * Map the elements to {@link CompletionStage}, and flatten so that the elements the values redeemed by each
     * {@link CompletionStage} are emitted from this stream.
     * <p>
     * <img src="doc-files/flatMapCompletionStage.png" alt="flatMapCompletionStage marble diagram">
     * <p>
     * This method only works with one element at a time. When an element is received, the {@code mapper} function is
     * executed, and the next element is not consumed or passed to the {@code mapper} function until the previous
     * {@link CompletionStage} is redeemed. Hence this method also guarantees that ordering of the stream is maintained.
     *
     * @param mapper The mapper function.
     * @param <S>    The type of the elements emitted from the new stream.
     * @return A new stream builder.
     */
    <S> TransformingOperators<S> flatMapCompletionStage(Function<? super T, ? extends CompletionStage<? extends S>> mapper);

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
     * @param <S>    The type of the elements emitted from the new stream.
     * @return A new stream builder.
     */
    <S> TransformingOperators<S> flatMapIterable(Function<? super T, ? extends Iterable<? extends S>> mapper);
}
