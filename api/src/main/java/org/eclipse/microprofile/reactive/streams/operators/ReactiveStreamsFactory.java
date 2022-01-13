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

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Factory interface for providing the implementation of the static factory methods in {@link ReactiveStreams}.
 */
public interface ReactiveStreamsFactory {
    /**
     * Create a {@link PublisherBuilder} from the given {@link Publisher}.
     *
     * @param publisher
     *            The publisher to wrap.
     * @param <T>
     *            The type of the elements that the publisher produces.
     * @return A publisher builder that wraps the given publisher.
     */
    <T> PublisherBuilder<T> fromPublisher(Publisher<? extends T> publisher);

    /**
     * Create a {@link PublisherBuilder} that emits a single element.
     * <p>
     * <img src="doc-files/of-single.png" alt="of marble diagram">
     *
     * @param t
     *            The element to emit.
     * @param <T>
     *            The type of the element.
     * @return A publisher builder that will emit the element.
     */
    <T> PublisherBuilder<T> of(T t);

    /**
     * Create a {@link PublisherBuilder} that emits the given elements.
     * <p>
     * <img src="doc-files/of-many.png" alt="of marble diagram">
     *
     * @param ts
     *            The elements to emit.
     * @param <T>
     *            The type of the elements.
     * @return A publisher builder that will emit the elements.
     */
    <T> PublisherBuilder<T> of(T... ts);

    /**
     * Create an empty {@link PublisherBuilder}.
     * <p>
     * <img src="doc-files/empty.png" alt="empty marble diagram">
     *
     * @param <T>
     *            The type of the publisher builder.
     * @return A publisher builder that will just emit a completion signal.
     */
    <T> PublisherBuilder<T> empty();

    /**
     * Create a {@link PublisherBuilder} that will emit a single element if <code>t</code> is not null, otherwise will
     * be empty.
     * <p>
     * <img src="doc-files/ofNullable.png" alt="ofNullable marble diagram">
     *
     * @param t
     *            The element to emit, <code>null</code> if to element should be emitted.
     * @param <T>
     *            The type of the element.
     * @return A publisher builder that optionally emits a single element.
     */
    <T> PublisherBuilder<T> ofNullable(T t);

    /**
     * Create a {@link PublisherBuilder} that will emits the elements produced by the passed in {@link Iterable}.
     * <p>
     * <img src="doc-files/fromIterable.png" alt="fromIterable marble diagram">
     *
     * @param ts
     *            The elements to emit.
     * @param <T>
     *            The type of the elements.
     * @return A publisher builder that emits the elements of the iterable.
     */
    <T> PublisherBuilder<T> fromIterable(Iterable<? extends T> ts);

    /**
     * Create a failed {@link PublisherBuilder}.
     * <p>
     * <img src="doc-files/failed.png" alt="failed marble diagram">
     * <p>
     * This publisher will just emit an error.
     *
     * @param t
     *            The error te emit.
     * @param <T>
     *            The type of the publisher builder.
     * @return A publisher builder that completes the stream with an error.
     */
    <T> PublisherBuilder<T> failed(Throwable t);

    /**
     * Create a {@link ProcessorBuilder}. This builder will start as an identity processor.
     * <p>
     * <img src="doc-files/identity.png" alt="identity marble diagram">
     *
     * @param <T>
     *            The type of elements that the processor consumes and emits.
     * @return The identity processor builder.
     */
    <T> ProcessorBuilder<T, T> builder();

    /**
     * Create a {@link ProcessorBuilder} from the given {@link Processor}.
     *
     * @param processor
     *            The processor to be wrapped.
     * @param <T>
     *            The type of the elements that the processor consumes.
     * @param <R>
     *            The type of the elements that the processor emits.
     * @return A processor builder that wraps the processor.
     */
    <T, R> ProcessorBuilder<T, R> fromProcessor(Processor<? super T, ? extends R> processor);

    /**
     * Create a {@link SubscriberBuilder} from the given {@link Subscriber}. The subscriber can only be used to create a
     * single subscriber builder.
     *
     * @param subscriber
     *            The subscriber to be wrapped.
     * @param <T>
     *            The type of elements that the subscriber consumes.
     * @return A subscriber builder that wraps the subscriber.
     */
    <T> SubscriberBuilder<T, Void> fromSubscriber(Subscriber<? super T> subscriber);

    /**
     * Creates an infinite stream produced by the iterative application of the function {@code f} to an initial element
     * {@code seed} consisting of {@code seed}, {@code f(seed)}, {@code f(f(seed))}, etc.
     * <p>
     * <img src="doc-files/iterate.png" alt="iterate marble diagram">
     *
     * @param seed
     *            The initial element.
     * @param f
     *            A function applied to the previous element to produce the next element.
     * @param <T>
     *            The type of stream elements.
     * @return A publisher builder.
     */
    <T> PublisherBuilder<T> iterate(T seed, UnaryOperator<T> f);

    /**
     * Creates an infinite stream that emits elements supplied by the supplier {@code s}.
     * <p>
     * <img src="doc-files/generate.png" alt="generate marble diagram">
     *
     * @param s
     *            The supplier.
     * @param <T>
     *            The type of stream elements.
     * @return A publisher builder.
     */
    <T> PublisherBuilder<T> generate(Supplier<? extends T> s);

    /**
     * Concatenates two publishers.
     * <p>
     * <img src="doc-files/concat.png" alt="concat marble diagram">
     * <p>
     * The resulting stream will be produced by subscribing to the first publisher, and emitting the elements it emits,
     * until it emits a completion signal, at which point the second publisher will be subscribed to, and its elements
     * will be emitted.
     * <p>
     * If the first publisher completes with an error signal, then the second publisher will be subscribed to but
     * immediately cancelled, none of its elements will be emitted. This ensures that hot publishers are cleaned up. If
     * downstream emits a cancellation signal before the first publisher finishes, it will be passed to both publishers.
     *
     * @param a
     *            The first publisher.
     * @param b
     *            The second publisher.
     * @param <T>
     *            The type of stream elements.
     * @return A publisher builder.
     */
    <T> PublisherBuilder<T> concat(PublisherBuilder<? extends T> a, PublisherBuilder<? extends T> b);

    /**
     * Creates a publisher from a {@link CompletionStage}.
     * <p>
     * <img src="doc-files/fromCompletionStage.png" alt="fromCompletionStage marble diagram">
     * <p>
     * When the {@code CompletionStage} is redeemed, the publisher will emit the redeemed element, and then signal
     * completion. If the completion stage is redeemed with {@code null}, the stream will be failed with a
     * {@link NullPointerException}.
     * <p>
     * If the {@code CompletionStage} is completed with a failure, this failure will be propagated through the stream.
     *
     * @param completionStage
     *            The {@code CompletionStage} to create the publisher from.
     * @param <T>
     *            The type of the {@code CompletionStage} value.
     * @return A {@code PublisherBuilder} representation of this {@code CompletionStage}.
     */
    <T> PublisherBuilder<T> fromCompletionStage(CompletionStage<? extends T> completionStage);

    /**
     * Creates a publisher from a {@link CompletionStage}.
     * <p>
     * <img src="doc-files/fromCompletionStageNullable.png" alt="fromCompletionStage marble diagram">
     * <p>
     * When the {@code CompletionStage} is redeemed, the publisher will emit the redeemed element, and then signal
     * completion. If the completion stage is redeemed with {@code null}, the stream will be immediately completed with
     * no element, ie, it will be an empty stream.
     * <p>
     * If the {@code CompletionStage} is completed with a failure, this failure will be propagated through the stream.
     *
     * @param completionStage
     *            The {@code CompletionStage} to create the publisher from.
     * @param <T>
     *            The type of the {@code CompletionStage} value.
     * @return A {@code PublisherBuilder} representation of this {@code CompletionStage}.
     */
    <T> PublisherBuilder<T> fromCompletionStageNullable(CompletionStage<? extends T> completionStage);

    /**
     * Creates a {@link ProcessorBuilder} by coupling a {@link SubscriberBuilder} to a {@link PublisherBuilder}.
     * <p>
     * <img src="doc-files/coupled.png" alt="coupled marble diagram">
     * <p>
     * The resulting processor sends all the elements received to the passed in subscriber, and emits all the elements
     * received from the passed in publisher.
     * <p>
     * In addition, the lifecycles of the subscriber and publisher are coupled, such that if one terminates or receives
     * a termination signal, the other will be terminated. Below is a table of what signals are emited when:
     * <p>
     * <table border="1">
     * <caption>Lifecycle signal propagation</caption>
     * <tr>
     * <th>Returned ProcessorBuilder inlet</th>
     * <th>Passed in SubscriberBuilder</th>
     * <th>Passed in PublisherBuilder</th>
     * <th>Returned ProcessorBuilder outlet</th>
     * </tr>
     * <tr>
     * <td>Cause: complete from upstream</td>
     * <td>Effect: complete</td>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * </tr>
     * <tr>
     * <td>Cause: error from upstream</td>
     * <td>Effect: error</td>
     * <td>Effect: cancel</td>
     * <td>Effect: error</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Cause: cancels</td>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * <td>Cause: completes</td>
     * <td>Effect: complete</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Effect: error</td>
     * <td>Cause: errors</td>
     * <td>Effect: error</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * <td>Effect: cancel</td>
     * <td>Cause: cancel from downstream</td>
     * </tr>
     * </table>
     *
     * @param subscriber
     *            The subscriber builder to wrap.
     * @param publisher
     *            The publisher builder to wrap.
     * @param <T>
     *            The type of elements received.
     * @param <R>
     *            The type of elements emitted.
     * @return The coupled processor builder.
     */
    <T, R> ProcessorBuilder<T, R> coupled(SubscriberBuilder<? super T, ?> subscriber,
            PublisherBuilder<? extends R> publisher);

    /**
     * Creates a {@link ProcessorBuilder} by coupling a {@link Subscriber} to a {@link Publisher}.
     * <p>
     * <img src="doc-files/coupled.png" alt="coupled marble diagram">
     * <p>
     * The resulting processor sends all the elements received to the passed in subscriber, and emits all the elements
     * received from the passed in publisher.
     * <p>
     * In addition, the lifecycles of the subscriber and publisher are coupled, such that if one terminates or receives
     * a termination signal, the other will be terminated. Below is a table of what signals are emited when:
     * <p>
     * <table border="1">
     * <caption>Lifecycle signal propagation</caption>
     * <tr>
     * <th>Returned ProcessorBuilder inlet</th>
     * <th>Passed in SubscriberBuilder</th>
     * <th>Passed in PublisherBuilder</th>
     * <th>Returned ProcessorBuilder outlet</th>
     * </tr>
     * <tr>
     * <td>Cause: complete from upstream</td>
     * <td>Effect: complete</td>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * </tr>
     * <tr>
     * <td>Cause: error from upstream</td>
     * <td>Effect: error</td>
     * <td>Effect: cancel</td>
     * <td>Effect: error</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Cause: cancels</td>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * <td>Cause: completes</td>
     * <td>Effect: complete</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Effect: error</td>
     * <td>Cause: errors</td>
     * <td>Effect: error</td>
     * </tr>
     * <tr>
     * <td>Effect: cancel</td>
     * <td>Effect: complete</td>
     * <td>Effect: cancel</td>
     * <td>Cause: cancel from downstream</td>
     * </tr>
     * </table>
     *
     * @param subscriber
     *            The subscriber builder to wrap.
     * @param publisher
     *            The publisher builder to wrap.
     * @param <T>
     *            The type of elements received.
     * @param <R>
     *            The type of elements emitted.
     * @return The coupled processor builder.
     * @see #coupled(SubscriberBuilder, PublisherBuilder)
     */
    <T, R> ProcessorBuilder<T, R> coupled(Subscriber<? super T> subscriber,
            Publisher<? extends R> publisher);

}
