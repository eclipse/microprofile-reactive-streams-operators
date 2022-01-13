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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.reactivestreams.Processor;
import org.testng.annotations.Test;

/**
 * Verification for flat map completion stage.
 */
public class FlatMapCompletionStageVerification extends AbstractStageVerification {
    FlatMapCompletionStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void flatMapCsStageShouldMapFutures() throws Exception {
        CompletableFuture<Integer> one = new CompletableFuture<>();
        CompletableFuture<Integer> two = new CompletableFuture<>();
        CompletableFuture<Integer> three = new CompletableFuture<>();

        CompletionStage<List<Integer>> result = rs.of(one, two, three)
                .flatMapCompletionStage(Function.identity())
                .toList()
                .run(getEngine());

        Thread.sleep(100);

        one.complete(1);
        two.complete(2);
        three.complete(3);

        assertEquals(await(result), Arrays.asList(1, 2, 3));
    }

    @Test
    public void flatMapCsStageShouldMaintainOrderOfFutures() throws Exception {
        CompletableFuture<Integer> one = new CompletableFuture<>();
        CompletableFuture<Integer> two = new CompletableFuture<>();
        CompletableFuture<Integer> three = new CompletableFuture<>();

        CompletionStage<List<Integer>> result = rs.of(one, two, three)
                .flatMapCompletionStage(Function.identity())
                .toList()
                .run(getEngine());

        three.complete(3);
        Thread.sleep(100);
        two.complete(2);
        Thread.sleep(100);
        one.complete(1);

        assertEquals(await(result), Arrays.asList(1, 2, 3));
    }

    @Test
    public void flatMapCsStageShouldOnlyMapOneElementAtATime() throws Exception {
        CompletableFuture<Integer> one = new CompletableFuture<>();
        CompletableFuture<Integer> two = new CompletableFuture<>();
        CompletableFuture<Integer> three = new CompletableFuture<>();

        AtomicInteger concurrentMaps = new AtomicInteger(0);

        CompletionStage<List<Integer>> result = rs.of(one, two, three)
                .flatMapCompletionStage(i -> {
                    assertEquals(1, concurrentMaps.incrementAndGet());
                    return i;
                })
                .toList()
                .run(getEngine());

        Thread.sleep(100);
        concurrentMaps.decrementAndGet();
        one.complete(1);
        Thread.sleep(100);
        concurrentMaps.decrementAndGet();
        two.complete(2);
        Thread.sleep(100);
        concurrentMaps.decrementAndGet();
        three.complete(3);

        assertEquals(await(result), Arrays.asList(1, 2, 3));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapCsStageShouldPropagateUpstreamErrors() {
        await(rs.<Integer>failed(new QuietRuntimeException("failed"))
                .flatMapCompletionStage(CompletableFuture::completedFuture)
                .toList()
                .run(getEngine()));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapCsStageShouldHandleErrorsThrownByCallback() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .flatMapCompletionStage(i -> {
                    throw new QuietRuntimeException("failed");
                })
                .toList()
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapCsStageShouldHandleFailedCompletionStages() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .flatMapCompletionStage(i -> {
                    CompletableFuture<Object> failed = new CompletableFuture<>();
                    failed.completeExceptionally(new QuietRuntimeException("failed"));
                    return failed;
                })
                .toList()
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test
    public void flatMapCsStageShouldPropagateCancel() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        await(infiniteStream().onTerminate(() -> cancelled.complete(null))
                .flatMapCompletionStage(CompletableFuture::completedFuture).cancel().run(getEngine()));
        await(cancelled);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapCsStageShouldFailIfNullIsReturned() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream().onTerminate(() -> cancelled.complete(null))
                .flatMapCompletionStage(t -> CompletableFuture.completedFuture(null))
                .toList().run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test
    public void flatMapCsStageBuilderShouldBeResuable() {
        ProcessorBuilder<Integer, Integer> mapper = rs.<Integer>builder()
                .flatMapCompletionStage(i -> CompletableFuture.completedFuture(i + 1));
        assertEquals(await(rs.of(1, 2, 3).via(mapper).toList().run(getEngine())), Arrays.asList(2, 3, 4));
        assertEquals(await(rs.of(4, 5, 6).via(mapper).toList().run(getEngine())), Arrays.asList(5, 6, 7));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new ProcessorVerification());
    }

    public class ProcessorVerification extends StageProcessorVerification<Integer> {
        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder()
                    .flatMapCompletionStage(CompletableFuture::completedFuture)
                    .buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
