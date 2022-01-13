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

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A subscriber that redeems a completion stage when it completes.
 * <p>
 * The result is provided through a {@link CompletionStage}, which is redeemed when the subscriber receives a completion
 * or error signal, or otherwise cancels the stream.
 * <p>
 * The best way to instantiate one of these is using the {@link CompletionSubscriber#of} factory method.
 *
 * @param <T>
 *            The type of the elements that the subscriber consumes.
 * @param <R>
 *            The type of the result that the subscriber emits.
 */
public interface CompletionSubscriber<T, R> extends Subscriber<T> {

    /**
     * Get the completion stage.
     * <p>
     * This should be redeemed by the subscriber either when it cancels, or when it receives an
     * {@link Subscriber#onComplete} signal or an {@link Subscriber#onError(Throwable)} signal. Generally, the redeemed
     * value or error should be the result of consuming the stream.
     *
     * @return The completion stage.
     */
    CompletionStage<R> getCompletion();

    /**
     * Create a {@link CompletionSubscriber} by combining the given subscriber and completion stage. The objects passed
     * to this method should not be associated with more than one stream instance.
     * <p>
     * The returned {@code CompletionSubscriber} will delegate calls from the {@code Subscriber} interface to
     * {@code subscriber} and will return {@code completion} from {@link #getCompletion()}.
     * <p>
     * It is the callers responsibility to ensure that {@code completion} is completed with the correct value when
     * {@code subscriber} terminates.
     *
     * @param subscriber
     *            The subscriber.
     * @param completion
     *            The completion stage.
     * @return A completion subscriber.
     */
    static <T, R> CompletionSubscriber<T, R> of(Subscriber<T> subscriber, CompletionStage<R> completion) {

        // Use a named local class rather than anonymous class for better debugging/stack traces etc
        class DefaultCompletionSubscriber implements CompletionSubscriber<T, R> {

            private final Subscriber<T> subscriber;
            private final CompletionStage<R> completion;

            private DefaultCompletionSubscriber(Subscriber<T> subscriber, CompletionStage<R> completion) {
                this.subscriber = Objects.requireNonNull(subscriber, "Subscriber must not be null");
                this.completion = Objects.requireNonNull(completion, "CompletionStage must not be null");
            }

            @Override
            public CompletionStage<R> getCompletion() {
                return completion;
            }

            @Override
            public void onSubscribe(Subscription subscription) {
                subscriber.onSubscribe(subscription);
            }

            @Override
            public void onNext(T t) {
                subscriber.onNext(t);
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }

            @Override
            public String toString() {
                return "CompletionSubscriber(" + subscriber + ", " + completion + ")";
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                DefaultCompletionSubscriber that = (DefaultCompletionSubscriber) o;
                return Objects.equals(subscriber, that.subscriber) &&
                        Objects.equals(completion, that.completion);
            }

            @Override
            public int hashCode() {
                return Objects.hash(subscriber, completion);
            }
        }

        return new DefaultCompletionSubscriber(subscriber, completion);
    }

}
