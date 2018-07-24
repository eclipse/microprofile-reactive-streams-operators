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

package org.eclipse.microprofile.reactive.messaging.tck.container;

import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.util.Optional;

/**
 * A puppet for sending and receiving messages.
 * <p>
 * An instance of this
 */
public interface TckMessagingPuppet {

    /**
     * Send a message to the given incoming topic.
     */
    void sendMessage(String topic, Message<byte[]> message);

    /**
     * Receive a message from the given outgoing topic.
     * <p>
     * If no message is received within the given timeout, empty should be returned.
     */
    Optional<Message<byte[]>> receiveMessage(String topic, Duration timeout);

    /**
     * The test environment.
     */
    default TestEnvironment testEnvironment() {
        return TestEnvironment.DEFAULT;
    }
}
