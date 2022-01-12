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

package org.eclipse.microprofile.reactive.streams.operators.tck.spi;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.reactivestreams.Subscriber;
import org.testng.annotations.Test;

/**
 * Verification for the collect stage.
 */
public class CollectStageVerification extends AbstractStageVerification {

    CollectStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void toListStageShouldReturnAList() {
        assertEquals(await(rs.of(1, 2, 3)
                .toList().run(getEngine())), Arrays.asList(1, 2, 3));
    }

    @Test
    public void toListStageShouldReturnEmpty() {
        assertEquals(await(rs.of()
                .toList().run(getEngine())), Collections.emptyList());
    }

    @Test
    public void collectShouldAccumulateResult() {
        assertEquals(await(rs.of(1, 2, 3)
                .collect(
                        () -> new AtomicInteger(0),
                        AtomicInteger::addAndGet)
                .run(getEngine())).get(), 6);
    }

    @Test
    public void collectShouldSupportEmptyStreams() {
        assertEquals(await(rs.<Integer>empty()
                .collect(
                        () -> new AtomicInteger(42),
                        AtomicInteger::addAndGet)
                .run(getEngine())).get(), 42);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void collectShouldPropagateUpstreamErrors() {
        await(rs.<Integer>failed(new QuietRuntimeException("failed"))
                .collect(
                        () -> new AtomicInteger(0),
                        AtomicInteger::addAndGet)
                .run(getEngine()));
    }

    @Test
    public void finisherFunctionShouldBeInvoked() {
        assertEquals(await(rs.of("1", "2", "3")
                .collect(Collectors.joining(", ")).run(getEngine())), "1, 2, 3");
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void toListStageShouldPropagateUpstreamErrors() {
        await(rs.failed(new QuietRuntimeException("failed"))
                .toList().run(getEngine()));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void collectStageShouldPropagateErrorsFromSupplierThroughCompletionStage() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<Integer> result = null;
        try {
            result = infiniteStream()
                    .onTerminate(() -> cancelled.complete(null))
                    .collect(Collector.<Integer, Integer, Integer>of(() -> {
                        throw new QuietRuntimeException("failed");
                    }, (a, b) -> {
                    }, (a, b) -> a + b, Function.identity()))
                    .run(getEngine());
        } catch (Exception e) {
            fail("Exception thrown directly from stream, it should have been captured by the returned CompletionStage",
                    e);
        }
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void collectStageShouldPropagateErrorsFromAccumulator() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<String> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .collect(Collector.of(() -> "", (a, b) -> {
                    throw new QuietRuntimeException("failed");
                }, (a, b) -> a + b, Function.identity()))
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void collectStageShouldPropagateErrorsFromFinisher() {
        CompletionStage<Integer> result = rs.of(1, 2, 3)
                .collect(Collector.<Integer, Integer, Integer>of(() -> 0, (a, b) -> {
                },
                        (a, b) -> a + b,
                        r -> {
                            throw new QuietRuntimeException("failed");
                        }))
                .run(getEngine());
        await(result);
    }

    @Test
    public void collectStageBuilderShouldBeReusable() {
        SubscriberBuilder<Integer, List<Integer>> toList = rs.<Integer>builder().toList();
        assertEquals(await(rs.of(1, 2, 3).to(toList).run(getEngine())), Arrays.asList(1, 2, 3));
        assertEquals(await(rs.of(4, 5, 6).to(toList).run(getEngine())), Arrays.asList(4, 5, 6));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Arrays.asList(new ToListSubscriberVerification(), new CollectSubscriberVerification());
    }

    class ToListSubscriberVerification extends StageSubscriberBlackboxVerification<Integer> {
        @Override
        public Subscriber<Integer> createSubscriber() {
            return rs.<Integer>builder().toList().build(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }

    class CollectSubscriberVerification extends StageSubscriberBlackboxVerification<Integer> {
        @Override
        public Subscriber<Integer> createSubscriber() {
            return rs.<Integer>builder()
                    .collect(
                            () -> new AtomicInteger(0),
                            AtomicInteger::addAndGet)
                    .build(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
