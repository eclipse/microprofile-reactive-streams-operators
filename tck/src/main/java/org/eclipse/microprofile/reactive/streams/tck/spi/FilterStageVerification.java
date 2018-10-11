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
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.testng.Assert.assertEquals;

/**
 * Verification for the filter stage.
 */
public class FilterStageVerification extends AbstractStageVerification {

    FilterStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void filterStageShouldFilterElements() {
        assertEquals(await(rs.of(1, 2, 3, 4, 5, 6)
            .filter(i -> (i & 1) == 1)
            .toList()
            .run(getEngine())), Arrays.asList(1, 3, 5));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void filterStageShouldPropagateExceptions() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = infiniteStream()
            .onTerminate(() -> cancelled.complete(null))
            .filter(foo -> {
                throw new QuietRuntimeException("failed");
            })
            .toList()
            .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void filterStageShouldPropagateUpstreamExceptions() {
        await(rs.failed(new QuietRuntimeException("failed"))
            .filter(foo -> true)
            .toList()
            .run(getEngine()));
    }

    @Test
    public void filterStageShouldPropagateCancel() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        await(infiniteStream().onTerminate(() -> cancelled.complete(null)).filter(i -> i < 3).cancel().run(getEngine()));
        await(cancelled);
    }

    @Test
    public void filterStageBuilderShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> filter = rs.<Integer>builder().filter(i -> i < 3);
        assertEquals(await(rs.of(1, 2, 3).via(filter).toList().run(getEngine())), Arrays.asList(1, 2));
        assertEquals(await(rs.of(1, 2, 3).via(filter).toList().run(getEngine())), Arrays.asList(1, 2));
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
            return rs.<Integer>builder().filter(i -> true).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new RuntimeException("failed"))
                .filter(i -> true).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
