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

package org.eclipse.microprofile.reactive.streams.operators.tck.spi;

import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertEquals;

public class LimitStageVerification extends AbstractStageVerification {

    LimitStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void limitStageShouldLimitTheOutputElements() {
        assertEquals(await(infiniteStream()
            .limit(3)
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3));
    }

    @Test
    public void limitStageShouldAllowLimitingToZero() {
        assertEquals(await(infiniteStream()
            .limit(0)
            .toList()
            .run(getEngine())), Collections.emptyList());
    }

    @Test
    public void limitStageToZeroShouldCompleteStreamEvenWhenNoElementsAreReceived() {
        assertEquals(await(rs.fromPublisher(subscriber ->
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                }

                @Override
                public void cancel() {
                }
            })
        ).limit(0)
            .toList()
            .run(getEngine())), Collections.emptyList());
    }

    @Test
    public void limitStageShouldCancelUpStreamWhenDone() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        infiniteStream()
            .onTerminate(() -> cancelled.complete(null))
            .limit(1)
            .toList()
            .run(getEngine());
        await(cancelled);
    }

    @Test
    public void limitStageShouldIgnoreSubsequentErrorsWhenDone() {
        assertEquals(await(
            infiniteStream()
                .flatMap(i -> {
                    if (i == 4) {
                        return rs.failed(new RuntimeException("failed"));
                    } else {
                        return rs.of(i);
                    }
                })
                .limit(3)
                .toList()
                .run(getEngine())
        ), Arrays.asList(1, 2, 3));
    }

    @Test
    public void limitStageShouldPropagateCancellation() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        await(
            infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .peek(i -> {
                    if (i == 100) {
                        cancelled.completeExceptionally(new RuntimeException("Was not cancelled"));
                    }
                })
                .limit(100)
                .limit(3)
                .toList()
                .run(getEngine())
        );
        await(cancelled);
    }

    @Test
    public void limitStageBuilderShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> limit = rs.<Integer>builder().limit(3);
        assertEquals(await(infiniteStream().via(limit).toList().run(getEngine())), Arrays.asList(1, 2, 3));
        assertEquals(await(infiniteStream().map(i -> i + 1).via(limit).toList().run(getEngine())), Arrays.asList(2, 3, 4));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new ProcessorVerification());
    }

    public class ProcessorVerification extends StageProcessorVerification<Integer> {
        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder()
                .limit(Long.MAX_VALUE)
                .buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new RuntimeException("failed"))
                .limit(Long.MAX_VALUE)
                .buildRs(getEngine());
        }
    }
}
