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

package org.eclipse.microprofile.reactive.streams.operators.core;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.streams.operators.CompletionRunner;
import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

final class CompletionRunnerImpl<T> extends ReactiveStreamsGraphBuilder implements CompletionRunner<T> {

    CompletionRunnerImpl(Stage stage, ReactiveStreamsGraphBuilder previous) {
        super(stage, previous);
    }

    @Override
    public CompletionStage<T> run() {
        return run(ReactiveStreamsEngineResolver.instance());
    }

    @Override
    public CompletionStage<T> run(ReactiveStreamsEngine engine) {
        Objects.requireNonNull(engine, "Engine must not be null");
        return engine.buildCompletion(toGraph());
    }
}
