/*******************************************************************************
 * Copyright (c) 2018, 2022 Contributors to the Eclipse Foundation
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

import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

/**
 * Operators for handling errors in streams.
 * <p>
 * The documentation for each operator uses marble diagrams to visualize how the operator functions. Each element
 * flowing in and out of the stream is represented as a coloured marble that has a value, with the operator applying
 * some transformation or some side effect, termination and error signals potentially being passed, and for operators
 * that subscribe to the stream, an output value being redeemed at the end.
 * <p>
 * Below is an example diagram labelling all the parts of the stream.
 * <p>
 * <img src="doc-files/example.png" alt="Example marble diagram">
 *
 * @param <T>
 *            The type of the elements that the stream emits.
 * @see ReactiveStreams
 */
public interface ErrorHandlingOperators<T> {

    /**
     * Returns a stream containing all the elements from this stream. Additionally, in the case of failure, it invokes
     * the given function and emits the result as final event of the stream.
     * <p>
     * <img src="doc-files/onErrorResume.png" alt="onErrorResume marble diagram">
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its subscriber,
     * the stream invokes its subscriber's <code>onError</code> method, and then terminates without invoking any more of
     * its subscriber's methods. This operator changes this behavior. If the current stream encounters an error, instead
     * of invoking its subscriber's <code>onError</code> method, it will instead emit the return value of the passed
     * function. This operator prevents errors from propagating or to supply fallback data should errors be encountered.
     *
     * @param errorHandler
     *            the function returning the value that needs to be emitting instead of the error. The function must not
     *            return {@code null}.
     * @return The new stream builder.
     */
    ErrorHandlingOperators<T> onErrorResume(Function<Throwable, ? extends T> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream. Additionally, in the case of failure, it invokes
     * the given function and emits the elements from the returned {@link PublisherBuilder} instead.
     * <p>
     * <img src="doc-files/onErrorResumeWith.png" alt="onErrorResumeWith marble diagram">
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its subscriber,
     * the stream invokes its subscriber's <code>onError</code> method, and then terminates without invoking any more of
     * its subscriber's methods. This operator changes this behavior. If the current stream encounters an error, instead
     * of invoking its subscriber's <code>onError</code> method, it will instead relinquish control to the
     * {@link PublisherBuilder} returned from the given function, which invokes the subscriber's <code>onNext</code>
     * method if it is able to do so. The subscriber's original {@link Subscription} is used to control the flow of
     * elements both before and after any error occurring. In such a case, because no publisher necessarily invokes
     * <code>onError</code> on the stream's subscriber, it may never know that an error happened.
     *
     * @param errorHandler
     *            the function returning the stream that needs to be emitting instead of the error. The function must
     *            not return {@code null}.
     * @return The new stream builder.
     */
    ErrorHandlingOperators<T> onErrorResumeWith(
            Function<Throwable, ? extends PublisherBuilder<? extends T>> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream. Additionally, in the case of failure, it invokes
     * the given function and emits the elements from the returned {@link Publisher} instead.
     * <p>
     * <img src="doc-files/onErrorResumeWithRsPublisher.png" alt="onErrorResumeWithRsPublisher marble diagram">
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its subscriber,
     * the stream invokes its subscriber's <code>onError</code> method, and then terminates without invoking any more of
     * its subscriber's methods. This operator changes this behavior. If the current stream encounters an error, instead
     * of invoking its subscriber's <code>onError</code> method, the subscriber will be fed from the {@link Publisher}
     * returned from the given function, and the subscriber's <code>onNext</code> method is called as the returned
     * Publisher publishes. The subscriber's original {@link Subscription} is used to control the flow of both the
     * original and the onError Publishers' elements. In such a case, because no publisher necessarily invokes
     * <code>onError</code>, the subscriber may never know that an error happened.
     *
     * @param errorHandler
     *            the function returning the stream that need to be emitting instead of the error. The function must not
     *            return {@code null}.
     * @return The new stream builder.
     */
    ErrorHandlingOperators<T> onErrorResumeWithRsPublisher(
            Function<Throwable, ? extends Publisher<? extends T>> errorHandler);
}
