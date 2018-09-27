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

package org.eclipse.microprofile.reactive.streams.core;

import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
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
 * Implementations of stages.
 */
final class Stages {

    private Stages() {
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

    final static class Map implements Stage.Map, Inlet, Outlet {
        private final Function<?, ?> mapper;

        Map(Function<?, ?> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        public Function<?, ?> getMapper() {
            return mapper;
        }
    }

    final static class Peek implements Stage.Peek, Inlet, Outlet {
        private final Consumer<?> consumer;

        Peek(Consumer<?> consumer) {
            this.consumer = Objects.requireNonNull(consumer, "Consumer must not be null");
        }

        public Consumer<?> getConsumer() {
            return consumer;
        }
    }

    final static class Filter implements Stage.Filter, Inlet, Outlet {
        private final Predicate<?> predicate;

        Filter(Predicate<?> predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Predicate must not be null");
        }

        public Predicate<?> getPredicate() {
            return predicate;
        }
    }

    final static class DropWhile implements Stage.DropWhile, Inlet, Outlet {
        private final Predicate<?> predicate;

        DropWhile(Predicate<?> predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Predicate must not be null");
        }

        public Predicate<?> getPredicate() {
            return predicate;
        }
    }

    final static class Skip implements Stage.Skip, Inlet, Outlet {
        private final long skip;

        Skip(long skip) {
            if (skip < 0) {
                throw new IllegalArgumentException("Cannot skip less than zero elements");
            }
            this.skip = skip;
        }

        public long getSkip() {
            return skip;
        }
    }

    final static class Limit implements Stage.Limit, Inlet, Outlet {
        private final long limit;

        Limit(long limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("Cannot limit a stream to less than zero elements.");
            }
            this.limit = limit;
        }

        public long getLimit() {
            return limit;
        }
    }

    final static class Distinct implements Stage.Distinct, Inlet, Outlet {

        static final Distinct INSTANCE = new Stages.Distinct();

        private Distinct() {
            // Avoid direct instantiation.
        }
    }


    final static class TakeWhile implements Stage.TakeWhile, Inlet, Outlet {
        private final Predicate<?> predicate;

        TakeWhile(Predicate<?> predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Predicate must not be null");
        }

        public Predicate<?> getPredicate() {
            return predicate;
        }
    }

    final static class PublisherStage implements Stage.PublisherStage, Outlet {
        private final Publisher<?> publisher;

        PublisherStage(Publisher<?> publisher) {
            this.publisher = Objects.requireNonNull(publisher, "Publisher must not be null");
        }

        public Publisher<?> getRsPublisher() {
            return publisher;
        }
    }

    final static class Of implements Stage.Of, Outlet {

        static final Of EMPTY = new Stages.Of(Collections.emptyList());

        private final Iterable<?> elements;

        Of(Iterable<?> elements) {
            this.elements = Objects.requireNonNull(elements, "Iterable must not be null");
        }

        public Iterable<?> getElements() {
            return elements;
        }
    }

    final static class ProcessorStage implements Stage.ProcessorStage, Inlet, Outlet {
        private final Processor<?, ?> processor;

        ProcessorStage(Processor<?, ?> processor) {
            this.processor = Objects.requireNonNull(processor, "Processor must not be null");
        }

        public Processor<?, ?> getRsProcessor() {
            return processor;
        }
    }

    final static class FindFirst implements Stage.FindFirst, Inlet {

        static final FindFirst INSTANCE = new Stages.FindFirst();

        private FindFirst() {
        }
    }

    final static class SubscriberStage implements Stage.SubscriberStage, Inlet {
        private final Subscriber<?> subscriber;

        SubscriberStage(Subscriber<?> subscriber) {
            this.subscriber = Objects.requireNonNull(subscriber, "Subscriber must not be null");
        }

        public Subscriber<?> getRsSubscriber() {
            return subscriber;
        }
    }

    final static class Collect implements Stage.Collect, Inlet {
        private final Collector<?, ?, ?> collector;

        Collect(Collector<?, ?, ?> collector) {
            this.collector = Objects.requireNonNull(collector, "Collector must not be null");
        }

        public Collector<?, ?, ?> getCollector() {
            return collector;
        }
    }

    final static class FlatMap implements Stage.FlatMap, Inlet, Outlet {
        private final Function<?, Graph> mapper;

        FlatMap(Function<?, Graph> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        public Function<?, Graph> getMapper() {
            return mapper;
        }
    }

    final static class FlatMapCompletionStage implements Stage.FlatMapCompletionStage, Inlet, Outlet {
        private final Function<?, CompletionStage<?>> mapper;

        FlatMapCompletionStage(Function<?, CompletionStage<?>> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        public Function<?, CompletionStage<?>> getMapper() {
            return mapper;
        }
    }

    final static class FlatMapIterable implements Stage.FlatMapIterable, Inlet, Outlet {
        private final Function<?, Iterable<?>> mapper;

        FlatMapIterable(Function<?, Iterable<?>> mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper function must not be null");
        }

        public Function<?, Iterable<?>> getMapper() {
            return mapper;
        }
    }

    final static class OnError implements Stage.OnError, Inlet, Outlet {
        private final Consumer<Throwable> consumer;

        OnError(Consumer<Throwable> consumer) {
            this.consumer = Objects.requireNonNull(consumer, "Consumer must not be null");
        }

        public Consumer<Throwable> getConsumer() {
            return consumer;
        }
    }

    final static class OnTerminate implements Stage.OnTerminate, Inlet, Outlet {
        private final Runnable action;

        OnTerminate(Runnable runnable) {
            this.action = Objects.requireNonNull(runnable, "Action must not be null");
        }

        public Runnable getAction() {
            return action;
        }
    }

    final static class OnComplete implements Stage.OnComplete, Inlet, Outlet {
        private final Runnable action;

        OnComplete(Runnable runnable) {
            this.action = Objects.requireNonNull(runnable, "Action must not be null");
        }

        public Runnable getAction() {
            return action;
        }
    }

    final static class OnErrorResume implements Stage.OnErrorResume, Inlet, Outlet {
        private final Function<Throwable, ?> function;

        OnErrorResume(Function<Throwable, ?>  function) {
            this.function = Objects.requireNonNull(function, "Resume function must not be null");
        }

        public Function<Throwable, ?> getFunction() {
            return function;
        }
    }

    final static class OnErrorResumeWith implements Stage.OnErrorResumeWith, Inlet, Outlet {
        private final Function<Throwable, Graph> function;

        OnErrorResumeWith(Function<Throwable, Graph> function) {
            this.function = Objects.requireNonNull(function, "Resume with function must be empty");
        }

        public Function<Throwable, Graph> getFunction() {
            return function;
        }
    }

    final static class Failed implements Stage.Failed, Outlet {
        private final Throwable error;

        Failed(Throwable error) {
            this.error = Objects.requireNonNull(error, "Exception must not be null");
        }

        public Throwable getError() {
            return error;
        }
    }

    final static class Concat implements Stage.Concat, Outlet {
        private final Graph first;
        private final Graph second;

        Concat(Graph first, Graph second) {
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

    final static class Cancel implements Stage.Cancel, Inlet {

        static final Cancel INSTANCE = new Stages.Cancel();

        private Cancel() {
        }
    }

    final static class FromCompletionStage implements Stage.FromCompletionStage, Outlet {
        private final CompletionStage<?> completionStage;

        FromCompletionStage(CompletionStage<?> completionStage) {
            this.completionStage = Objects.requireNonNull(completionStage, "CompletionStage must not be null");
        }

        @Override
        public CompletionStage<?> getCompletionStage() {
            return completionStage;
        }
    }

    final static class FromCompletionStageNullable implements Stage.FromCompletionStageNullable, Outlet {
        private final CompletionStage<?> completionStage;

        FromCompletionStageNullable(CompletionStage<?> completionStage) {
            this.completionStage = Objects.requireNonNull(completionStage, "CompletionStage must not be null");
        }

        @Override
        public CompletionStage<?> getCompletionStage() {
            return completionStage;
        }
    }

}
