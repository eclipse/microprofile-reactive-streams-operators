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

import java.util.function.Consumer;

/**
 * Operations for peeking at elements and signals in the stream, without impacting the stream itself.
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
public interface PeekingOperators<T> {

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action on each
     * element.
     * <p>
     * <img src="doc-files/peek.png" alt="peek marbles diagram">
     *
     * @param consumer The function called for every element.
     * @return A new stream builder that consumes elements of type <code>T</code> and emits the same elements. In between,
     * the given function is called for each element.
     */
    PeekingOperators<T> peek(Consumer<? super T> consumer);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action if this
     * stream conveys an error. The given consumer is called with the failure.
     * <p>
     * <img src="doc-files/onError.png" alt="onError marble diagram">
     *
     * @param errorHandler The function called with the failure.
     * @return A new stream that consumes elements of type <code>T</code> and emits the same elements. If the
     * stream conveys a failure, the given error handler is called.
     */
    PeekingOperators<T> onError(Consumer<Throwable> errorHandler);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action when this
     * stream completes or failed. The given action does not know if the stream failed or completed. If you need to
     * distinguish use {@link #onError(Consumer)} and {@link #onComplete(Runnable)}. In addition, the action is called if
     * the stream is cancelled downstream.
     * <p>
     * <img src="doc-files/onTerminate.png" alt="onTerminate marble diagram">
     *
     * @param action The function called when the stream completes or failed.
     * @return A new stream builder that consumes elements of type <code>T</code> and emits the same elements. The given
     * action is called when the stream completes or fails.
     */
    PeekingOperators<T> onTerminate(Runnable action);

    /**
     * Returns a stream containing all the elements from this stream, additionally performing the provided action when this
     * stream completes.
     * <p>
     * <img src="doc-files/onComplete.png" alt="onComplete marble diagram">
     *
     * @param action The function called when the stream completes.
     * @return A new stream builder that consumes elements of type <code>T</code> and emits the same elements. The given
     * action is called when the stream completes.
     */
    PeekingOperators<T> onComplete(Runnable action);
}
