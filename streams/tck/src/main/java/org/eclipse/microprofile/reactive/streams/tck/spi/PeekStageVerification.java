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
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class PeekStageVerification extends AbstractStageVerification {

    PeekStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void peekStageShouldNotModifyElements() {
        AtomicInteger count = new AtomicInteger();
        assertEquals(await(ReactiveStreams.of(1, 2, 3)
            .peek(i -> count.incrementAndGet())
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3));
        assertEquals(count.get(), 3);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void peekStageShouldHandleExceptions() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = infiniteStream()
            .onTerminate(() -> cancelled.complete(null))
            .peek(x -> {
                throw new QuietRuntimeException("failed");
            })
            .toList()
            .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void peekStageShouldPropagateUpstreamExceptions() {
        await(ReactiveStreams.failed(new QuietRuntimeException("failed"))
            .peek(x -> {})
            .toList()
            .run(getEngine()));
    }

    public void peekStageShouldNotBeExecutedForEmptyStreams() {
        AtomicBoolean invoked = new AtomicBoolean();
        await(ReactiveStreams.empty()
            .peek(x -> invoked.set(true))
            .toList()
            .run(getEngine()));
        assertFalse(invoked.get());
    }

    @Test
    public void peekStageShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> peek = ReactiveStreams.<Integer>builder().peek(t -> {});

        assertEquals(await(ReactiveStreams.of(1, 2, 3).via(peek).toList().run(getEngine())), Arrays.asList(1, 2, 3));
        assertEquals(await(ReactiveStreams.of(4, 5, 6).via(peek).toList().run(getEngine())), Arrays.asList(4, 5, 6));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(
            new ProcessorVerification()
        );
    }

    public class ProcessorVerification extends StageProcessorVerification<Integer> {

        private Consumer<Integer> noop = x -> {
        };

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return ReactiveStreams.<Integer>builder().peek(noop).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return ReactiveStreams.<Integer>failed(new RuntimeException("failed"))
                .peek(noop).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
