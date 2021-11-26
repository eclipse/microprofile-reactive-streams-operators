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

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Operators for connecting different graphs together.
 *
 * @param <T>
 *            The type of the elements that the stream emits.
 */
public interface ConnectingOperators<T> {

    /**
     * Connect the outlet of the {@link Publisher} built by this builder to the given {@link Subscriber}. The Reactive
     * Streams specification states that a subscriber should cancel any new stream subscription it receives if it
     * already has an active subscription. The returned result of this method is a stream that creates a subscription
     * for the subscriber passed in, so the resulting stream should only be run once. For the same reason, the
     * subscriber passed in should not have any active subscriptions and should not be used in more than one call to
     * this method.
     *
     * @param subscriber
     *            The subscriber to connect.
     * @return A completion builder that completes when the stream completes.
     */
    ProducesResult<Void> to(Subscriber<? super T> subscriber);

    /**
     * Connect the outlet of this stream to the given {@link SubscriberBuilder} graph. The Reactive Streams
     * specification states that a subscriber should cancel any new stream subscription it receives if it already has an
     * active subscription. For this reason, a subscriber builder, particularly any that represents a graph that
     * includes a user supplied {@link Subscriber} or {@link Processor} stage, should not be used in the creation of
     * more than one stream instance.
     *
     * @param subscriberBuilder
     *            The subscriber builder to connect.
     * @return A completion builder that completes when the stream completes.
     */
    <R> ProducesResult<R> to(SubscriberBuilder<? super T, ? extends R> subscriberBuilder);

    /**
     * Connect the outlet of the {@link Publisher} built by this builder to the given {@link ProcessorBuilder}. The
     * Reactive Streams specification states that a subscribing processor should cancel any new stream subscription it
     * receives if it already has an active subscription. For this reason, a processor builder, particularly any that
     * represents a graph that includes a user supplied {@link Processor} stage, should not be used in the creation of
     * more than one stream instance.
     *
     * @param processorBuilder
     *            The processor builder to connect.
     * @return A stream builder that represents the passed in processor's outlet.
     */
    <R> ConnectingOperators<R> via(ProcessorBuilder<? super T, ? extends R> processorBuilder);

    /**
     * Connect the outlet of this stream to the given {@link Processor}. The Reactive Streams specification states that
     * a subscribing processor should cancel any new stream subscription it receives if it already has an active
     * subscription. The returned result of this method is a stream that creates a subscription for the processor passed
     * in, so the resulting stream should only be run once. For the same reason, the processor passed in should not have
     * any active subscriptions and should not be used in more than one call to this method.
     *
     * 
     * @param processor
     *            The processor builder to connect.
     * @return A stream builder that represents the passed in processor builder's outlet.
     */
    <R> ConnectingOperators<R> via(Processor<? super T, ? extends R> processor);
}
