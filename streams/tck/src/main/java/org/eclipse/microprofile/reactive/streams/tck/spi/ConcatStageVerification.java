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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.LongStream;

import static org.testng.Assert.assertEquals;

/**
 * Verification for the concat stage.
 */
public class ConcatStageVerification extends AbstractStageVerification {

    ConcatStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void concatStageShouldConcatTwoGraphs() {
        assertEquals(await(
            ReactiveStreams.concat(
                ReactiveStreams.of(1, 2, 3),
                ReactiveStreams.of(4, 5, 6)
            )
                .toList()
                .run(getEngine())
        ), Arrays.asList(1, 2, 3, 4, 5, 6));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void concatStageShouldCancelSecondStageIfFirstFails() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();

        CompletionStage<Void> completion = ReactiveStreams.concat(
            ReactiveStreams.failed(new QuietRuntimeException("failed")),
            infiniteStream().onTerminate(() -> cancelled.complete(null))
        )
            .ignore()
            .run(getEngine());

        await(cancelled);
        await(completion);
    }

    @Test
    public void concatStageShouldCancelSecondStageIfFirstCancellationOccursDuringFirst() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();

        CompletionStage<List<Integer>> result = ReactiveStreams.concat(
            infiniteStream(),
            infiniteStream().onTerminate(() -> cancelled.complete(null))
        )
            .limit(5)
            .toList()
            .run(getEngine());

        await(cancelled);
        assertEquals(await(result), Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void concatStageShouldCancelSecondStageIfCancellationOccursDuringSecond() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();

        CompletionStage<List<Integer>> result = ReactiveStreams.concat(
            ReactiveStreams.of(1, 2, 3),
            infiniteStream().onTerminate(() -> cancelled.complete(null))
        )
            .limit(5)
            .toList()
            .run(getEngine());

        await(cancelled);
        assertEquals(await(result), Arrays.asList(1, 2, 3, 1, 2));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void concatStageShouldPropagateExceptionsFromSecondStage() {
        await(
            ReactiveStreams.concat(
                ReactiveStreams.of(1, 2, 3),
                ReactiveStreams.failed(new QuietRuntimeException("failed"))
            ).toList().run(getEngine())
        );
    }

    @Test
    public void concatStageShouldWorkWithEmptyFirstGraph() {
        assertEquals(await(
            ReactiveStreams.concat(
                ReactiveStreams.empty(),
                ReactiveStreams.of(1, 2, 3)
            )
                .toList()
                .run(getEngine())
        ), Arrays.asList(1, 2, 3));
    }

    @Test
    public void concatStageShouldWorkWithEmptySecondGraph() {
        assertEquals(await(
            ReactiveStreams.concat(
                ReactiveStreams.of(1, 2, 3),
                ReactiveStreams.empty()
            )
                .toList()
                .run(getEngine())
        ), Arrays.asList(1, 2, 3));
    }

    @Test
    public void concatStageShouldWorkWithBothGraphsEmpty() {
        assertEquals(await(
            ReactiveStreams.concat(
                ReactiveStreams.empty(),
                ReactiveStreams.empty()
            )
                .toList()
                .run(getEngine())
        ), Collections.emptyList());
    }

    @Test
    public void concatStageShouldSupportNestedConcats() {
        assertEquals(await(
            ReactiveStreams.concat(
                ReactiveStreams.concat(
                    ReactiveStreams.of(1, 2, 3),
                    ReactiveStreams.of(4, 5, 6)
                ),
                ReactiveStreams.concat(
                    ReactiveStreams.of(7, 8, 9),
                    ReactiveStreams.of(10, 11, 12)
                )
            )
                .toList()
                .run(getEngine())
        ), Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
    }

    @Test
    public void concatStageBuilderShouldBeReusable() {
        PublisherBuilder<Integer> concated = ReactiveStreams.concat(
            ReactiveStreams.of(1, 2, 3),
            ReactiveStreams.of(4, 5, 6)
        );
        assertEquals(await(concated.toList().run(getEngine())), Arrays.asList(1, 2, 3, 4, 5, 6));
        assertEquals(await(concated.toList().run(getEngine())), Arrays.asList(1, 2, 3, 4, 5, 6));
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.singletonList(new PublisherVerification());
    }

    class PublisherVerification extends StagePublisherVerification<Long> {
        @Override
        public Publisher<Long> createPublisher(long elements) {
            long toEmitFromFirst = elements / 2;

            return ReactiveStreams.concat(
                ReactiveStreams.fromIterable(
                    () -> LongStream.rangeClosed(1, toEmitFromFirst).boxed().iterator()
                ),
                ReactiveStreams.fromIterable(
                    () -> LongStream.rangeClosed(toEmitFromFirst + 1, elements).boxed().iterator()
                )
            ).buildRs(getEngine());
        }
    }

}
