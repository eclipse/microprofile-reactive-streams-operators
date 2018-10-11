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
 * Verification for the distinct stage.
 */
public class DistinctStageVerification extends AbstractStageVerification {

    DistinctStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void distinctStageShouldReturnDistinctElements() {
        assertEquals(await(rs.of(1, 2, 2, 3, 2, 1, 3)
            .distinct()
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3));
    }

    @Test
    public void distinctStageShouldReturnAnEmptyStreamWhenCalledOnEmptyStreams() {
        assertEquals(await(rs.empty()
            .distinct()
            .toList()
            .run(getEngine())), Collections.emptyList());
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void distinctStageShouldPropagateUpstreamExceptions() {
        await(rs.failed(new QuietRuntimeException("failed"))
            .distinct()
            .toList()
            .run(getEngine()));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void distinctStageShouldPropagateExceptionsThrownByEquals() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        class ObjectThatThrowsFromEquals {
            @Override
            public int hashCode() {
                return 1;
            }

            @Override
            public boolean equals(Object obj) {
                throw new QuietRuntimeException("failed");
            }
        }
        CompletionStage<List<ObjectThatThrowsFromEquals>> result = rs.of(
            new ObjectThatThrowsFromEquals(), new ObjectThatThrowsFromEquals()
        )
            .onTerminate(() -> cancelled.complete(null))
            .distinct().toList().run(getEngine());
        await(cancelled);
        await(result);
    }

    @Test
    public void distinctStageShouldPropagateCancel() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        await(infiniteStream().onTerminate(() -> cancelled.complete(null)).distinct().cancel().run(getEngine()));
        await(cancelled);
    }

    @Test
    public void distinctStageBuilderShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> distinct = rs.<Integer>builder().distinct();
        assertEquals(await(rs.of(1, 2, 2, 3).via(distinct).toList().run(getEngine())), Arrays.asList(1, 2, 3));
        assertEquals(await(rs.of(3, 3, 4, 5).via(distinct).toList().run(getEngine())), Arrays.asList(3, 4, 5));
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
            return rs.<Integer>builder().distinct().buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new RuntimeException("failed"))
                .distinct().buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
