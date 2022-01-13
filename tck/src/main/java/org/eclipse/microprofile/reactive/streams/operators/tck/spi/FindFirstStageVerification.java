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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Subscriber;
import org.testng.annotations.Test;

/**
 * Verification for find first stage.
 */
public class FindFirstStageVerification extends AbstractStageVerification {

    FindFirstStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void findFirstStageShouldFindTheFirstElement() {
        assertEquals(await(rs.of(1, 2, 3)
                .findFirst().run(getEngine())), Optional.of(1));
    }

    @Test
    public void findFirstStageShouldFindTheFirstElementInSingleElementStream() {
        assertEquals(await(rs.of(1)
                .findFirst().run(getEngine())), Optional.of(1));
    }

    @Test
    public void findFirstStageShouldReturnEmptyForEmptyStream() {
        assertEquals(await(rs.of()
                .findFirst().run(getEngine())), Optional.empty());
    }

    @Test
    public void findFirstStageShouldCancelUpstream() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        assertEquals(await(infiniteStream().onTerminate(() -> cancelled.complete(null))
                .findFirst().run(getEngine())), Optional.of(1));
        await(cancelled);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void findFirstStageShouldPropagateErrors() {
        await(rs.failed(new QuietRuntimeException("failed"))
                .findFirst().run(getEngine()));
    }

    @Test
    public void findFirstStageShouldBeReusable() {
        assertEquals(await(rs.of(1, 2, 3)
                .findFirst().run(getEngine())), Optional.of(1));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new SubscriberVerification());
    }

    class SubscriberVerification extends StageSubscriberBlackboxVerification<Integer> {
        @Override
        public Subscriber<Integer> createSubscriber() {
            return rs.<Integer>builder().findFirst().build(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
