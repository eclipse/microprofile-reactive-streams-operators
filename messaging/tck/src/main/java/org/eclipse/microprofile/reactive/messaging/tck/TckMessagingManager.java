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

package org.eclipse.microprofile.reactive.messaging.tck;

import org.eclipse.microprofile.reactive.messaging.tck.container.MockedReceiver;
import org.eclipse.microprofile.reactive.messaging.tck.container.MockedSender;
import org.eclipse.microprofile.reactive.messaging.tck.container.TestEnvironment;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to create and hold receivers and senders.
 */
@ApplicationScoped
public class TckMessagingManager {
    private final Map<String, MockedReceiver<?>> receivers = new ConcurrentHashMap<>();
    private final Map<String, MockedSender<?>> senders = new ConcurrentHashMap<>();

    public <T> MockedReceiver<T> getReceiver(String topic) {
        return (MockedReceiver<T>) receivers.computeIfAbsent(topic, t -> new MockedReceiver<T>(TestEnvironment.DEFAULT, topic));
    }

    public <T> MockedSender<T> getSender(String topic) {
        return (MockedSender<T>) senders.computeIfAbsent(topic, t -> new MockedSender<T>());
    }
}
