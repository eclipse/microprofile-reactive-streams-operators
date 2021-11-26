/*******************************************************************************
 * Copyright (c) 2018, 2021 Contributors to the Eclipse Foundation
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;

import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

public class SubscriberStageVerification extends AbstractStageVerification {
    SubscriberStageVerification(ReactiveStreamsSpiVerification.VerificationDeps deps) {
        super(deps);
    }

    @Test
    public void subscriberStageShouldRedeemCompletionStageWhenCompleted() {
        CompletionStage<Void> result = rs.of().to(
                rs.builder().ignore().build(getEngine())).run(getEngine());
        await(result);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void subscriberStageShouldRedeemCompletionStageWhenFailed() {
        CompletionStage<Void> result = rs.failed(new RuntimeException("failed")).to(
                rs.builder().ignore().build(getEngine())).run(getEngine());
        await(result);
    }

    @Test(expectedExceptions = CancellationException.class)
    public void subscriberStageShouldRedeemCompletionStageWithCancellationExceptionWhenCancelled() {
        CompletionStage<Void> result = rs.fromPublisher(subscriber -> subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
            }

            @Override
            public void cancel() {
            }
        })).to(
                rs.builder().cancel().build(getEngine())).run(getEngine());
        await(result);
    }

    @Override
    List<Object> reactiveStreamsTckVerifiers() {
        return Collections.emptyList();
    }
}
