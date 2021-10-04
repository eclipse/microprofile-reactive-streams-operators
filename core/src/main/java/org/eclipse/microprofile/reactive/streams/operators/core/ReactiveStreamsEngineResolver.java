/*******************************************************************************
 * Copyright (c) 2018, 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.streams.operators.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ServiceLoader;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;

/**
 * This class is not intended to be used by end-users but for portable container integration purpose only.
 * <p>
 * Service provider for ReactiveStreamsEngine. The implementation registers itself via the {@link ServiceLoader}
 * mechanism.
 */
public class ReactiveStreamsEngineResolver {

    protected ReactiveStreamsEngineResolver() {
        // Avoid direct instantiation.
    }

    private static volatile ReactiveStreamsEngine instance = null;

    /**
     * Creates a ReactiveStreamsFactory object Only used internally from within {@link ReactiveStreams}
     *
     * @return ReactiveStreamsFactory an instance of ReactiveStreamsFactory
     */
    public static ReactiveStreamsEngine instance() {
        if (instance == null) {
            synchronized (ReactiveStreamsEngineResolver.class) {
                if (instance != null) {
                    return instance;
                }

                ClassLoader cl = AccessController.doPrivileged(
                        (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
                if (cl == null) {
                    cl = ReactiveStreamsEngine.class.getClassLoader();
                }

                ReactiveStreamsEngine newInstance = loadFromSpi(cl);

                if (newInstance == null) {
                    throw new IllegalStateException(
                            "No ReactiveStreamsEngine implementation found!");
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    private static ReactiveStreamsEngine loadFromSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }
        if (instance == null) {
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                    ServiceLoader<ReactiveStreamsEngine> sl = ServiceLoader.load(
                            ReactiveStreamsEngine.class, cl);
                    for (ReactiveStreamsEngine spi : sl) {
                        if (instance != null) {
                            throw new IllegalStateException(
                                    "Multiple ReactiveStreamsEngine implementations found: "
                                            + spi.getClass().getName() + " and "
                                            + instance.getClass().getName());
                        } else {
                            instance = spi;
                        }
                    }
                    return null;
                });
            } catch (PrivilegedActionException e) {
                Throwable t = e.getCause();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                if (t instanceof Error) {
                    throw (Error) t;
                }
                throw new RuntimeException(t);
            }
        }
        return instance;
    }

    /**
     * Set the instance. It is used by OSGi environment while service loader pattern is not supported.
     *
     * @param factory
     *            set the instance.
     */
    public static void setInstance(ReactiveStreamsEngine factory) {
        instance = factory;
    }

}
