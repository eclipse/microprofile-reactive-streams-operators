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

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.tck.container.ContainerPuppet;
import org.eclipse.microprofile.reactive.messaging.tck.container.MockPayload;
import org.eclipse.microprofile.reactive.messaging.tck.container.MockedReceiver;
import org.eclipse.microprofile.reactive.messaging.tck.container.MockedSender;
import org.eclipse.microprofile.reactive.messaging.tck.container.QuietRuntimeException;
import org.eclipse.microprofile.reactive.messaging.tck.container.TckDeploymentUtils;
import org.eclipse.microprofile.reactive.messaging.tck.container.TestEnvironment;
import org.eclipse.microprofile.reactive.messaging.tck.container.Topics;
import org.eclipse.microprofile.reactive.messaging.tck.container.WaitAssert;
import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Provides tests for methods of the shape:
 *
 * <pre>
 * &#064;Incoming
 * public ProcessorBuilder&gt;SomeMessage, Void&lt; process() {
 *   ...
 * }
 * </pre>
 */
@Test(groups = {"all", "incoming"})
public class IncomingProcessorMethodVerification extends Arquillian {

    @Deployment
    public static JavaArchive createDeployment() {
        return TckDeploymentUtils.createDeployment(Bean.class);
    }

    private static final String SIMPLE = "simple";
    private static final String COMPLETING = "completing";
    private static final String FAILING = "failing";
    private static final String FAILING_IMPLICIT_ACK = "failing-implicit-ack";
    private static final String WRAPPED_INCOMING = "wrapped-incoming";
    private static final String WRAPPED_INCOMING_ACK = "wrapped-incoming-ack";
    private static final String WRAPPED_OUTGOING = "wrapped-outgoing";
    private static final String WRAPPED_OUTGOING_ACK = "wrapped-outgoing-ack";
    private static final String WRAPPED_INCOMING_OUTGOING = "wrapped-incoming-outgoing";
    private static final String WRAPPED_INCOMING_OUTGOING_ACK = "wrapped-incoming-outgoing-ack";

    @Topics
    public static String[] topics() {
        return new String[]{
            SIMPLE, COMPLETING, FAILING, FAILING_IMPLICIT_ACK, WRAPPED_INCOMING, WRAPPED_INCOMING_ACK, WRAPPED_OUTGOING,
            WRAPPED_OUTGOING_ACK, WRAPPED_INCOMING_OUTGOING, WRAPPED_INCOMING_OUTGOING_ACK
        };
    }


    @ArquillianResource
    private ContainerPuppet controller;
    @ArquillianResource
    private TestEnvironment environment;

    @Inject
    private TckMessagingManager manager;

    @Inject
    private Bean bean;

    public static final class Unit {
    }
    private static final Unit UNIT = new Unit();

    @ApplicationScoped
    public static class Bean {

        @Inject
        private TckMessagingManager manager;

        @Incoming(topic = SIMPLE)
        public ProcessorBuilder<MockPayload, Unit> handleSimple() {
            return manager.<MockPayload>getReceiver(SIMPLE).createProcessor(
                manager.<Unit>getSender(SIMPLE).createPublisher());
        }

        @Incoming(topic = COMPLETING)
        public ProcessorBuilder<MockPayload, Unit> handleCompleting() {
            return manager.<MockPayload>getReceiver(COMPLETING).createProcessor(
                manager.<Unit>getSender(COMPLETING).createPublisher());
        }

        @Incoming(topic = FAILING)
        public ProcessorBuilder<MockPayload, Unit> handleFailing() {
            return manager.<MockPayload>getReceiver(FAILING).createProcessor(
                manager.<Unit>getSender(FAILING).createPublisher());
        }

        @Incoming(topic = FAILING_IMPLICIT_ACK)
        public ProcessorBuilder<MockPayload, Unit> handleCompletingImplicitAck() {
            return manager.<MockPayload>getReceiver(FAILING_IMPLICIT_ACK).createProcessor(
                manager.<Unit>getSender(FAILING_IMPLICIT_ACK).createPublisher());
        }

        @Incoming(topic = WRAPPED_INCOMING)
        public ProcessorBuilder<Message<MockPayload>, Unit> handleWrappedIncoming() {
            return manager.<MockPayload>getReceiver(WRAPPED_INCOMING).createWrappedProcessor(
                manager.<Unit>getSender(WRAPPED_INCOMING).createPublisher());
        }

        @Incoming(topic = WRAPPED_INCOMING_ACK)
        public ProcessorBuilder<Message<MockPayload>, Unit> handleWrappedIncomingAck() {
            return manager.<MockPayload>getReceiver(WRAPPED_INCOMING_ACK).createWrappedProcessor(
                manager.<Unit>getSender(WRAPPED_INCOMING_ACK).createPublisher());
        }

        @Incoming(topic = WRAPPED_OUTGOING)
        public ProcessorBuilder<MockPayload, Message<Unit>> handleWrappedOutgoing() {
            return manager.<MockPayload>getReceiver(WRAPPED_OUTGOING).createProcessor(
                manager.<Unit>getSender(WRAPPED_OUTGOING).createWrappedPublisher());
        }

        @Incoming(topic = WRAPPED_OUTGOING_ACK)
        public ProcessorBuilder<MockPayload, Message<Unit>> handleWrappedOutgoingAck() {
            return manager.<MockPayload>getReceiver(WRAPPED_OUTGOING_ACK).createProcessor(
                manager.<Unit>getSender(WRAPPED_OUTGOING_ACK).createWrappedPublisher());
        }

        @Incoming(topic = WRAPPED_INCOMING_OUTGOING)
        public ProcessorBuilder<Message<MockPayload>, Message<Unit>> handleWrappedIncomingOutgoing() {
            return manager.<MockPayload>getReceiver(WRAPPED_INCOMING_OUTGOING).createWrappedProcessor(
                manager.<Unit>getSender(WRAPPED_INCOMING_OUTGOING).createWrappedPublisher());
        }

        @Incoming(topic = WRAPPED_INCOMING_OUTGOING_ACK)
        public ProcessorBuilder<Message<MockPayload>, Message<Unit>> handleWrappedIncomingOutgoingAck() {
            return manager.<MockPayload>getReceiver(WRAPPED_INCOMING_OUTGOING_ACK).createWrappedProcessor(
                manager.<Unit>getSender(WRAPPED_INCOMING_OUTGOING_ACK).createWrappedPublisher());
        }

    }

    @Test
    public void simpleProcessorBuilderMethodShouldHandleMessages() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(SIMPLE);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);
        MockPayload msg3 = new MockPayload("mock", 3);

        controller.sendPayloads(SIMPLE, msg1, msg2);

        receiver.expectNextMessageWithPayload(msg1);
        receiver.expectNextMessageWithPayload(msg2);
        receiver.expectNoMessages("Didn't expect a message because didn't send one.");
        controller.sendPayloads(SIMPLE, msg3);
        receiver.expectNextMessageWithPayload(msg3);
    }

    @Test
    public void simpleProcessorBuilderMethodShouldNotRestartWhenCompleted() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(COMPLETING);
        MockedSender<Unit> sender = manager.getSender(COMPLETING);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);
        controller.sendPayloads(COMPLETING, msg1);
        receiver.expectNextMessageWithPayload(msg1);
        receiver.cancelAll();
        sender.completeAll();
        controller.sendPayloads(COMPLETING, msg2);
        receiver.expectNoMessages("The receiver must have been restarted.");
    }


    @Test
    public void simpleProcessorBuilderMethodShouldRestartWhenFailed() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(FAILING);
        MockedSender<Unit> sender = manager.getSender(FAILING);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);
        controller.sendPayloads(FAILING, msg1);
        receiver.expectNextMessageWithPayload(msg1);
        sender.send(UNIT);
        receiver.cancelAll();
        sender.failAll(new QuietRuntimeException("failed"));

        controller.sendPayloads(FAILING, msg2);
        // We might receive msg1 again if the acknowledgement didn't arrive before the subscription was shut down.
        receiver.expectEventualMessageWithPayload(msg2);
    }

    @Test(groups = "ack")
    public void simpleProcessorBuilderShouldImplicitlyAckMessagesBasedOnOutput() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(FAILING_IMPLICIT_ACK);
        MockedSender<Unit> sender = manager.getSender(FAILING_IMPLICIT_ACK);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);

        controller.sendPayloads(FAILING_IMPLICIT_ACK, msg1, msg2);
        receiver.expectNextMessageWithPayload(msg1);
        receiver.expectNextMessageWithPayload(msg2);
        sender.send(UNIT);
        receiver.cancelAll();
        sender.failAll(new QuietRuntimeException("failed"));

        // msg2 should be received again because we didn't implicitly acknowledge it
        receiver.expectNextMessageWithPayload(msg2);
    }

    @Test
    public void wrappedIncomingProcessorMethodShouldWork() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_INCOMING);
        MockPayload msg1 = new MockPayload("mock", 1);

        controller.sendPayloads(WRAPPED_INCOMING, msg1);
        receiver.expectNextMessageWithPayload(msg1);
    }

    @Test(groups = "ack")
    public void wrappedIncomingProcessorMethodShouldOnlyAckWhenAckMethodInvoked() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_INCOMING_ACK);
        MockedSender<Unit> sender = manager.getSender(WRAPPED_INCOMING_ACK);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);

        controller.sendPayloads(WRAPPED_INCOMING_ACK, msg1, msg2);
        Message<MockPayload> rcvd1 = receiver.expectNextMessageWithPayload(msg1);
        receiver.expectNextMessageWithPayload(msg2);

        // Now emit two messages, these shouldn't cause them to ack
        sender.send(UNIT, UNIT);

        // And explicitly ack the first message, wait for it
        WaitAssert.await(rcvd1.ack(), environment);

        // Now if we fail and start back up, we should receive the second again
        receiver.cancelAll();
        sender.failAll(new QuietRuntimeException("failed"));

        receiver.expectNextMessageWithPayload(msg2);
    }

    @Test
    public void wrappedOutgoingProcessorMethodShouldHaveItsAckFunctionInvoked() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_OUTGOING);
        MockedSender<Unit> sender = manager.getSender(WRAPPED_OUTGOING);
        MockPayload msg1 = new MockPayload("mock", 1);

        controller.sendPayloads(WRAPPED_OUTGOING, msg1);
        receiver.expectNextMessageWithPayload(msg1);

        CompletableFuture<Void> acked = new CompletableFuture<>();
        sender.send(Message.of(UNIT, () -> {
            acked.complete(null);
            return acked;
        }));

        WaitAssert.await(acked, environment);
    }

    @Test(groups = "ack")
    public void wrappedOutgoingProcessorBuilderShouldImplicitlyAckMessagesBasedOnOutput() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_OUTGOING_ACK);
        MockedSender<Unit> sender = manager.getSender(WRAPPED_OUTGOING_ACK);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);

        controller.sendPayloads(WRAPPED_OUTGOING_ACK, msg1, msg2);
        receiver.expectNextMessageWithPayload(msg1);
        receiver.expectNextMessageWithPayload(msg2);
        sender.send(UNIT);
        receiver.cancelAll();
        sender.failAll(new QuietRuntimeException("failed"));

        // msg2 should be received again because we didn't implicitly acknowledge it
        receiver.expectNextMessageWithPayload(msg2);
    }


    @Test
    public void wrappedIncomingOutgoingProcessorMethodShouldWork() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_INCOMING_OUTGOING);
        MockPayload msg1 = new MockPayload("mock", 1);

        controller.sendPayloads(WRAPPED_INCOMING_OUTGOING, msg1);
        receiver.expectNextMessageWithPayload(msg1);
    }

    @Test(groups = "ack")
    public void wrappedIncomingOutgoingProcessorMethodShouldOnlyAckMessagesThroughTheOutgoingAckFunction() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_INCOMING_OUTGOING_ACK);
        MockedSender<Unit> sender = manager.getSender(WRAPPED_INCOMING_OUTGOING_ACK);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);

        controller.sendPayloads(WRAPPED_INCOMING_OUTGOING_ACK, msg1, msg2);
        Message<MockPayload> rcvd1 = receiver.expectNextMessageWithPayload(msg1);
        receiver.expectNextMessageWithPayload(msg2);

        // Now propagate the first ack function, but not the second
        CompletableFuture<Void> acked2 = new CompletableFuture<>();
        sender.send(Message.of(UNIT, () -> rcvd1.ack()), Message.of(UNIT, () -> {
            acked2.complete(null);
            return acked2;
        }));

        // Wait for the second to be acked
        WaitAssert.await(acked2, environment);

        // Now if we fail and start back up, we should receive the second again
        receiver.cancelAll();
        sender.failAll(new QuietRuntimeException("failed"));

        receiver.expectNextMessageWithPayload(msg2);
    }


}
