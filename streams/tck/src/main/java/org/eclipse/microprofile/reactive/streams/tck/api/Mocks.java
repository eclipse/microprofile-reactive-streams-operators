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

package org.eclipse.microprofile.reactive.streams.tck.api;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Shared mock objects
 */
class Mocks {

    private Mocks() {
    }

    static final Subscriber SUBSCRIBER = new Subscriber() {
        @Override
        public void onSubscribe(Subscription s) {
        }
        @Override
        public void onNext(Object o) {
        }
        @Override
        public void onError(Throwable t) {
        }
        @Override
        public void onComplete() {
        }
    };

    static final Publisher PUBLISHER = s -> {};

    static final Processor PROCESSOR = new Processor() {
        @Override
        public void subscribe(Subscriber s) {
        }
        @Override
        public void onSubscribe(Subscription s) {
        }
        @Override
        public void onNext(Object o) {
        }
        @Override
        public void onError(Throwable t) {
        }
        @Override
        public void onComplete() {
        }
    };

    static final Subscription SUBSCRIPTION = new Subscription() {
        @Override
        public void request(long n) {
        }
        @Override
        public void cancel() {
        }
    };

}
