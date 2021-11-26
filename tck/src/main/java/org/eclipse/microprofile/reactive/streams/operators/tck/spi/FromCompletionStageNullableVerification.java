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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

public class FromCompletionStageNullableVerification extends AbstractStageVerification {

    FromCompletionStageNullableVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void fromCsNullableStageShouldEmitAnElementWhenAlreadyRedeemed() {
        assertEquals(await(
                rs.fromCompletionStageNullable(CompletableFuture.completedFuture(10))
                        .toList()
                        .run(getEngine())),
                Collections.singletonList(10));
    }

    @Test
    public void fromCsNullableStageShouldEmitAnElementWhenRedeemedLater() throws InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = rs.fromCompletionStageNullable(future)
                .toList()
                .run(getEngine());
        // Give it some time to not complete
        Thread.sleep(100);
        assertFalse(result.toCompletableFuture().isDone());
        future.complete(10);
        assertEquals(await(result), Collections.singletonList(10));
    }

    @Test
    public void fromCsNullableStageShouldBeEmptyWhenAlreadyRedeemedWithNull() {
        assertEquals(await(
                rs.fromCompletionStageNullable(CompletableFuture.completedFuture(null))
                        .toList()
                        .run(getEngine())),
                Collections.emptyList());
    }

    @Test
    public void fromCsNullableStageShouldBeEmptyWhenRedeemedWithNullLater() throws InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = rs.fromCompletionStageNullable(future)
                .toList()
                .run(getEngine());
        // Give it some time to not complete
        Thread.sleep(100);
        assertFalse(result.toCompletableFuture().isDone());
        future.complete(null);
        assertEquals(await(result), Collections.emptyList());
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void fromCsNullableStageShouldPropagateAlreadyRedeemedExceptions() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future.completeExceptionally(new QuietRuntimeException("failed"));
        await(
                rs.fromCompletionStageNullable(future)
                        .toList()
                        .run(getEngine()));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void fromCsNullableStageShouldPropagateExceptionsWhenFailedLater() throws InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = rs.fromCompletionStageNullable(future)
                .toList()
                .run(getEngine());
        // Give it some time to not complete
        Thread.sleep(100);
        assertFalse(result.toCompletableFuture().isDone());
        future.completeExceptionally(new QuietRuntimeException("failed"));
        await(result);
    }

    @Test
    public void fromCsNullableStageShouldBeReusable() {
        PublisherBuilder<Integer> publisher =
                rs.fromCompletionStageNullable(CompletableFuture.completedFuture(10));

        assertEquals(await(publisher.toList().run(getEngine())), Collections.singletonList(10));
        assertEquals(await(publisher.toList().run(getEngine())), Collections.singletonList(10));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new PublisherVerification());
    }

    public class PublisherVerification extends StagePublisherVerification<String> {
        @Override
        public Publisher<String> createPublisher(long elements) {
            return rs.fromCompletionStageNullable(
                    CompletableFuture.completedFuture(elements == 0 ? null : "value")).buildRs(getEngine());
        }

        @Override
        public long maxElementsFromPublisher() {
            return 1;
        }
    }

}
