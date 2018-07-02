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

package org.eclipse.microprofile.reactive.streams.tck;

import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

public class FlatMapPublisherStageVerification extends AbstractStageVerification {
    FlatMapPublisherStageVerification(ReactiveStreamsTck.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void flatMapStageShouldMapElements() {
        assertEquals(await(ReactiveStreams.of(1, 2, 3)
            .flatMapPublisher(n -> ReactiveStreams.of(n, n, n).buildRs())
            .toList()
            .run(getEngine())), Arrays.asList(1, 1, 1, 2, 2, 2, 3, 3, 3));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapStageShouldPropagateRuntimeExceptions() {
        await(ReactiveStreams.of("foo")
            .flatMapPublisher(foo -> {
                throw new RuntimeException("failed");
            })
            .toList()
            .run(getEngine()));
    }

    @Test
    public void flatMapStageShouldOnlySubscribeToOnePublisherAtATime() throws Exception {
        AtomicInteger activePublishers = new AtomicInteger();

        // A publisher that publishes one element 100ms after being requested,
        // and then completes 100ms later. It also uses activePublishers to ensure
        // that it is the only publisher that is subscribed to at any one time.
        class ScheduledPublisher implements Publisher<Integer> {
            private final int id;

            private ScheduledPublisher(int id) {
                this.id = id;
            }

            private AtomicBoolean published = new AtomicBoolean(false);

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                assertEquals(activePublishers.incrementAndGet(), 1);
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        if (published.compareAndSet(false, true)) {
                            getExecutorService().schedule(() -> {
                                subscriber.onNext(id);
                                getExecutorService().schedule(() -> {
                                    activePublishers.decrementAndGet();
                                    subscriber.onComplete();
                                }, 100, TimeUnit.MILLISECONDS);
                            }, 100, TimeUnit.MILLISECONDS);
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }

        CompletionStage<List<Integer>> result = ReactiveStreams.of(1, 2, 3, 4, 5)
            .flatMapPublisher(ScheduledPublisher::new)
            .toList()
            .run(getEngine());

        assertEquals(result.toCompletableFuture().get(2, TimeUnit.SECONDS),
            Arrays.asList(1, 2, 3, 4, 5));
    }


    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Arrays.asList(new OuterProcessorVerification(), new InnerSubscriberVerification());
    }

    /**
     * Verifies the outer processor.
     */
    public class OuterProcessorVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return ReactiveStreams.<Integer>builder().flatMapPublisher(x -> ReactiveStreams.of(x).buildRs()).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return ReactiveStreams.<Integer>failed(new RuntimeException("failed"))
                .flatMapPublisher(x -> ReactiveStreams.of(x).buildRs()).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }

    /**
     * Verifies the inner subscriber passed to publishers produced by the mapper function.
     */
    public class InnerSubscriberVerification extends StageSubscriberWhiteboxVerification<Integer> {

        @Override
        public Subscriber<Integer> createSubscriber(WhiteboxSubscriberProbe<Integer> probe) {
            CompletableFuture<Subscriber<? super Integer>> subscriber = new CompletableFuture<>();
            ReactiveStreams.of(ReactiveStreams.<Integer>fromPublisher(subscriber::complete))
                .flatMapPublisher(PublisherBuilder::buildRs)
                .to(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        // We need to initially request an element to ensure that we get the publisher.
                        subscription.request(1);
                        probe.registerOnSubscribe(new SubscriberPuppet() {
                            @Override
                            public void triggerRequest(long elements) {
                                subscription.request(elements);
                            }

                            @Override
                            public void signalCancel() {
                                subscription.cancel();
                            }
                        });
                    }

                    @Override
                    public void onNext(Integer item) {
                        probe.registerOnNext(item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        probe.registerOnError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        probe.registerOnComplete();
                    }
                })
                .run(getEngine());

            return (Subscriber) await(subscriber);
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
