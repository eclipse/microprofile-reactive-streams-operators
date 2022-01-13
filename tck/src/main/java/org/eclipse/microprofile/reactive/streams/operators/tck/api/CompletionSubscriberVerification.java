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

package org.eclipse.microprofile.reactive.streams.operators.tck.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.streams.operators.CompletionSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

/**
 * Verification for {@link org.eclipse.microprofile.reactive.streams.operators.CompletionSubscriber}.
 */
public class CompletionSubscriberVerification {

    @Test
    public void completionSubscriberShouldReturnSameCompletionStage() {
        CompletableFuture future = new CompletableFuture();
        assertSame(CompletionSubscriber.of(Mocks.SUBSCRIBER, future).getCompletion(), future);
    }

    @Test
    public void completionSubscriberShouldDelegateToSubscriber() {
        Deque<Object> calls = new ArrayDeque<>();
        CompletionSubscriber subscriber = CompletionSubscriber.of(new Subscriber() {
            @Override
            public void onSubscribe(Subscription s) {
                calls.add(s);
            }

            @Override
            public void onNext(Object o) {
                calls.add(o);
            }

            @Override
            public void onError(Throwable t) {
                calls.add(t);
            }

            @Override
            public void onComplete() {
                calls.add("onComplete");
            }
        }, new CompletableFuture<>());

        subscriber.onSubscribe(Mocks.SUBSCRIPTION);
        assertSame(calls.removeFirst(), Mocks.SUBSCRIPTION);
        subscriber.onNext("element");
        assertEquals(calls.removeFirst(), "element");
        Exception e = new Exception();
        subscriber.onError(e);
        assertSame(calls.removeFirst(), e);
        subscriber.onComplete();
        assertEquals(calls.removeFirst(), "onComplete");
        assertTrue(calls.isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void completionSubscriberShouldNotAcceptNullSubscriber() {
        CompletionSubscriber.of(null, new CompletableFuture<>());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void completionSubscriberShouldNotAcceptNullCompletionStage() {
        CompletionSubscriber.of(Mocks.SUBSCRIBER, null);
    }
}
