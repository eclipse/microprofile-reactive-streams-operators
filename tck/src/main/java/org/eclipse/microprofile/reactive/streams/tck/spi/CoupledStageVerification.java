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

package org.eclipse.microprofile.reactive.streams.tck.spi;

import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CoupledStageVerification extends AbstractStageVerification {

    CoupledStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void coupledStageShouldCancelAndCompleteUpstreamWhenDownstreamCancels() {
        CompletableFuture<Void> subscriberCompleted = new CompletableFuture<>();
        CompletableFuture<Void> upstreamCancelled = new CompletableFuture<>();

        idlePublisher()
            .onTerminate(() -> upstreamCancelled.complete(null))
            .via(
                rs.coupled(rs.builder().onComplete(() -> subscriberCompleted.complete(null)).ignore(),
                    idlePublisher())
            ).cancel()
            .run();

        await(subscriberCompleted);
        await(upstreamCancelled);
    }

    @Test
    public void coupledStageShouldCancelAndCompleteUpstreamWhenPublisherCompletes() {
        CompletableFuture<Void> subscriberCompleted = new CompletableFuture<>();
        CompletableFuture<Void> upstreamCancelled = new CompletableFuture<>();

        idlePublisher()
            .onTerminate(() -> upstreamCancelled.complete(null))
            .via(
                rs.coupled(rs.builder().onComplete(() -> subscriberCompleted.complete(null)).ignore(),
                    rs.empty())
            ).ignore()
            .run();

        await(subscriberCompleted);
        await(upstreamCancelled);
    }

    @Test
    public void coupledStageShouldCancelAndCompleteUpstreamWhenPublisherFails() {
        CompletableFuture<Throwable> subscriberFailed = new CompletableFuture<>();
        CompletableFuture<Void> upstreamCancelled = new CompletableFuture<>();

        idlePublisher()
            .onTerminate(() -> upstreamCancelled.complete(null))
            .via(
                rs.coupled(rs.builder().onError(subscriberFailed::complete).ignore(),
                    rs.failed(new QuietRuntimeException("failed")))
            ).ignore()
            .run();

        assertTrue(await(subscriberFailed) instanceof QuietRuntimeException);
        await(upstreamCancelled);
    }

    @Test
    public void coupledStageShouldCancelAndCompleteDownstreamWhenUpstreamCompletes() {
        CompletableFuture<Void> publisherCancelled = new CompletableFuture<>();
        CompletableFuture<Void> downstreamCompleted = new CompletableFuture<>();

        rs.empty()
            .via(
                rs.coupled(rs.builder().ignore(),
                    idlePublisher().onTerminate(() -> publisherCancelled.complete(null)))
            ).onComplete(() -> downstreamCompleted.complete(null))
            .ignore()
            .run();

        await(publisherCancelled);
        await(downstreamCompleted);
    }

    @Test
    public void coupledStageShouldCancelAndFailDownstreamWhenUpstreamFails() {
        CompletableFuture<Void> publisherCancelled = new CompletableFuture<>();
        CompletableFuture<Throwable> downstreamFailed = new CompletableFuture<>();

        rs.failed(new QuietRuntimeException("failed"))
            .via(
                rs.coupled(rs.builder().ignore(),
                    idlePublisher().onTerminate(() -> publisherCancelled.complete(null)))
            ).onError(downstreamFailed::complete)
            .ignore()
            .run();

        await(publisherCancelled);
        assertTrue(await(downstreamFailed) instanceof QuietRuntimeException);
    }

    @Test
    public void coupledStageShouldCancelAndCompleteDownstreamWhenSubscriberCancels() {
        CompletableFuture<Void> publisherCancelled = new CompletableFuture<>();
        CompletableFuture<Void> downstreamCompleted = new CompletableFuture<>();

        idlePublisher()
            .via(
                rs.coupled(rs.builder().cancel(),
                    idlePublisher().onTerminate(() -> publisherCancelled.complete(null)))
            ).onComplete(() -> downstreamCompleted.complete(null))
            .ignore()
            .run();

        await(publisherCancelled);
        await(downstreamCompleted);
    }

    @Test
    public void coupledStageShouldBeResuable() {
        ProcessorBuilder<Object, Integer> coupled = rs.coupled(rs.builder().ignore(), rs.of(1, 2, 3));
        assertEquals(await(idlePublisher().via(coupled).toList().run()), Arrays.asList(1, 2, 3));
        assertEquals(await(idlePublisher().via(coupled).toList().run()), Arrays.asList(1, 2, 3));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Arrays.asList(
            new PublisherVerification(),
            new SubscriberVerification(),
            new ProcessorVerification()
        );
    }

    public class PublisherVerification extends StagePublisherVerification<Long> {
        @Override
        public Publisher<Long> createPublisher(long elements) {
            return rs.coupled(rs.builder().ignore(), rs.fromIterable(
                () -> LongStream.rangeClosed(1, elements).boxed().iterator()
            )).buildRs(getEngine());
        }

        @Override
        public Publisher<Long> createFailedPublisher() {
            return rs.coupled(rs.builder().ignore(), rs.<Long>failed(new QuietRuntimeException("failed"))).buildRs(getEngine());
        }
    }

    public class SubscriberVerification extends StageSubscriberWhiteboxVerification<Integer> {

        @Override
        public Subscriber<Integer> createSubscriber(WhiteboxSubscriberProbe<Integer> probe) {
            return rs.coupled(rs.fromSubscriber(new Subscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription s) {
                    probe.registerOnSubscribe(new SubscriberPuppet() {
                        @Override
                        public void triggerRequest(long elements) {
                            s.request(elements);
                        }

                        @Override
                        public void signalCancel() {
                            s.cancel();
                        }
                    });
                }

                @Override
                public void onNext(Integer integer) {
                    probe.registerOnNext(integer);
                }

                @Override
                public void onError(Throwable t) {
                    probe.registerOnError(t);
                }

                @Override
                public void onComplete() {
                    probe.registerOnComplete();
                }
            }), idlePublisher()).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }

    public class ProcessorVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            Processor<Integer, Integer> processor = rs.<Integer>builder().buildRs(getEngine());
            return rs.coupled(processor, processor).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }

        @Override
        public long maxElementsFromPublisher() {
            // This must be zero because the coupled nature of the stream means that completion signals can overtake elements.
            return 0;
        }
    }

}
