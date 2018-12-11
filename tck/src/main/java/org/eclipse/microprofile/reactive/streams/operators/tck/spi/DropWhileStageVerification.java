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

package org.eclipse.microprofile.reactive.streams.operators.tck.spi;

import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.testng.Assert.assertEquals;

/**
 * Verification for the drop while stage.
 */
public class DropWhileStageVerification extends AbstractStageVerification {

    DropWhileStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void dropWhileStageShouldSupportDroppingElements() {
        assertEquals(await(rs.of(1, 2, 3, 4, 0)
            .dropWhile(i -> i < 3)
            .toList()
            .run(getEngine())), Arrays.asList(3, 4, 0));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void dropWhileStageShouldHandleErrors() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = infiniteStream()
            .onTerminate(() -> cancelled.complete(null))
            .dropWhile(i -> {
                throw new QuietRuntimeException("failed");
            })
            .toList()
            .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void dropWhileStageShouldPropagateUpstreamErrorsWhileDropping() {
        await(rs.<Integer>failed(new QuietRuntimeException("failed"))
            .dropWhile(i -> i < 3)
            .toList()
            .run(getEngine()));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void dropWhileStageShouldPropagateUpstreamErrorsAfterFinishedDropping() {
        await(infiniteStream()
            .peek(i -> {
                if (i == 4) {
                    throw new QuietRuntimeException("failed");
                }
            })
            .dropWhile(i -> i < 3)
            .toList()
            .run(getEngine()));
    }

    @Test
    public void dropWhileStageShouldNotRunPredicateOnceItsFinishedDropping() {
        assertEquals(await(rs.of(1, 2, 3, 4)
            .dropWhile(i -> {
                if (i < 3) {
                    return true;
                }
                else if (i == 4) {
                    throw new RuntimeException("4 was passed");
                }
                else {
                    return false;
                }
            })
            .toList()
            .run(getEngine())), Arrays.asList(3, 4));
    }

    @Test
    public void dropWhileStageShouldAllowCompletionWhileDropping() {
        assertEquals(await(rs.of(1, 1, 1, 1)
            .dropWhile(i -> i < 3)
            .toList()
            .run(getEngine())), Collections.emptyList());
    }

    @Test
    public void dropWhileStageShouldPropagateCancel() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        await(infiniteStream().onTerminate(() -> cancelled.complete(null)).dropWhile(i -> i < 3).cancel().run(getEngine()));
        await(cancelled);
    }

    @Test
    public void dropWhileStageBuilderShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> dropWhile = rs.<Integer>builder()
            .dropWhile(i -> i < 3);

        assertEquals(await(rs.of(1, 2, 3, 4)
            .via(dropWhile)
            .toList()
            .run(getEngine())), Arrays.asList(3, 4));

        assertEquals(await(rs.of(0, 1, 6, 7)
            .via(dropWhile)
            .toList()
            .run(getEngine())), Arrays.asList(6, 7));

    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(
            new ProcessorVerification()
        );
    }

    class ProcessorVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder().dropWhile(i -> false).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new RuntimeException("failed"))
                .dropWhile(i -> false).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
