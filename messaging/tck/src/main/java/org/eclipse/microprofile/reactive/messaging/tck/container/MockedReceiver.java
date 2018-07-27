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
import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Convenience helper for holding received messages so that assertions can be run against them.
 */
public class MockedReceiver<T> {
    private final TestEnvironment testEnvironment;
    private final String topic;
    private final BlockingDeque<Message<T>> queue = new LinkedBlockingDeque<>();
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public MockedReceiver(TestEnvironment testEnvironment, String topic) {
        this.testEnvironment = testEnvironment;
        this.topic = topic;
    }

    public int numSubscriptions() {
        return subscriptions.size();
    }

    public <R> ProcessorBuilder<Message<T>, R> createWrappedProcessor(PublisherBuilder<R> publisher) {
        return ReactiveStreams.fromProcessor(new MessageProcessor(publisher.buildRs()));
    }

    public <R> ProcessorBuilder<T, R> createProcessor(PublisherBuilder<R> publisher) {
        return ReactiveStreams.<T>builder()
            .<Message<T>>map(SimpleMessage::new)
            .via(new MessageProcessor<>(publisher.buildRs()));
    }

    public SubscriberBuilder<Message<T>, Void> createWrappedSubscriber() {
        return ReactiveStreams.fromSubscriber(new MessageProcessor(null));
    }

    public SubscriberBuilder<T, Void> createSubscriber() {
        return ReactiveStreams.<T>builder()
            .<Message<T>>map(SimpleMessage::new)
            .to(new MessageProcessor(null));
    }

    /**
     * Cancel all subscriptions.
     */
    public void cancelAll() {
        List<Subscription> subscriptions = new ArrayList<>(this.subscriptions);
        this.subscriptions.clear();
        subscriptions.forEach(Subscription::cancel);
    }

    /**
     * Expect the next message to have the given payload.
     */
    public Message<T> expectNextMessageWithPayload(T payload) {
        Message<T> msg = receiveMessageWithPayload(payload, testEnvironment.receiveTimeout().toMillis());
        if (!msg.getPayload().equals(payload)) {
            throw new AssertionError("Expected a message on topic " + topic + " with payload " + payload + " but got " + msg);
        }
        return msg;
    }

    /**
     * Expect a message to eventually arrive with the given payload.
     * <p>
     * Any messages not matching the payload will be acknowledged and ignored.
     */
    public Message<T> expectEventualMessageWithPayload(T payload) {
        long start = System.currentTimeMillis();
        Message<T> msg = receiveMessageWithPayload(payload, testEnvironment.receiveTimeout().toMillis());
        List<String> ignored = new ArrayList<>();
        try {
            while (!msg.getPayload().equals(payload)) {
                ignored.add(msg.toString());
                msg.ack();
                long remaining = testEnvironment.receiveTimeout().toMillis() - (System.currentTimeMillis() - start);
                msg = receiveMessageWithPayload(payload, remaining);
            }
            return msg;
        }
        catch (ReceiveTimeoutException e) {
            throw new AssertionError(e.getMessage() + ". Did receive and ignore the following payloads: " + String.join(", ", ignored));
        }
    }

    /**
     * Expect no more messages.
     */
    public void expectNoMessages(String errorMsg) {
        try {
            Message<T> msg = queue.poll(testEnvironment.noMessageTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (msg != null) {
                throw new AssertionError(errorMsg + ": Expected no messages on topic " + topic + " but instead got message: " + msg);
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage(T payload) {
        receiveWrappedMessage(new SimpleMessage<>(payload));
    }

    public void receiveWrappedMessage(Message<T> msg) {
        queue.add(msg);
    }

    private Message<T> receiveMessageWithPayload(Object payload, long remaining) {
        Message<T> msg = null;
        if (remaining > 0) {
            try {
                msg = queue.poll(remaining, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (msg == null) {
            throw new ReceiveTimeoutException("Timeout " + testEnvironment.receiveTimeout().toMillis() +
                "ms while waiting for a message on topic " + topic + " with payload " + payload);
        }
        return msg;
    }

    private class MessageProcessor<R> implements Processor<Message<T>, R> {
        private final Publisher<R> publisher;
        private volatile AtomicReference<Subscription> subscription = new AtomicReference<>();

        public MessageProcessor(Publisher<R> publisher) {
            this.publisher = publisher;
        }

        @Override
        public void subscribe(Subscriber<? super R> subscriber) {
            publisher.subscribe(subscriber);
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            if (!this.subscription.compareAndSet(null, subscription)) {
                subscription.cancel();
            }
            else {
                subscriptions.add(subscription);
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(Message<T> message) {
            receiveWrappedMessage(message);
        }

        @Override
        public void onError(Throwable throwable) {
            subscriptions.remove(subscription.get());
        }

        @Override
        public void onComplete() {
            subscriptions.remove(subscription.get());
        }
    }
}
