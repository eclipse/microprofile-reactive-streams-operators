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

package org.eclipse.microprofile.reactive.streams.operators.tck.api;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreamsFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Factory for tests that verify the API itself without dependence on an SPI implementation.
 * <p>
 * These tests serve not only to test the API provided by the MicroProfile API project, but also to test clean room
 * implementations of the API, that is, implementations that don't depend on the MicroProfile API artifact.
 */
public class ReactiveStreamsApiVerification {

    private final ReactiveStreamsFactory rs;

    public ReactiveStreamsApiVerification(ReactiveStreamsFactory rs) {
        this.rs = rs;
    }

    public List<Object> allTests() {
        return Arrays.asList(
            new ReactiveStreamsVerification(rs),
            new PublisherBuilderVerification(rs),
            new ProcessorBuilderVerification(rs),
            new SubscriberBuilderVerification(rs),
            new CompletionRunnerVerification(rs),
            new CompletionSubscriberVerification()
        );
    }
}
