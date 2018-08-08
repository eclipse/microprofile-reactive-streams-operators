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

import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
import org.reactivestreams.tck.TestEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * This test is a factory for all the tests for verifying implementations of the SPI.
 */
public class ReactiveStreamsSpiVerification {

    private final TestEnvironment testEnvironment;
    private final ReactiveStreamsEngine engine;
    private final ScheduledExecutorService executorService;

    public ReactiveStreamsSpiVerification(TestEnvironment testEnvironment, ReactiveStreamsEngine engine, ScheduledExecutorService executorService) {
        this.testEnvironment = testEnvironment;
        this.engine = engine;
        this.executorService = executorService;
    }

    class VerificationDeps {
        ReactiveStreamsEngine engine() {
            return engine;
        }

        TestEnvironment testEnvironment() {
            return testEnvironment;
        }

        ScheduledExecutorService executorService() {
            return executorService;
        }
    }

    public List<Object> allTests() {
        List<Function<VerificationDeps, AbstractStageVerification>> stageVerifications = Arrays.asList(
            OfStageVerification::new,
            MapStageVerification::new,
            FlatMapStageVerification::new,
            FilterStageVerification::new,
            FindFirstStageVerification::new,
            CollectStageVerification::new,
            TakeWhileStageVerification::new,
            FlatMapCompletionStageVerification::new,
            FlatMapIterableStageVerification::new,
            ConcatStageVerification::new,
            EmptyProcessorVerification::new,
            CancelStageVerification::new,
            SubscriberStageVerification::new,
            PeekStageVerification::new,
            DistinctStageVerification::new,
            OnStagesVerification::new,
            LimitStageVerification::new,
            SkipStageVerification::new,
            DropWhileStageVerification::new,
            OnErrorResumeStageVerification::new
        );

        List<Object> allTests = new ArrayList<>();
        VerificationDeps deps = new VerificationDeps();
        for (Function<VerificationDeps, AbstractStageVerification> creator : stageVerifications) {
            AbstractStageVerification stageVerification = creator.apply(deps);
            allTests.add(stageVerification);
            allTests.addAll(stageVerification.reactiveStreamsTckVerifiers());
        }

        return allTests;
    }
}
