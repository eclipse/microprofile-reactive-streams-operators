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
import java.util.function.Function;

import static org.testng.Assert.assertEquals;

public class MapStageVerification extends AbstractStageVerification {

    MapStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void mapStageShouldMapElements() {
        assertEquals(await(ReactiveStreams.of(1, 2, 3)
            .map(Object::toString)
            .toList()
            .run(getEngine())), Arrays.asList("1", "2", "3"));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void mapStageShouldHandleExceptions() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
            .onTerminate(() -> cancelled.complete(null))
            .map(foo -> {
                throw new QuietRuntimeException("failed");
            })
            .toList()
            .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void mapStageShouldPropagateUpstreamExceptions() {
        await(ReactiveStreams.failed(new QuietRuntimeException("failed"))
            .map(Function.identity())
            .toList()
            .run(getEngine()));
    }

    @Test
    public void mapStageBuilderShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> map = ReactiveStreams.<Integer>builder().map(i -> i + 1);
        assertEquals(await(ReactiveStreams.of(1, 2, 3).via(map).toList().run(getEngine())), Arrays.asList(2, 3, 4));
        assertEquals(await(ReactiveStreams.of(4, 5, 6).via(map).toList().run(getEngine())), Arrays.asList(5, 6, 7));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(
            new ProcessorVerification()
        );
    }

    public class ProcessorVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return ReactiveStreams.<Integer>builder().map(Function.identity()).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return ReactiveStreams.<Integer>failed(new RuntimeException("failed"))
                .map(Function.identity()).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
