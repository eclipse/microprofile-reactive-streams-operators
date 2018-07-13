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

package org.eclipse.microprofile.reactive.messaging.tck.spi;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;

import java.util.List;

/**
 * Containers being tested by this TCK must provide an implementation of this.
 * <p>
 * It is used to supply configuration about the TCK, including hints about capabilities of both the
 * implementation and the messaging provider.
 * <p>
 * It's also used to tell the container what queues need to be configured, and to allow the TCK to place
 * messages directly on these queues and to read messages from them.
 * <p>
 * A container may implement multiple of these to run the test in multiple configurations, for example,
 * if the container supports multiple different messaging providers, it may implement one per messaging
 * provider.
 * <p>
 * Implementations of this should be provided using the JDK ServiceLoader mechanism.
 */
public interface TckContainer {

    /**
     * The test environment.
     */
    default TestEnvironment testEnvironment() {
        return TestEnvironment.DEFAULT;
    }

    /**
     * Whether the messaging provider being used supports incoming messages.
     */
    default boolean supportsIncoming() {
        return true;
    }

    /**
     * Whether the messaging provider being used supports outgoing messages.
     */
    default boolean supportsOutgoing() {
        return true;
    }

    /**
     * Whether this container requires the archive deployments that it returns to be merged into the tests deployment.
     * <p>
     * This is necessary for running the TCK on Weld SE, which doesn't support multiple deployments, so instead requires a single archive to be
     * deployed that contains all the classes.
     */
    default boolean mergeArchives() {
        return false;
    }

    /**
     * Create some deployments for the given topics.
     * <p>
     * One of the deployments must be an archive that provides an {@link javax.enterprise.context.ApplicationScoped} instance of
     * {@link TckMessagingPuppet} for injection.
     * <p>
     * Implementations may return one deployment per topic, or one deployment for all topics, or whatever they want, as long as the tests are able to
     * send/receive on the topics specified.
     * <p>
     * This method must ensure that if the topics need to be created or reset, that they are, so that any tests will start with a blank slate.
     * Additionally, the TckMessagingPuppet must be able to send/receive on the specified topics.
     *
     * @param topics The topics to create and create the deployments for.
     * @return The deployments.
     */
    List<DeploymentDescription> createDeployments(String... topics);

    /**
     * Tear down the given topics.
     * <p>
     * This may mean deleting them. Note that {@link #createDeployments(String...)} should reset the topics if they exist, so implementing this isn't
     * strictly necessary, but is hygienic.
     *
     * @param topics The topics to tear down.
     */
    void teardownTopics(String... topics);

}
