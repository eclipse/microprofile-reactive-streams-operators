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


import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test cases for OnError, OnTerminate and onComplete.
 */
public class OnStagesVerification extends AbstractStageVerification {

    private Runnable noop = () -> {
    };

    OnStagesVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void onCompleteStageShouldBeCalledWhenTheStreamComplete() {
        AtomicBoolean called = new AtomicBoolean();
        assertEquals(await(rs.of(1, 2, 3)
            .onComplete(() -> called.set(true))
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3));
        assertTrue(called.get());
    }

    @Test
    public void onTerminateStageShouldBeCalledWhenTheStreamComplete() {
        AtomicBoolean called = new AtomicBoolean();
        assertEquals(await(rs.of(1, 2, 3)
            .onTerminate(() -> called.set(true))
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3));
        assertTrue(called.get());
    }

    @Test
    public void onErrorStageShouldNotBeCalledWhenTheStreamComplete() {
        AtomicBoolean called = new AtomicBoolean();
        assertEquals(await(rs.of(1, 2, 3)
            .onError(failure -> called.set(true))
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3));
        assertFalse(called.get());
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void onCompleteStageShouldPropagateRuntimeExceptions() {
        await(rs.of("foo")
            .onComplete(() -> {
                throw new QuietRuntimeException("failed");
            })
            .toList()
            .run(getEngine()));
    }

    @Test
    public void onCompleteStageShouldNotBeCalledWhenTheStreamFailed() {
        AtomicBoolean called = new AtomicBoolean();
        await(rs.failed(new QuietRuntimeException("failed"))
            .onComplete(() -> called.set(true))
            .toList()
            .run(getEngine())
            .exceptionally(t -> Collections.emptyList())
        );
        assertFalse(called.get());
    }

    @Test
    public void onErrorStageShouldBeCalledWhenTheStreamFailed() {
        AtomicReference<Throwable> called = new AtomicReference<>();
        await(rs.failed(new QuietRuntimeException("failed"))
            .onError(called::set)
            .toList()
            .run(getEngine())
            .exceptionally(t -> Collections.emptyList())
        );
        assertNotNull(called.get());
        assertTrue(called.get().getMessage().equalsIgnoreCase("failed"));
    }

    @Test
    public void onErrorStageShouldBeCalledWhenTheStreamFailedBecauseOfAnIntermediateStage() {
        AtomicReference<Throwable> called = new AtomicReference<>();
        await(rs.of(1, 2, 3)
            .map(x -> {
                throw new QuietRuntimeException("failed");
            })
            .onError(called::set)
            .toList()
            .run(getEngine())
            .exceptionally(t -> Collections.emptyList())
        );
        assertNotNull(called.get());
        assertTrue(called.get().getMessage().equalsIgnoreCase("failed"));
    }

    @Test
    public void onTerminateStageShouldBeCalledWhenTheStreamFailed() {
        AtomicBoolean called = new AtomicBoolean();
        await(rs.failed(new QuietRuntimeException("failed"))
            .onTerminate(() -> called.set(true))
            .toList()
            .run(getEngine())
            .exceptionally(t -> Collections.emptyList())
        );
        assertTrue(called.get());
    }

    @Test
    public void onTerminateStageShouldBeCalledWhenTheStreamFailedBecauseOfAnIntermediateStage() {
        AtomicBoolean called = new AtomicBoolean();
        await(rs.of(1, 2, 3)
            .map(x -> {
                throw new QuietRuntimeException("failed");
            })
            .onTerminate(() -> called.set(true))
            .toList()
            .run(getEngine())
            .exceptionally(t -> Collections.emptyList())
        );
        assertTrue(called.get());
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void onTerminateStageShouldPropagateRuntimeExceptions() {
        await(rs.of("foo")
            .onTerminate(() -> {
                throw new QuietRuntimeException("failed");
            })
            .toList()
            .run(getEngine()));
    }

    @Test
    public void onCompleteAndOnTerminateStagesShouldBeCalledWhenTheStreamCompletes() {
        AtomicBoolean onTerminateCalled = new AtomicBoolean();
        AtomicBoolean onCompleteCalled = new AtomicBoolean();

        await(rs.empty()
            .onComplete(() -> onCompleteCalled.set(true))
            .onTerminate(() -> onTerminateCalled.set(true))
            .toList()
            .run(getEngine())
        );
        assertTrue(onTerminateCalled.get());
        assertTrue(onCompleteCalled.get());
    }

    @Test
    public void onErrorAndOnTerminateStagesShouldBeCalledWhenTheStreamFails() {
        AtomicBoolean onTerminateCalled = new AtomicBoolean();
        AtomicReference<Throwable> onErrorCalled = new AtomicReference<>();

        await(rs.failed(new QuietRuntimeException("failed"))
            .onError(onErrorCalled::set)
            .onTerminate(() -> onTerminateCalled.set(true))
            .toList()
            .run(getEngine())
            .exceptionally(t -> Collections.emptyList())
        );
        assertTrue(onTerminateCalled.get());
        assertEquals(onErrorCalled.get().getMessage(), "failed");
    }

    @Test
    public void onTerminateShouldBeCalledWhenTheStreamIsCancelledFromDownstream() {
        CompletableFuture<Void> onTerminateCalled = new CompletableFuture<>();
        rs.of(1)
            .flatMapCompletionStage(i -> new CompletableFuture())
            .onTerminate(() -> onTerminateCalled.complete(null))
            .cancel()
            .run(getEngine());
        await(onTerminateCalled);
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Arrays.asList(
            new OnCompleteVerification(),
            new OnTerminateVerification(),
            new OnErrorVerification()
        );
    }

    public class OnCompleteVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder().onComplete(noop).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new QuietRuntimeException("failed"))
                .onComplete(noop).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }

    public class OnTerminateVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder().onTerminate(noop).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new QuietRuntimeException("failed"))
                .onTerminate(noop).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }

    public class OnErrorVerification extends StageProcessorVerification<Integer> {

        @Override
        public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
            return rs.<Integer>builder().onError(x -> {
            }).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new QuietRuntimeException("failed"))
                .onError(x -> {
                }).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
