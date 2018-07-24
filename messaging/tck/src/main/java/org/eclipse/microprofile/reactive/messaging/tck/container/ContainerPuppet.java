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
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.ByteArrayOutputStream;

/**
 * Convenience helper to send messages serialized using JSONB.
 */
public class ContainerPuppet {

    @Inject
    private Instance<TckMessagingPuppet> puppet;

    private final Jsonb jsonb = JsonbBuilder.create();

    public void sendMessages(String topic, Message<?>... messages) {
        for (Message<?> message : messages) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jsonb.toJson(message.getPayload(), baos);
            puppet.get().sendMessage(topic, Message.of(baos.toByteArray()));
        }
    }

    public void sendPayloads(String topic, Object... payloads) {
        for (Object payload : payloads) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jsonb.toJson(payload, baos);
            puppet.get().sendMessage(topic, Message.of(baos.toByteArray()));
        }
    }

}
