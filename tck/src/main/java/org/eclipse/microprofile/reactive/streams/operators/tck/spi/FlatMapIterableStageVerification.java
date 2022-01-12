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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

/**
 * Verification for flat map iterable stage.
 */
public class FlatMapIterableStageVerification extends AbstractStageVerification {
    FlatMapIterableStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void flatMapIterableStageShouldMapElements() {
        assertEquals(await(rs.of(1, 2, 3)
                .flatMapIterable(n -> Arrays.asList(n, n, n))
                .toList()
                .run(getEngine())), Arrays.asList(1, 1, 1, 2, 2, 2, 3, 3, 3));
    }

    @Test
    public void flatMapIterableStageShouldSupportEmptyMappingOfElements() {
        assertEquals(await(rs.of(1, 2, 3)
                .flatMapIterable(n -> Collections.emptyList())
                .toList()
                .run(getEngine())), Collections.emptyList());
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapIterableStageShouldHandleExceptionsInCallback() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(foo -> {
                    throw new QuietRuntimeException("failed");
                })
                .toList()
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapIterableStageShouldHandleExceptionsInIterableIterateMethod() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(foo -> () -> {
                    throw new QuietRuntimeException("failed");
                })
                .toList()
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapIterableStageShouldHandleExceptionsInIteratorHasNextMethod() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(foo -> () -> new Iterator<Object>() {
                    @Override
                    public boolean hasNext() {
                        throw new QuietRuntimeException("failed");
                    }
                    @Override
                    public Object next() {
                        return null;
                    }
                })
                .toList()
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapIterableStageShouldHandleExceptionsInIteratorNextMethod() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream()
                .onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(foo -> () -> new Iterator<Object>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }
                    @Override
                    public Object next() {
                        throw new QuietRuntimeException("failed");
                    }
                })
                .toList()
                .run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapIterableStageShouldPropagateUpstreamExceptions() {
        await(rs.failed(new QuietRuntimeException("failed"))
                .flatMapIterable(Collections::singletonList)
                .toList()
                .run(getEngine()));
    }

    @Test
    public void flatMapIterableBuilderShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> mapper = rs.<Integer>builder().flatMapIterable(i -> Arrays.asList(i, i));
        assertEquals(await(rs.of(1, 2).via(mapper).toList().run(getEngine())), Arrays.asList(1, 1, 2, 2));
        assertEquals(await(rs.of(1, 2).via(mapper).toList().run(getEngine())), Arrays.asList(1, 1, 2, 2));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapIterableStageShouldFailIfNullIterableReturned() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream().onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(t -> null)
                .toList().run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapIterableStageShouldFailIfNullIteratorReturned() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream().onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(t -> () -> null)
                .toList().run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapIterableStageShouldFailIfNullElementReturnedFromIterator() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = infiniteStream().onTerminate(() -> cancelled.complete(null))
                .flatMapIterable(t -> Collections.singletonList(null))
                .toList().run(getEngine());
        await(cancelled);
        await(result);
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new ProcessorVerification());
    }

    /**
     * Verifies the outer processor.
     */
    public class ProcessorVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder().flatMapIterable(Arrays::asList).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new RuntimeException("failed"))
                    .flatMapIterable(Arrays::asList).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }

}
