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
import org.eclipse.microprofile.reactive.messaging.tck.arquillian.Topics;
import org.eclipse.microprofile.reactive.messaging.tck.mocks.MockPayload;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Topics({
    AsyncIncomingMethodVerification.SIMPLE_COMPLETION_STAGE_VOID_METHOD,
    AsyncIncomingMethodVerification.COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL
})
public class AsyncIncomingMethodVerification extends Arquillian {

  @Deployment
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(Bean.class)
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @Inject
  private TckMessagingManager manager;
  @Inject
  private ContainerController controller;

  @Inject
  private Bean bean;

  static final String SIMPLE_COMPLETION_STAGE_VOID_METHOD = "simple-completion-stage-void-method";
  static final String COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL = "completion-stage-void-method-non-parallel";

  @ApplicationScoped
  public static class Bean {

    @Inject
    private TckMessagingManager manager;

    @Incoming(topic = SIMPLE_COMPLETION_STAGE_VOID_METHOD)
    public CompletionStage<Void> handleSimpleCompletionStageVoidMethod(MockPayload payload) {
      manager.getReceiver(SIMPLE_COMPLETION_STAGE_VOID_METHOD).receiveMessage(payload);
      return CompletableFuture.completedFuture(null);
    }

    private final Deque<CompletableFuture<Void>> futures = new ArrayDeque<>();

    public Deque<CompletableFuture<Void>> getFutures() {
      return futures;
    }

    @Incoming(topic = COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL)
    public CompletionStage<Void> handleCompletionStageVoidMethodNonParallel(MockPayload payload) {
      manager.getReceiver(COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL).receiveMessage(payload);
      CompletableFuture<Void> future = new CompletableFuture<>();
      futures.add(future);
      return future;
    }
  }

  @Test
  public void simpleCompletionStageVoidMethod() {
    MockedReceiver<MockPayload> receiver = manager.getReceiver(SIMPLE_COMPLETION_STAGE_VOID_METHOD);
    MockPayload msg1 = new MockPayload("mock", 1);
    MockPayload msg2 = new MockPayload("mock", 2);
    MockPayload msg3 = new MockPayload("mock", 3);

    controller.sendPayloads(SIMPLE_COMPLETION_STAGE_VOID_METHOD, msg1, msg2);

    receiver.expectNextMessageWithPayload(msg1);
    receiver.expectNextMessageWithPayload(msg2);
    receiver.expectNoMessages("Didn't expect a message because didn't send one.");
    controller.sendPayloads(SIMPLE_COMPLETION_STAGE_VOID_METHOD, msg3);
    receiver.expectNextMessageWithPayload(msg3);
  }

  @Test
  public void completionStageVoidMethodShouldNotProcessMessagesInParallel() {
    MockedReceiver<MockPayload> receiver = manager.getReceiver(COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL);
    MockPayload msg1 = new MockPayload("mock", 1);
    MockPayload msg2 = new MockPayload("mock", 2);
    MockPayload msg3 = new MockPayload("mock", 3);

    controller.sendPayloads(COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL, msg1, msg2);

    receiver.expectNextMessageWithPayload(msg1);
    receiver.expectNoMessages("Did not redeem future from previous message yet.");
    bean.getFutures().removeFirst().complete(null);
    receiver.expectNextMessageWithPayload(msg2);
    receiver.expectNoMessages("Did not redeem future from previous message yet.");
    bean.getFutures().removeFirst().complete(null);
    controller.sendPayloads(COMPLETION_STAGE_VOID_METHOD_NON_PARALLEL, msg3);
    receiver.expectNextMessageWithPayload(msg3);
    bean.getFutures().removeFirst().complete(null);
  }

}
