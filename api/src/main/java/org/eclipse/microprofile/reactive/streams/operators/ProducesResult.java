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

package org.eclipse.microprofile.reactive.streams.operators;

/**
 * A stream that completes with a single result.
 * <p>
 * This will either be a {@link SubscriberBuilder}, representing a stream with an inlet that can be plumbed to a
 * publisher in order to run it, or a {@link CompletionRunner}, representing a closed graph that can be run as is.
 *
 * @param <T>
 *            The result of the stream.
 * @see SubscriberBuilder
 * @see CompletionRunner
 */
public interface ProducesResult<T> {
}
