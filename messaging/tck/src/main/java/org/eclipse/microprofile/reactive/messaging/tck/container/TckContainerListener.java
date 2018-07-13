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

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class TckContainerListener {

    @ApplicationScoped
    @Inject
    private InstanceProducer<ContainerPuppet> containerPuppet;

    @ApplicationScoped
    @Inject
    private InstanceProducer<TckMessagingPuppet> tckMessagingPuppet;

    @ApplicationScoped
    @Inject
    private InstanceProducer<TestEnvironment> testEnvironment;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<Injector> injector;

    public void supplyComponents(@Observes BeforeSuite event) {
        TckMessagingPuppet messagingPuppet = serviceLoader.get().onlyOne(TckMessagingPuppet.class);
        if (messagingPuppet == null) {
            throw new IllegalStateException("No messaging puppet service has been provided. To provide one, you must provide one using a " +
                RemoteLoadableExtension.class + " which must be provided to the remote container by a " +
                "org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender");
        }
        tckMessagingPuppet.set(messagingPuppet);
        containerPuppet.set(injector.get().inject(new ContainerPuppet()));
        testEnvironment.set(messagingPuppet.testEnvironment());
    }
}
