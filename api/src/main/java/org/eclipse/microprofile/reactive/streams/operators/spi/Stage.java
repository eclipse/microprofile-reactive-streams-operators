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

package org.eclipse.microprofile.reactive.streams.operators.spi;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

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
     * A map stage.
     * <p>
     * The given mapper function must be invoked on each element consumed, and the output of the function must be
     * emitted.
     * <p>
     * Any {@link RuntimeException} thrown by the function must be propagated downstream as an error, and upstream
     * must be cancelled. Any subsequent elements received from upstream before the cancellation signal is handled
     * must be dropped.
     */
    interface Map extends Stage {
        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        Function<?, ?> getMapper();
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
    interface Peek extends Stage {
        /**
         * The consumer function.
         *
         * @return The consumer function.
         */
        Consumer<?> getConsumer();
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
    interface Filter extends Stage {
        /**
         * The predicate.
         *
         * @return The predicate.
         */
        Predicate<?> getPredicate();
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
    interface DropWhile extends Stage {
        /**
         * The predicate.
         *
         * @return The predicate.
         */
        Predicate<?> getPredicate();
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
    interface Skip extends Stage {
        /**
         * The number of elements to skip.
         *
         * @return The number of elements to skip.
         */
        long getSkip();
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
    interface Limit extends Stage {
        /**
         * The limit.
         *
         * @return The limit.
         */
        long getLimit();
    }

    /**
     * A stage returning a stream consisting of the distinct elements (according to {@link Object#equals(Object)}) of this
     * stream.
     * <p>
     * Any {@link RuntimeException} thrown by the {@code equals} or {@code hashCode} methods of elements must be
     * propagated downstream as an error, and upstream must be cancelled.
     */
    interface Distinct extends Stage {
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
    interface TakeWhile extends Stage {
        /**
         * The predicate.
         *
         * @return The predicate.
         */
        Predicate<?> getPredicate();
    }

    /**
     * A publisher stage.
     * <p>
     * The given {@code publisher} must be subscribed to whatever subscriber is provided to this graph, via any
     * other subsequent stages.
     */
    interface PublisherStage extends Stage {
        /**
         * The publisher.
         *
         * @return The publisher.
         */
        Publisher<?> getRsPublisher();
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
    interface Of extends Stage {
        /**
         * The elements to emit.
         *
         * @return The elements to emit.
         */
        Iterable<?> getElements();
    }

    /**
     * A processor stage.
     * <p>
     * When built, must connect upstream of the graph to the inlet of this processor, and downstream to the outlet.
     */
    interface ProcessorStage extends Stage {
        /**
         * The processor.
         *
         * @return The processor.
         */
        Processor<?, ?> getRsProcessor();
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
    interface FindFirst extends Stage {
    }

    /**
     * A subscriber.
     * <p>
     * When built, the {@link CompletionStage} must emit <code>null</code> when the stream completes normally, or an
     * error if the stream terminates with an error.
     * <p>
     * Implementing this will typically require inserting a handler before the subscriber that listens for errors.
     */
    interface SubscriberStage extends Stage {
        /**
         * The subscriber.
         *
         * @return The subscriber.
         */
        Subscriber<?> getRsSubscriber();
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
    interface Collect extends Stage {
        /**
         * The collector.
         *
         * @return The collector.
         */
        Collector<?, ?, ?> getCollector();
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
    interface FlatMap extends Stage {
        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        Function<?, Graph> getMapper();
    }

    /**
     * A flat map stage that emits and flattens {@link CompletionStage}.
     * <p>
     * The flat map stage must execute the given mapper on each element, and concatenate the values redeemed by the
     * {@link CompletionStage}'s emitted by the mapper function into the resulting stream.
     * If the value is {@code null}, then no element must be emitted, and the stream should be failed with a
     * {@link NullPointerException}. 
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
    interface FlatMapCompletionStage extends Stage {
        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        Function<?, CompletionStage<?>> getMapper();
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
    interface FlatMapIterable extends Stage {
        /**
         * The mapper function.
         *
         * @return The mapper function.
         */
        Function<?, Iterable<?>> getMapper();
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
    interface OnError extends Stage {
        /**
         * The error handler.
         *
         * @return the error handler.
         */
        Consumer<Throwable> getConsumer();
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
    interface OnTerminate extends Stage {
        /**
         * The action to execute.
         *
         * @return the action to execute.
         */
        Runnable getAction();
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
    interface OnComplete extends Stage {
        /**
         * The action to execute.
         *
         * @return the action to execute.
         */
        Runnable getAction();
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
    interface OnErrorResume extends Stage {
        /**
         * The error handler.
         *
         * @return the error handler.
         */
        Function<Throwable, ?> getFunction();
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
     * relinquish control to the {@code PublisherBuilder} returned from
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
    interface OnErrorResumeWith extends Stage {
        /**
         * The error handler.
         *
         * @return the error handler.
         */
        Function<Throwable, Graph> getFunction();
    }

    /**
     * A failed publisher.
     * <p>
     * When built, this must produce a publisher that immediately fails the stream with the passed in error.
     */
    interface Failed extends Stage {
        /**
         * The error.
         *
         * @return the error.
         */
        Throwable getError();
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
    interface Concat extends Stage {
        /**
         * The first graph in the stream.
         *
         * @return The first graph.
         */
        Graph getFirst();

        /**
         * The second graph in the stream.
         *
         * @return The second graph.
         */
        Graph getSecond();
    }

    /**
     * A cancelling stage.
     * <p>
     * This stage immediately cancels upstream when it is run.
     * <p>
     * The {@link CompletionStage} produced by this stage must also be redeemed immediately with {@code null}.
     */
    interface Cancel extends Stage {
    }

    /**
     * A publisher representation of a {@link CompletionStage}.
     * <p>
     * This stage must emit the value produced by the {@code CompletionStage}, then immediately complete the stream.
     * If the value is {@code null}, then no element must be emitted, and the stream should be failed with a
     * {@link NullPointerException}. If the {@code CompletionStage} is redeemed with a failure, the stream must be
     * failed with that failure.
     * <p>
     * If the stream is cancelled by downstream before the {@code CompletionStage} has been redeemed, then this stage
     * must do nothing. It must not cancel the {@code CompletionStage} (since {@code CompletionStage} offers no API
     * for cancellation), and it must not emit any further signals when the {@code CompletionStage} is redeemed.
     */
    interface FromCompletionStage extends Stage {
        /**
         * Get the {@link CompletionStage}.
         *
         * @return The completion stage.
         */
        CompletionStage<?> getCompletionStage();
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
    interface FromCompletionStageNullable extends Stage {
        /**
         * Get the {@link CompletionStage}.
         *
         * @return The completion stage.
         */
        CompletionStage<?> getCompletionStage();
    }

    /**
     * A stage that couples a wrapped subscriber graph to a wrapped publisher graph.
     * <p>
     * The resulting stage sends all the elements received to the passed in subscriber, and emits all the elements
     * received from the passed in publisher.
     * <p>
     * In addition, the lifecycles of the subscriber and publisher are coupled, such that if one terminates or
     * receives a termination signal, the other will be terminated. Below is a table of what signals are emited when:
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
     */
    interface Coupled extends Stage {
        /**
         * Get the subscriber graph.
         *
         * @return The subscriber graph.
         */
        Graph getSubscriber();

        /**
         * Get the publisher graph.
         *
         * @return The publisher graph.
         */
        Graph getPublisher();
    }

}
