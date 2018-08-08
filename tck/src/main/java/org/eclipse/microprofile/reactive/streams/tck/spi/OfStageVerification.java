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

import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.LongStream;

import static org.testng.Assert.assertEquals;

public class OfStageVerification extends AbstractStageVerification {

    OfStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void iterableStageShouldEmitManyElements() {
        assertEquals(await(
            ReactiveStreams.of("a", "b", "c")
                .toList()
                .run(getEngine())
        ), Arrays.asList("a", "b", "c"));
    }

    @Test
    public void emptyIterableStageShouldEmitNoElements() {
        assertEquals(await(
            ReactiveStreams.empty()
                .toList()
                .run(getEngine())
        ), Collections.emptyList());
    }

    @Test
    public void singleIterableStageShouldEmitOneElement() {
        assertEquals(await(
            ReactiveStreams.of("a")
                .toList()
                .run(getEngine())
        ), Collections.singletonList("a"));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void ofStageShouldHandleExceptionsInIterableIterateMethod() {
        CompletionStage<List<Object>> result;
        try {
            result = ReactiveStreams.fromIterable(() -> {
                throw new QuietRuntimeException("failed");
            })
                .toList()
                .run(getEngine());
        }
        catch (QuietRuntimeException e) {
            throw new AssertionError("Exception was thrown directly, should have been part of the redeemed completion stage", e);
        }
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void ofStageShouldHandleExceptionsInIteratorHasNextMethod() {
        await(ReactiveStreams.fromIterable(() -> new Iterator<Object>() {
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
            .run(getEngine()));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void ofStageShouldHandleExceptionsInIteratorNextMethod() {
        await(ReactiveStreams.fromIterable(() -> new Iterator<Object>() {
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
            .run(getEngine()));
    }

    @Test
    public void ofStageShouldBeReusable() {
        PublisherBuilder<Integer> publisher = ReactiveStreams.of(1, 2, 3);

        assertEquals(await(publisher.toList().run(getEngine())), Arrays.asList(1, 2, 3));
        assertEquals(await(publisher.toList().run(getEngine())), Arrays.asList(1, 2, 3));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new PublisherVerification());
    }

    public class PublisherVerification extends StagePublisherVerification<Long> {
        @Override
        public Publisher<Long> createPublisher(long elements) {
            return ReactiveStreams.fromIterable(
                () -> LongStream.rangeClosed(1, elements).boxed().iterator()
            ).buildRs(getEngine());
        }
    }


}
