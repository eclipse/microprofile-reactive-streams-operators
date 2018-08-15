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

package org.eclipse.microprofile.reactive.streams.spi;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * A stage of a Reactive Streams graph.
 * <p>
 * A Reactive Streams engine will walk a graph of stages to produce {@link Publisher},
 * {@link Subscriber} and {@link Processor} instances that handle the stream
 * according to the sequence of stages.
 */
public interface Stage {

    /**
     * Whether this stage has an inlet - ie, when built, will it implement the {@link Subscriber}
     * interface?
     *
     * @return True if this stage has an inlet.
     */
    default boolean hasInlet() {
        return false;
    }

    /**
     * Whether this stage has an outlet - ie, when built, will it implement the {@link Publisher}
     * interface?
     *
     * @return True if this stage has an outlet.
     */
    default boolean hasOutlet() {
        return false;
    }

    /**
     * Convenience interface for inlet stages.
     */
    interface Inlet extends Stage {
        @Override
        default boolean hasInlet() {
            return true;
        }
    }

    /**
     * Convenience interface for outlet stages.
     */
    interface Outlet extends Stage {
        @Override
        default boolean hasOutlet() {
            return true;
        }
    }

    /**
     * A map stage.
     * <p>
     * The given mapper function must be invoked on each element consumed, and the output of the function must be
     * emitted.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     */
    final class Map implements Inlet, Outlet {
        private final Function<?, ?> mapper;

        public Map(Function<?, ?> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        public Function<?, ?> getMapper() {
            return mapper;
        }
    }

    /**
     * A stage returning a stream containing all the elements from this stream,
     * additionally performing the provided action on each element.
     * <p>
     * The given consumer function must be invoked on each element consumed.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     */
    final class Peek implements Inlet, Outlet {
        private final Consumer<?> consumer;

        public Peek(Consumer<?> consumer) {
            this.consumer = Objects.requireNonNull(consumer, "Consumer must not be null");
        }

        /**
         * The consumer function.
         *
         * @return The consumer function.
         */
        public Consumer<?> getConsumer() {
            return consumer;
        }
    }

    /**
     * A filter stage.
     * <p>
     * The given predicate must be invoked on each element consumed. If it returns {@code true}, the element must be
     * emitted, otherwise, it must be dropped.
     * <p>
     * Any {@link RuntimeException} thrown by the predicate must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     */
    final class Filter implements Inlet, Outlet {
        private final Predicate<?> predicate;

        public Filter(Predicate<?> predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Predicate must not be null");
        }

        /**
         * The predicate.
         *
         * @return The predicate.
         */
        public Predicate<?> getPredicate() {
            return predicate;
        }
    }

    /**
     * A drop while stage.
     * <p>
     * The given predicate must be invoked on each element consumed, until it returns {@code true}. Each element that it
     * returns {@code true} for must be dropped, and once it returns {@code false}, that element that it returned
     * {@code false} for, and all subsequent elements, must be emitted. The predicate must not be invoked after it returns
     * {@code false} the first time.
     * <p>
     * If upstream terminates for any reason before the predicate returns {@code false}, downstream must also be
     * terminated.
     * <p>
     * Any {@link RuntimeException} thrown by the predicate must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     */
    final class DropWhile implements Inlet, Outlet {
        private final Predicate<?> predicate;

        public DropWhile(Predicate<?> predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Predicate must not be null");
        }

        /**
         * The predicate.
         *
         * @return The predicate.
         */
        public Predicate<?> getPredicate() {
            return predicate;
        }
    }

    /**
     * A skip stage.
     * <p>
     * The first {@code skip} elements must be skipped, after that all elements must be emitted.
     * <p>
     * If the number of elements to skip is zero, this stage has no effect.
     * <p>
     * If less than {@code skip} elements are emitted before termination, then the termination must be propagated
     * downstream as normal.
     */
    final class Skip implements Inlet, Outlet {
        private final long skip;

        /**
         * Create a skip stage.
         *
         * @param skip The number of elements to skip.
         * @throws IllegalArgumentException If the number of elements is less than zero.
         */
        public Skip(long skip) {
            if (skip < 0) {
                throw new IllegalArgumentException("Cannot skip less than zero elements");
            }
            this.skip = skip;
        }

        /**
         * The number of elements to skip.
         *
         * @return The number of elements to skip.
         */
        public long getSkip() {
            return skip;
        }
    }

    /**
     * A limit stage.
     * <p>
     * Only {@code limit} elements may be emitted, once that many elements are emitted, downstream must be completed,
     * and upstream must be cancelled. Any subsequent elements received from upstream before the cancellation signal
     * is handled must be dropped.
     * <p>
     * If less than {@code limit} elements are received before termination, then the termination must be propagated
     * downstream as normal.
     */
    final class Limit implements Inlet, Outlet {
        private final long limit;

        /**
         * Create a limit stage.
         *
         * @param limit The number of elements to limit the stream to.
         * @throws IllegalArgumentException If the number of elements is less than zero.
         */
        public Limit(long limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("Cannot limit a stream to less than zero elements.");
            }
            this.limit = limit;
        }

        /**
         * The limit.
         *
         * @return The limit.
         */
        public long getLimit() {
            return limit;
        }
    }

    /**
     * A stage returning a stream consisting of the distinct elements (according to {@link Object#equals(Object)}) of this
     * stream.
     * <p>
     * Any {@link RuntimeException} thrown by the {@code equals} or {@code hashCode} methods of elements must be
     * propagated downstream as an error, and upstream must be cancelled.
     */
    final class Distinct implements Inlet, Outlet {

        /**
         * The singleton instance of the distinct stage. Implementations may use reference equality to test for this
         * stage.
         */
        public static final Distinct INSTANCE = new Distinct();

        private Distinct() {
            // Avoid direct instantiation.
        }
    }


    /**
     * A take while stage.
     * <p>
     * The given {@code predicate} must be invoked on each element consumed. While the predicate returns {@code true},
     * the element must be emitted, when the predicate returns {@code false}, the element must not be emitted, 
     * downstream must be completed and upstream must be cancelled.
     * <p>
     * The {@code predicate} must not be invoked again once it returns {@code false} for the first time. Any elements
     * supplied by upstream before it handles the cancellation signal must be dropped.
     * <p>
     * Any {@link RuntimeException} thrown by the predicate must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     */
    final class TakeWhile implements Inlet, Outlet {
        private final Predicate<?> predicate;

        public TakeWhile(Predicate<?> predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Predicate must not be null");
        }

        /**
         * The predicate.
         *
         * @return The predicate.
         */
        public Predicate<?> getPredicate() {
            return predicate;
        }
    }

    /**
     * A publisher stage.
     * <p>
     * The given {@code publisher} must be subscribed to whatever subscriber is provided to this graph, via any
     * other subsequent stages.
     */
    final class PublisherStage implements Outlet {
        private final Publisher<?> publisher;

        public PublisherStage(Publisher<?> publisher) {
            this.publisher = Objects.requireNonNull(publisher, "Publisher must not be null");
        }

        /**
         * The publisher.
         *
         * @return The publisher.
         */
        public Publisher<?> getRsPublisher() {
            return publisher;
        }
    }

    /**
     * A publisher of zero to many values.
     * <p>
     * When built, must produce a publisher that produces all the values (until cancelled) emitted by this iterables
     * iterator, followed by completion of the stream.
     * <p>
     * Any exceptions thrown by the iterator must be propagated downstream, or by the invocation of the
     * {@code iterator} method, must be propagated downstream.
     */
    final class Of implements Outlet {
        /**
         * The singleton instance of the stage used in the case of an empty list. Implementations may use reference
         * equality to test for this stage.
         */
        public static final Of EMPTY = new Of(Collections.emptyList());

        private final Iterable<?> elements;

        public Of(Iterable<?> elements) {
            this.elements = Objects.requireNonNull(elements, "Iterable must not be null");
        }

        /**
         * The elements to emit.
         *
         * @return The elements to emit.
         */
        public Iterable<?> getElements() {
            return elements;
        }
    }

    /**
     * A processor stage.
     * <p>
     * When built, must connect upstream of the graph to the inlet of this processor, and downstream to the outlet.
     */
    final class ProcessorStage implements Inlet, Outlet {
        private final Processor<?, ?> processor;

        public ProcessorStage(Processor<?, ?> processor) {
            this.processor = Objects.requireNonNull(processor, "Processor must not be null");
        }

        /**
         * The processor.
         *
         * @return The processor.
         */
        public Processor<?, ?> getRsProcessor() {
            return processor;
        }
    }

    /**
     * A subscriber stage that emits the first element encountered.
     * <p>
     * When built, the {@link CompletionStage} must emit an {@link java.util.Optional} of the first element
     * encountered. If no element is emitted before completion of the stream, it must emit an empty optional. Once
     * the element has been emitted, the stream must be cancelled if not already complete.
     * <p>
     * If an error is emitted before the first element is encountered, the stream must redeem the completion stage with
     * that error.
     */
    final class FindFirst implements Inlet {

        /**
         * The singleton instance of the find first stage. Implementations may use reference equality to test for this
         * stage.
         */
        public static final FindFirst INSTANCE = new FindFirst();

        private FindFirst() {
        }
    }

    /**
     * A subscriber.
     * <p>
     * When built, the {@link CompletionStage} must emit <code>null</code> when the stream completes normally, or an
     * error if the stream terminates with an error.
     * <p>
     * Implementing this will typically require inserting a handler before the subscriber that listens for errors.
     */
    final class SubscriberStage implements Inlet {
        private final Subscriber<?> subscriber;

        public SubscriberStage(Subscriber<?> subscriber) {
            this.subscriber = Objects.requireNonNull(subscriber, "Subscriber must not be null");
        }

        /**
         * The subscriber.
         *
         * @return The subscriber.
         */
        public Subscriber<?> getRsSubscriber() {
            return subscriber;
        }
    }

    /**
     * A collect stage.
     * <p>
     * This must use the collectors supplier to create an accumulated value, and then the accumulator BiConsumer must
     * be used to accumulate the received elements in the value. Finally, the returned {@link CompletionStage} must be
     * redeemed by value returned by the finisher function applied to the accumulated value when the stream terminates
     * normally, or must be redeemed with an error if the stream terminates with an error.
     * <p>
     * If the collector throws an exception, the upstream must be cancelled, and the {@link CompletionStage} must be
     * redeemed with that error.
     */
    final class Collect implements Inlet {
        private final Collector<?, ?, ?> collector;

        public Collect(Collector<?, ?, ?> collector) {
            this.collector = Objects.requireNonNull(collector, "Collector must not be null");
        }

        /**
         * The collector.
         *
         * @return The collector.
         */
        public Collector<?, ?, ?> getCollector() {
            return collector;
        }
    }

    /**
     * A flat map stage.
     * <p>
     * The flat map stage must execute the given mapper on each element, and concatenate the publishers emitted by
     * the mapper function into the resulting stream.
     * <p>
     * The graph emitted by the mapper function is guaranteed to have an outlet but no inlet.
     * <p>
     * The engine must ensure only one publisher emitted by the mapper function is running at a time.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     * <p>
     * If downstream cancels, then both the currently running inner publisher produced by the {@code mapper}, if one
     * is currently running, and the outer upstream, must be cancelled.
     * <p>
     * If the inner publisher terminates with an error, the outer publisher must be cancelled, and the error must be
     * propagated downstream. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     * <p>
     * If the outer publisher terminates with an error, then implementations may cancel the inner publisher, if one is
     * currently running, immediately, or they may wait for the inner publisher to complete, before propagating the
     * error downstream. For the purpose of failing fast, so that network failures can be detected and handled, it
     * is recommended, but not required, that the inner publisher is cancelled as soon as possible.
     */
    final class FlatMap implements Inlet, Outlet {
        private final Function<?, Graph> mapper;

        public FlatMap(Function<?, Graph> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        public Function<?, Graph> getMapper() {
            return mapper;
        }
    }

    /**
     * A flat map stage that emits and flattens {@link CompletionStage}.
     * <p>
     * The flat map stage must execute the given mapper on each element, and concatenate the values redeemed by the
     * {@link CompletionStage}'s emitted by the mapper function into the resulting stream.
     * <p>
     * The engine must ensure only one mapper function is executed at a time, with the next mapper function not
     * executing until the {@link CompletionStage} returned by the previous mapper function has been redeemed.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     * <p>
     * If downstream cancels, then upstream must be cancelled, and if there is a currently executing
     * {@code CompletionStage}, its result must be ignored.
     * <p>
     * If a {@code CompletionStage} is redeemed with an error, upstream must be cancelled, and the error must be
     * propagated downstream. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     * <p>
     * If upstream terminates with an error, then implementations may decide to ignore the result of any currently
     * executing {@code CompletionStage}, and propagate the error immediately, or they may wait for the
     * {@code CompletionStage} to be redeemed, emit its result, and then propagate the error downstream. For the
     * purpose of failing fast, so that network failures can be detected and handled, it is recommended, but not
     * required, that the failure is propagated downstream as soon as possible, and the result of the
     * {@code CompletionStage} ignored.
     */
    final class FlatMapCompletionStage implements Inlet, Outlet {
        private final Function<?, CompletionStage<?>> mapper;

        public FlatMapCompletionStage(Function<?, CompletionStage<?>> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        public Function<?, CompletionStage<?>> getMapper() {
            return mapper;
        }
    }

    /**
     * A flat map stage that emits and fattens {@link Iterable}.
     * <p>
     * The flat map stage must execute the given mapper on each element, and concatenate the iterables emitted by
     * the mapper function into the resulting stream.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     * <p>
     * If downstream cancels, then upstream must be cancelled, and if there is a current iterator being published, it
     * must no longer be consumed. The iterator must not be run through to completion in such a circumstance.
     * <p>
     * If a currently consumed iterator throws an exception from either its {@code next} or {@code hasNext} methods,
     * upstream must be cancelled, and the error must be propagated downstream. Any subsequent elements received from
     * upstream before the cancellation signal is handled must be dropped.
     * <p>
     * If upstream terminates with an error, then implementations may decide to stop consuming any currently running
     * iterator, and propagate the error immediately, or they may run the iterator through to completion, before
     * propagating the error. For the purpose of failing fast, so that network failures can be detected and handled,
     * it is recommended, but not required, that the failure is propagated downstream as soon as possible, and the
     * iterator not be run through to completion.
     */
    final class FlatMapIterable implements Inlet, Outlet {
        private final Function<?, Iterable<?>> mapper;

        public FlatMapIterable(Function<?, Iterable<?>> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        public Function<?, Iterable<?>> getMapper() {
            return mapper;
        }
    }

    /**
     * A stage returning a stream containing all the elements from this stream,
     * additionally performing the provided action if this stream conveys an error.
     * <p>
     * The given consumer function must be invoked with the conveyed failure.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, replacing the
     * exception that the consumer was handling.
     */
    final class OnError implements Inlet, Outlet {
        private final Consumer<Throwable> consumer;


        public OnError(Consumer<Throwable> consumer) {
            this.consumer = Objects.requireNonNull(consumer, "Consumer must not be null");
        }

        /**
         * The error handler.
         *
         * @return the error handler.
         */
        public Consumer<Throwable> getConsumer() {
            return consumer;
        }
    }

    /**
     * A stage returning a stream containing all the elements from this stream, additionally performing the provided
     * action if this stream terminates with an error, completes, or is cancelled by downstream.
     * <p>
     * The given action cannot determine in which state the stream is (error, completed or cancelled). Use
     * {@link OnError} and {@link OnComplete} if you need to distinguish between these cases.
     * <p>
     * The action must only be invoked once. If both upstream completes, and downstream cancels, at the same time,
     * only one of those signals may trigger the invocation of action.
     * <p>
     * If this action is invoked as the result of an upstream completion or error, any {@link RuntimeException} thrown
     * by the function must be propagated downstream as an error, replacing the exception that the consumer was
     * handling. If the action is invoked as the result of downstream cancellation, then any exceptions thrown by the
     * function must be ignored, and cancellation must be propagated upstream.
     */
    final class OnTerminate implements Inlet, Outlet {
        private final Runnable action;

        public OnTerminate(Runnable runnable) {
            this.action = Objects.requireNonNull(runnable, "Action must not be null");
        }

        /**
         * The action to execute.
         *
         * @return the action to execute.
         */
        public Runnable getAction() {
            return action;
        }
    }

    /**
     * A stage returning a stream containing all the elements from this stream, additionally performing the provided
     * action when this stream completes.
     * <p>
     * The given action must be called when the stream completes successfully. Use {@link OnError} to handle failures,
     * and {@link OnTerminate} if the action needs to be called for completion, error or cancellation.
     * <p>
     * Any {@link RuntimeException} thrown by this function must be propagated downstream as an error.
     */
    final class OnComplete implements Inlet, Outlet {
        private final Runnable action;

        public OnComplete(Runnable runnable) {
            this.action = Objects.requireNonNull(runnable, "Action must not be null");
        }

        /**
         * The action to execute.
         *
         * @return the action to execute.
         */
        public Runnable getAction() {
            return action;
        }
    }

    /**
     * A stage to handle errors from upstream. It builds a stream containing all the elements from upstream.
     * Additionally, in the case of failure, rather than propagating the error downstream, it invokes the given
     * function method emits the result as the final event of the stream, terminating the stream after that.
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its
     * subscriber, the stream (publisher) invokes its subscriber's <code>onError</code> method, and then terminates
     * without invoking any more of its subscriber's methods. This operator changes this behavior. If the current
     * stream encounters an error, instead of invoking its subscriber's <code>onError</code> method, it will instead
     * emit the return value of the passed function. This operator prevents errors from propagating and allows
     * supplying fallback data should errors be encountered.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, replacing the
     * exception that the function was handling.
     */
    final class OnErrorResume implements Inlet, Outlet {
        private final Function<Throwable, ?> function;


        public OnErrorResume(Function<Throwable, ?>  function) {
            this.function = Objects.requireNonNull(function, "Resume function must not be null");
        }

        /**
         * The error handler.
         *
         * @return  the error handler.
         */
        public Function<Throwable, ?> getFunction() {
            return function;
        }
    }

    /**
     * A stage to handle errors from upstream. It builds a stream containing all the elements from upstream.
     * Additionally, in the case of failure, rather than propagating the error downstream, it invokes the given
     * function and switches control to the returned stream.
     * <p>
     * By default, when a stream encounters an error that prevents it from emitting the expected item to its
     * subscriber, the stream (publisher) invokes its subscriber's <code>onError</code> method, and then terminates
     * without invoking any more of its subscriber's methods. This operator changes this behavior. If the current
     * stream encounters an error, instead of invoking its subscriber's <code>onError</code> method, it will instead
     * relinquish control to the {@link org.eclipse.microprofile.reactive.streams.PublisherBuilder} returned from
     * given function. In such a case, because no publisher necessarily invokes <code>onError</code>, the subscriber
     * may never know that an error happened.
     * <p>
     * Any elements emitted by the returned publisher must be sent downstream, in addition to its completion and error
     * signals. If the returned publisher completes with an error, that error must not result in the recovery function
     * on this stage being invoked again.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, replacing the
     * exception that the function was handling.
     */
    final class OnErrorResumeWith implements Inlet, Outlet {
        private final Function<Throwable, Graph> function;


        public OnErrorResumeWith(Function<Throwable, Graph> function) {
            this.function = Objects.requireNonNull(function, "Resume with function must be empty");
        }

        /**
         * The error handler.
         *
         * @return  the error handler.
         */
        public Function<Throwable, Graph> getFunction() {
            return function;
        }
    }

    /**
     * A failed publisher.
     * <p>
     * When built, this must produce a publisher that immediately fails the stream with the passed in error.
     */
    final class Failed implements Outlet {
        private final Throwable error;

        public Failed(Throwable error) {
            this.error = Objects.requireNonNull(error, "Exception must not be null");
        }

        public Throwable getError() {
            return error;
        }
    }

    /**
     * Concatenate the given graphs together.
     * <p>
     * Each graph must have an outlet and no inlet.
     * <p>
     * The resulting publisher produced by the concat stage must emit all the elements from the first graph,
     * and once that graph emits a completion signal, it must then subscribe to and emit all the elements from
     * the second. If an error is emitted by the either graph, the error must be emitted from the resulting stream.
     * <p>
     * If processing terminates early while the first graph is still emitting, either due to that graph emitting an
     * error, or due to a cancellation signal from downstream, then the second graph must be subscribed to and cancelled.
     * This is to ensure that any hot publishers holding onto resources that may be backing the graphs are cleaned up.
     */
    final class Concat implements Outlet {
        private final Graph first;
        private final Graph second;

        public Concat(Graph first, Graph second) {
            this.first = validate(first);
            this.second = validate(second);
        }

        private static Graph validate(Graph graph) {
            if (graph.hasInlet() || !graph.hasOutlet()) {
                throw new IllegalArgumentException(
                    "Concatenated graphs must have an outlet, but no inlet, but this graph does not: " + graph);
            }
            return graph;
        }

        public Graph getFirst() {
            return first;
        }

        public Graph getSecond() {
            return second;
        }
    }

    /**
     * A cancelling stage.
     * <p>
     * This stage immediately cancels upstream when it is started.
     * <p>
     * The {@link CompletionStage} produced by this stage must also be redeemed immediately with {@code null}.
     */
    final class Cancel implements Inlet {

        /**
         * The singleton instance of the cancel stage. Implementations may use reference equality to test for this
         * stage.
         */
        public static final Cancel INSTANCE = new Cancel();

        private Cancel() {
        }
    }

    /**
     * A publisher representation of a {@link CompletionStage}.
     * <p>
     * This stage must emit the value produced by the {@code CompletionStage}, then immediately complete the stream.
     * If the value is {@code null}, then no element must be emitted, and the stream must be immediately completed.
     * If the {@code CompletionStage} is redeemed with a failure, the stream must be failed with that failure.
     * <p>
     * If the stream is cancelled by downstream before the {@code CompletionStage} has been redeemed, then this stage
     * must do nothing. It must not cancel the {@code CompletionStage} (since {@code CompletionStage} offers no API
     * for cancellation), and it must not emit any further signals when the {@code CompletionStage} is redeemed.
     */
    final class FromCompletionStage implements Outlet {
        private final CompletionStage<?> completionStage;

        public FromCompletionStage(CompletionStage<?> completionStage) {
            this.completionStage = Objects.requireNonNull(completionStage, "CompletionStage must not be null");
        }

        public CompletionStage<?> getCompletionStage() {
            return completionStage;
        }
    }
}
