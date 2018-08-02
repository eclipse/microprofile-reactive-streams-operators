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
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.messaging.tck.container.ContainerPuppet;
import org.eclipse.microprofile.reactive.messaging.tck.container.MockPayload;
import org.eclipse.microprofile.reactive.messaging.tck.container.MockedReceiver;
import org.eclipse.microprofile.reactive.messaging.tck.container.QuietRuntimeException;
import org.eclipse.microprofile.reactive.messaging.tck.container.TckDeploymentUtils;
import org.eclipse.microprofile.reactive.messaging.tck.container.TestEnvironment;
import org.eclipse.microprofile.reactive.messaging.tck.container.Topics;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.microprofile.reactive.messaging.tck.container.WaitAssert.waitUntil;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Provides tests for methods of the shape:
 *
 * <pre>
 * &#064;Incoming
 * &#064;Outgoing
 * public CompletionStage&gt;SomeMessage&lt; handle(SomeMessage message) {
 *   ...
 * }
 * </pre>
 */
@Test(groups = {"all", "incoming", "outgoing"})
public class IncomingOutgingCompletionStageMethodVerification extends Arquillian {

    @Deployment
    public static JavaArchive createDeployment() {
        return TckDeploymentUtils.createDeployment(Bean.class);
    }

    private static final String SIMPLE_IN = "simple-in";
    private static final String SIMPLE_OUT = "simple-out";
    private static final String NON_PARALLEL_IN = "non-parallel-in";
    private static final String NON_PARALLEL_OUT = "non-parallel-out";
    private static final String SYNC_FAILING_IN = "sync-failing-in";
    private static final String SYNC_FAILING_OUT = "sync-failing-out";
    private static final String ASYNC_FAILING_IN = "async-failing-in";
    private static final String ASYNC_FAILING_OUT = "async-failing-out";
    private static final String WRAPPED_MESSAGE_IN = "wrapped-message-in";
    private static final String WRAPPED_MESSAGE_OUT = "wrapped-message-out";
    private static final String OUTGOING_WRAPPED_IN = "outgoing-wrapped-in";
    private static final String OUTGOING_WRAPPED_OUT = "outgoing-wrapped-out";
    private static final String INCOMING_OUTGOING_WRAPPED_IN = "incoming-outgoing-wrapped-in";
    private static final String INCOMING_OUTGOING_WRAPPED_OUT = "incoming-outgoing-wrapped-out";

    @Topics
    public static String[] topics() {
        return new String[]{SIMPLE_IN, SIMPLE_OUT, NON_PARALLEL_IN, NON_PARALLEL_OUT, SYNC_FAILING_IN, SYNC_FAILING_OUT,
            ASYNC_FAILING_IN, ASYNC_FAILING_OUT, WRAPPED_MESSAGE_IN, WRAPPED_MESSAGE_OUT, OUTGOING_WRAPPED_IN,
            OUTGOING_WRAPPED_OUT, INCOMING_OUTGOING_WRAPPED_IN, INCOMING_OUTGOING_WRAPPED_OUT
        };
    }

    @ArquillianResource
    private ContainerPuppet puppet;
    @ArquillianResource
    private TestEnvironment environment;

    @Inject
    private TckMessagingManager manager;
    @Inject
    private Bean bean;


    @ApplicationScoped
    public static class Bean {

        @Inject
        private TckMessagingManager manager;

        @Incoming(topic = SIMPLE_IN)
        @Outgoing(topic = SIMPLE_OUT)
        public CompletionStage<MockPayload> handleVoidMethod(MockPayload payload) {
            manager.getReceiver(SIMPLE_IN).receiveMessage(payload);
            return CompletableFuture.completedFuture(payload.transform());
        }

        private final Deque<CompletableFuture<MockPayload>> futures = new ArrayDeque<>();

        public Deque<CompletableFuture<MockPayload>> getFutures() {
            return futures;
        }

        @Incoming(topic = NON_PARALLEL_IN)
        @Outgoing(topic = NON_PARALLEL_OUT)
        public CompletionStage<MockPayload> handleNonParallel(MockPayload payload) {
            manager.getReceiver(NON_PARALLEL_IN).receiveMessage(payload);
            CompletableFuture<MockPayload> future = new CompletableFuture<>();
            futures.add(future);
            return future;
        }

        private final AtomicBoolean syncFailed = new AtomicBoolean();

        public AtomicBoolean getSyncFailed() {
            return syncFailed;
        }

        @Incoming(topic = SYNC_FAILING_IN)
        @Outgoing(topic = SYNC_FAILING_OUT)
        public CompletionStage<MockPayload> handleSyncFailing(MockPayload payload) {
            if (payload.getField1().equals("fail") && !syncFailed.getAndSet(true)) {
                manager.getReceiver(SYNC_FAILING_IN).receiveMessage(payload);
                throw new QuietRuntimeException("failed");
            }
            else {
                manager.getReceiver(SYNC_FAILING_IN).receiveMessage(payload);
                return CompletableFuture.completedFuture(payload.transform());
            }
        }

        private final AtomicBoolean asyncFailed = new AtomicBoolean();

        public AtomicBoolean getAsyncFailed() {
            return asyncFailed;
        }


        @Incoming(topic = ASYNC_FAILING_IN)
        @Outgoing(topic = ASYNC_FAILING_OUT)
        public CompletionStage<MockPayload> handleAsyncFailing(MockPayload payload) {
            if (payload.getField1().equals("fail") && !asyncFailed.getAndSet(true)) {
                manager.getReceiver(ASYNC_FAILING_IN).receiveMessage(payload);
                CompletableFuture<MockPayload> future = new CompletableFuture<>();
                future.completeExceptionally(new QuietRuntimeException("failed"));
                return future;
            }
            else {
                manager.getReceiver(ASYNC_FAILING_IN).receiveMessage(payload);
                return CompletableFuture.completedFuture(payload.transform());
            }
        }

        private final AtomicBoolean wrappedFailed = new AtomicBoolean();

        public AtomicBoolean getWrappedFailed() {
            return wrappedFailed;
        }

        @Incoming(topic = WRAPPED_MESSAGE_IN)
        @Outgoing(topic = WRAPPED_MESSAGE_OUT)
        public CompletionStage<MockPayload> handleWrapped(Message<MockPayload> msg) {
            if (msg.getPayload().getField1().equals("acknowledged") || wrappedFailed.get()) {
                manager.<MockPayload>getReceiver(WRAPPED_MESSAGE_IN).receiveWrappedMessage(msg);
                return msg.ack().thenApply(v -> msg.getPayload().transform());
            }
            else if (msg.getPayload().getField1().equals("fail")) {
                wrappedFailed.set(true);
                manager.<MockPayload>getReceiver(WRAPPED_MESSAGE_IN).receiveWrappedMessage(msg);
                throw new QuietRuntimeException("failed");
            }
            else {
                manager.<MockPayload>getReceiver(WRAPPED_MESSAGE_IN).receiveWrappedMessage(msg);
                return CompletableFuture.completedFuture(msg.getPayload().transform());
            }
        }

        private final AtomicInteger acked = new AtomicInteger();
        private final AtomicBoolean outgoingWrappedFailed = new AtomicBoolean();

        public AtomicInteger getAcked() {
            return acked;
        }

        public AtomicBoolean getOutgoingWrappedFailed() {
            return outgoingWrappedFailed;
        }

        @Incoming(topic = OUTGOING_WRAPPED_IN)
        @Outgoing(topic = OUTGOING_WRAPPED_OUT)
        public CompletionStage<Message<MockPayload>> handleOutgoingWrapped(MockPayload msg) {
            if (msg.getField1().equals("fail") && !outgoingWrappedFailed.getAndSet(true)) {
                manager.getReceiver(OUTGOING_WRAPPED_IN).receiveMessage(msg);
                throw new QuietRuntimeException("failed");
            }
            else {
                manager.getReceiver(OUTGOING_WRAPPED_IN).receiveMessage(msg);
                return CompletableFuture.completedFuture(Message.of(msg.transform(), () -> {
                    acked.incrementAndGet();
                    return CompletableFuture.completedFuture(null);
                }));
            }
        }

        private final AtomicBoolean incomingOutgoingWrappedFailed = new AtomicBoolean();

        public AtomicBoolean getIncomingOutgoingWrappedFailed() {
            return incomingOutgoingWrappedFailed;
        }

        // This is used to ensure that before we fail the stream, the first message was successfully acked.
        private final CompletableFuture<Void> incomingOutgoingWrappedAcked = new CompletableFuture<>();

        @Incoming(topic = INCOMING_OUTGOING_WRAPPED_IN)
        @Outgoing(topic = INCOMING_OUTGOING_WRAPPED_OUT)
        public CompletionStage<Message<MockPayload>> handleIncomingOutgoingWrapped(Message<MockPayload> msg) {
            if (msg.getPayload().getField1().equals("acknowledged") || incomingOutgoingWrappedFailed.get()) {
                manager.<MockPayload>getReceiver(INCOMING_OUTGOING_WRAPPED_IN).receiveWrappedMessage(msg);
                return CompletableFuture.completedFuture(Message.of(msg.getPayload().transform(), () ->
                    msg.ack().thenAccept(incomingOutgoingWrappedAcked::complete)
                ));
            }
            else if (msg.getPayload().getField1().equals("fail")) {
                incomingOutgoingWrappedFailed.set(true);
                manager.<MockPayload>getReceiver(INCOMING_OUTGOING_WRAPPED_IN).receiveWrappedMessage(msg);
                return incomingOutgoingWrappedAcked.thenApply(v -> {
                    throw new QuietRuntimeException("failed");
                });
            }
            else {
                manager.<MockPayload>getReceiver(INCOMING_OUTGOING_WRAPPED_IN).receiveWrappedMessage(msg);
                // Note - not transferring the ack.
                return CompletableFuture.completedFuture(Message.of(msg.getPayload().transform()));
            }
        }
    }

    @Test
    public void simpleCompletionStageVoidMethodShouldProcessMessages() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(SIMPLE_IN);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);
        MockPayload msg3 = new MockPayload("mock", 3);

        puppet.sendPayloads(SIMPLE_IN, msg1, msg2);

        receiver.expectNextMessageWithPayload(msg1);
        puppet.expectNextMessageWithPayload(SIMPLE_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(SIMPLE_OUT, msg2.transform());
        receiver.expectNoMessages("Didn't expect a message because didn't send one.");
        puppet.expectNoMessages(SIMPLE_OUT);

        puppet.sendPayloads(SIMPLE_IN, msg3);
        receiver.expectNextMessageWithPayload(msg3);
        puppet.expectNextMessageWithPayload(SIMPLE_OUT, msg3.transform());
    }

    @Test
    public void completionStageMethodShouldNotProcessMessagesInParallel() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(NON_PARALLEL_IN);
        MockPayload msg1 = new MockPayload("mock", 1);
        MockPayload msg2 = new MockPayload("mock", 2);
        MockPayload msg3 = new MockPayload("mock", 3);

        puppet.sendPayloads(NON_PARALLEL_IN, msg1, msg2);

        receiver.expectNextMessageWithPayload(msg1);
        receiver.expectNoMessages("Did not redeem future from previous message yet.");
        bean.getFutures().removeFirst().complete(msg1.transform());
        puppet.expectNextMessageWithPayload(NON_PARALLEL_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        receiver.expectNoMessages("Did not redeem future from previous message yet.");
        bean.getFutures().removeFirst().complete(msg2.transform());
        puppet.expectNextMessageWithPayload(NON_PARALLEL_OUT, msg2.transform());
        puppet.sendPayloads(NON_PARALLEL_IN, msg3);
        receiver.expectNextMessageWithPayload(msg3);
        bean.getFutures().removeFirst().complete(msg3.transform());
        puppet.expectNextMessageWithPayload(NON_PARALLEL_OUT, msg3.transform());
    }

    @Test(groups = {"ack"})
    public void completionStageMethodShouldRetryMessagesThatFailSynchronously() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(SYNC_FAILING_IN);
        MockPayload msg1 = new MockPayload("fail", 1);
        MockPayload msg2 = new MockPayload("success", 2);

        puppet.sendPayloads(SYNC_FAILING_IN, msg1, msg2);
        // We should receive the fail message once, then failed should be true, then we should receive it again,
        // followed by the next message.
        receiver.expectNextMessageWithPayload(msg1);
        assertTrue(bean.getSyncFailed().get(), "Sync was not failed");
        receiver.expectNextMessageWithPayload(msg1);
        puppet.expectNextMessageWithPayload(SYNC_FAILING_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(SYNC_FAILING_OUT, msg2.transform());
    }

    @Test(groups = {"ack"})
    public void completionStageMethodShouldRetryMessagesThatFailAsynchronously() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(ASYNC_FAILING_IN);
        MockPayload msg1 = new MockPayload("fail", 1);
        MockPayload msg2 = new MockPayload("success", 2);

        puppet.sendPayloads(ASYNC_FAILING_IN, msg1, msg2);
        // We should receive the fail message once, then failed should be true, then we should receive it again,
        // followed by the next message.
        receiver.expectNextMessageWithPayload(msg1);
        assertTrue(bean.getAsyncFailed().get(), "Async was not failed");
        receiver.expectNextMessageWithPayload(msg1);
        puppet.expectNextMessageWithPayload(ASYNC_FAILING_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(ASYNC_FAILING_OUT, msg2.transform());
    }

    @Test(groups = {"ack"})
    public void completionStageMethodShouldNotAutomaticallyAcknowledgeWrappedMessages() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(WRAPPED_MESSAGE_IN);
        MockPayload msg1 = new MockPayload("acknowledged", 1);
        MockPayload msg2 = new MockPayload("unacknowledged", 2);
        MockPayload msg3 = new MockPayload("fail", 3);
        MockPayload msg4 = new MockPayload("success", 4);

        puppet.sendPayloads(WRAPPED_MESSAGE_IN, msg1, msg2, msg3, msg4);
        // First one should be acknwoldeged. The second one gets processed successfully, but first time, doesn't ack.
        // Third one fails on the first attempt, on the second acks. Fourth one always acks.
        receiver.expectNextMessageWithPayload(msg1);
        puppet.expectNextMessageWithPayload(WRAPPED_MESSAGE_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(WRAPPED_MESSAGE_OUT, msg2.transform());
        receiver.expectNextMessageWithPayload(msg3);
        assertTrue(bean.getWrappedFailed().get(), "Wrapped was not failed");
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(WRAPPED_MESSAGE_OUT, msg2.transform());
        receiver.expectNextMessageWithPayload(msg3);
        puppet.expectNextMessageWithPayload(WRAPPED_MESSAGE_OUT, msg3.transform());
        receiver.expectNextMessageWithPayload(msg4);
        puppet.expectNextMessageWithPayload(WRAPPED_MESSAGE_OUT, msg4.transform());
    }

    @Test(groups = {"ack"})
    public void completionStageWithOutgoingWrappedMessageShouldGetAcknowledged() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(OUTGOING_WRAPPED_IN);
        MockPayload msg1 = new MockPayload("fail", 1);
        MockPayload msg2 = new MockPayload("success", 2);

        puppet.sendPayloads(OUTGOING_WRAPPED_IN, msg1, msg2);
        receiver.expectNextMessageWithPayload(msg1);
        assertTrue(bean.getOutgoingWrappedFailed().get(), "Outgoing wrapped was not failed");

        receiver.expectNextMessageWithPayload(msg1);
        puppet.expectNextMessageWithPayload(OUTGOING_WRAPPED_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(OUTGOING_WRAPPED_OUT, msg2.transform());
        waitUntil(environment.receiveTimeout(), () -> assertEquals(bean.getAcked().get(), 2));
    }

    @Test(groups = {"ack"})
    public void completionStageMethodShouldAcknowldegePassedThroughAckFunctions() {
        MockedReceiver<MockPayload> receiver = manager.getReceiver(INCOMING_OUTGOING_WRAPPED_IN);
        MockPayload msg1 = new MockPayload("acknowledged", 1);
        MockPayload msg2 = new MockPayload("unacknowledged", 2);
        MockPayload msg3 = new MockPayload("fail", 3);
        MockPayload msg4 = new MockPayload("success", 4);

        puppet.sendPayloads(INCOMING_OUTGOING_WRAPPED_IN, msg1, msg2, msg3, msg4);
        receiver.expectNextMessageWithPayload(msg1);
        puppet.expectNextMessageWithPayload(INCOMING_OUTGOING_WRAPPED_OUT, msg1.transform());
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(INCOMING_OUTGOING_WRAPPED_OUT, msg2.transform());
        receiver.expectNextMessageWithPayload(msg3);
        assertTrue(bean.getIncomingOutgoingWrappedFailed().get(), "Wrapped was not failed");
        receiver.expectNextMessageWithPayload(msg2);
        puppet.expectNextMessageWithPayload(INCOMING_OUTGOING_WRAPPED_OUT, msg2.transform());
        receiver.expectNextMessageWithPayload(msg3);
        puppet.expectNextMessageWithPayload(INCOMING_OUTGOING_WRAPPED_OUT, msg3.transform());
        receiver.expectNextMessageWithPayload(msg4);
        puppet.expectNextMessageWithPayload(INCOMING_OUTGOING_WRAPPED_OUT, msg4.transform());
    }
}
