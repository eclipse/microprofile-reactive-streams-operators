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

import static org.testng.Assert.assertEquals;

public class SkipStageVerification extends AbstractStageVerification {

    SkipStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void skipStageShouldSkipElements() {
        assertEquals(await(rs.of(1, 2, 3, 4)
            .skip(2)
            .toList()
            .run(getEngine())), Arrays.asList(3, 4));
    }

    @Test
    public void skipStageShouldSupportSkippingNoElements() {
        assertEquals(await(rs.of(1, 2, 3, 4)
            .skip(0)
            .toList()
            .run(getEngine())), Arrays.asList(1, 2, 3, 4));
    }

    @Test
    public void skipStageShouldBeReusable() {
        ProcessorBuilder<Integer, Integer> skip = rs.<Integer>builder().skip(2);

        assertEquals(await(rs.of(1, 2, 3, 4).via(skip).toList().run(getEngine())), Arrays.asList(3, 4));
        assertEquals(await(rs.of(5, 6, 7, 8).via(skip).toList().run(getEngine())), Arrays.asList(7, 8));
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
            return rs.<Integer>builder().skip(0).buildRs(getEngine());
        }

        @Override
        public Publisher<Integer> createFailedPublisher() {
            return rs.<Integer>failed(new RuntimeException("failed"))
                .skip(1).buildRs(getEngine());
        }

        @Override
        public Integer createElement(int element) {
            return element;
        }
    }
}
