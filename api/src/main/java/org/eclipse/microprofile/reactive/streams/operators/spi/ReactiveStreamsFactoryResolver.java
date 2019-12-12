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
package org.eclipse.microprofile.reactive.streams.operators.spi;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreamsFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;

/**
 * This class is not intended to be used by end-users but for
 * portable container integration purpose only.
 * <p>
 * Service provider for ReactiveStreamsFactory. The implementation registers
 * itself via the {@link java.util.ServiceLoader} mechanism.
 */
public class ReactiveStreamsFactoryResolver {

    protected ReactiveStreamsFactoryResolver() {
        // Avoid direct instantiation.
    }

    private static volatile ReactiveStreamsFactory instance = null;

    /**
     * Creates a ReactiveStreamsFactory object
     * Only used internally from within {@link ReactiveStreams}
     *
     * @return ReactiveStreamsFactory an instance of ReactiveStreamsFactory
     */
    public static ReactiveStreamsFactory instance() {
        if (instance == null) {
            synchronized (ReactiveStreamsFactoryResolver.class) {
                if (instance != null) {
                    return instance;
                }

                ClassLoader cl = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
                if (cl == null) {
                    cl = ReactiveStreamsFactory.class.getClassLoader();
                }

                ReactiveStreamsFactory newInstance = loadFromSpi(cl);

                if (newInstance == null) {
                    throw new IllegalStateException(
                        "No ReactiveStreamsFactory implementation found!");
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    private static ReactiveStreamsFactory loadFromSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        if (instance == null) {
            ServiceLoader<ReactiveStreamsFactory> sl = ServiceLoader.load(
                ReactiveStreamsFactory.class, cl);
            for (ReactiveStreamsFactory spi : sl) {
                if (instance != null) {
                    throw new IllegalStateException(
                        "Multiple ReactiveStreamsFactory implementations found: "
                            + spi.getClass().getName() + " and "
                            + instance.getClass().getName());
                }
                else {
                    instance = spi;
                }
            }
        }
        return instance;
    }

    /**
     * Set the instance. It is used by OSGi environment while service loader
     * pattern is not supported.
     *
     * @param factory set the instance.
     */
    public static void setInstance(ReactiveStreamsFactory factory) {
        instance = factory;
    }

}
