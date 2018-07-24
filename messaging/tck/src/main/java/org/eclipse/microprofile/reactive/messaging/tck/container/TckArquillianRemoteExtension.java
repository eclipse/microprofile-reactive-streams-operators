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
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class TckArquillianRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(ResourceProvider.class, ContainerControllerResourceProvider.class)
            .service(ResourceProvider.class, TckMessagingPuppetResourceProvider.class)
            .service(ResourceProvider.class, TestEnvironmentResourceProvider.class)
            .observer(TckContainerListener.class);
    }

    public static class ContainerControllerResourceProvider implements ResourceProvider {

        @Inject
        private Instance<ContainerPuppet> containerController;

        @Override
        public boolean canProvide(Class<?> type) {
            return type.equals(ContainerPuppet.class);
        }

        @Override
        public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
            return containerController.get();
        }
    }

    public static class TckMessagingPuppetResourceProvider implements ResourceProvider {

        @Inject
        private Instance<TckMessagingPuppet> tckMessagingPuppet;

        @Override
        public boolean canProvide(Class<?> type) {
            return type.equals(TckMessagingPuppet.class);
        }

        @Override
        public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
            return tckMessagingPuppet.get();
        }
    }

    public static class TestEnvironmentResourceProvider implements ResourceProvider {

        @Inject
        private Instance<TestEnvironment> testEnvironment;

        @Override
        public boolean canProvide(Class<?> type) {
            return type.equals(TestEnvironment.class);
        }

        @Override
        public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
            return testEnvironment.get();
        }
    }

}
