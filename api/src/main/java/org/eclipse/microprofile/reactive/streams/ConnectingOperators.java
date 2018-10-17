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

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Operators for connecting different graphs together.
 *
 * @param <T> The type of the elements that the stream emits.
 */
public interface ConnectingOperators<T> {

    /**
     * Connect the outlet of the {@link Publisher} built by this builder to the given {@link Subscriber}.
     *
     * @param subscriber The subscriber to connect.
     * @return A completion builder that completes when the stream completes.
     */
    ProducesResult<Void> to(Subscriber<? super T> subscriber);

    /**
     * Connect the outlet of this stream to the given {@link SubscriberBuilder} graph.
     *
     * @param subscriber The subscriber builder to connect.
     * @return A completion builder that completes when the stream completes.
     */
    <R> ProducesResult<R> to(SubscriberBuilder<? super T, ? extends R> subscriber);

    /**
     * Connect the outlet of the {@link Publisher} built by this builder to the given {@link ProcessorBuilder}.
     *
     * @param processor The processor to connect.
     * @return A stream builder that represents the passed in processors outlet.
     */
    <R> ConnectingOperators<R> via(ProcessorBuilder<? super T, ? extends R> processor);

    /**
     * Connect the outlet of this stream to the given {@link Processor} graph.
     *
     * @param processor The processor builder to connect.
     * @return A stream builder that represents the passed in processor builders outlet.
     */
    <R> ConnectingOperators<R> via(Processor<? super T, ? extends R> processor);
}
