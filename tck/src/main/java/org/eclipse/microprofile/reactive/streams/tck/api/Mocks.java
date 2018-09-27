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

package org.eclipse.microprofile.reactive.streams.tck.api;

import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.ToGraphable;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Shared mock objects
 */
class Mocks {

    private Mocks() {
    }

    static final Graph EMPTY_PUBLISHER_GRAPH = new Graph() {
        @Override
        public Collection<Stage> getStages() {
            return Collections.singleton(new Stage.Of() {
                @Override
                public Iterable<?> getElements() {
                    return Collections.emptyList();
                }

                @Override
                public boolean hasOutlet() {
                    return true;
                }
            });
        }

        @Override
        public boolean hasInlet() {
            return false;
        }

        @Override
        public boolean hasOutlet() {
            return true;
        }
    };

    static final Graph SUBSCRIBER_GRAPH = new Graph() {
        @Override
        public Collection<Stage> getStages() {
            return Arrays.asList(
                new Stage.Distinct() {
                    @Override
                    public boolean hasOutlet() {
                        return true;
                    }

                    @Override
                    public boolean hasInlet() {
                        return true;
                    }
                },
                new Stage.Cancel() {
                    @Override
                    public boolean hasOutlet() {
                        return false;
                    }

                    @Override
                    public boolean hasInlet() {
                        return true;
                    }
                }
            );
        }

        @Override
        public boolean hasInlet() {
            return true;
        }

        @Override
        public boolean hasOutlet() {
            return false;
        }
    };

    static final Graph PROCESSOR_GRAPH = new Graph() {
        @Override
        public Collection<Stage> getStages() {
            return Arrays.asList(
                new Stage.Distinct() {
                    @Override
                    public boolean hasOutlet() {
                        return true;
                    }

                    @Override
                    public boolean hasInlet() {
                        return true;
                    }
                },
                new Stage.Limit() {
                    @Override
                    public long getLimit() {
                        return 5;
                    }

                    @Override
                    public boolean hasOutlet() {
                        return true;
                    }

                    @Override
                    public boolean hasInlet() {
                        return true;
                    }
                }
            );
        }

        @Override
        public boolean hasInlet() {
            return true;
        }

        @Override
        public boolean hasOutlet() {
            return true;
        }
    };

    static final Subscriber SUBSCRIBER = new Subscriber() {
        @Override
        public void onSubscribe(Subscription s) {
        }

        @Override
        public void onNext(Object o) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    };

    static final Publisher PUBLISHER = s -> {
    };

    static final Processor PROCESSOR = new Processor() {
        @Override
        public void subscribe(Subscriber s) {
        }

        @Override
        public void onSubscribe(Subscription s) {
        }

        @Override
        public void onNext(Object o) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    };

    static final Subscription SUBSCRIPTION = new Subscription() {
        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }
    };

    static final PublisherBuilder EMPTY_PUBLISHER_BUILDER = (PublisherBuilder) Proxy.newProxyInstance(
        Mocks.class.getClassLoader(),
        new Class<?>[]{PublisherBuilder.class, ToGraphable.class},
        (obj, method, args) -> {
            if (method.getName().equals("toGraph")) {
                return Mocks.EMPTY_PUBLISHER_GRAPH;
            }
            else {
                return null;
            }
        });

    static final ProcessorBuilder PROCESSOR_BUILDER = (ProcessorBuilder) Proxy.newProxyInstance(
        Mocks.class.getClassLoader(),
        new Class<?>[]{ProcessorBuilder.class, ToGraphable.class},
        (obj, method, args) -> {
            if (method.getName().equals("toGraph")) {
                return Mocks.PROCESSOR_GRAPH;
            }
            else {
                return null;
            }
        });

    static final SubscriberBuilder SUBSCRIBER_BUILDER = (SubscriberBuilder) Proxy.newProxyInstance(
        Mocks.class.getClassLoader(),
        new Class<?>[]{SubscriberBuilder.class, ToGraphable.class},
        (obj, method, args) -> {
            if (method.getName().equals("toGraph")) {
                return Mocks.SUBSCRIBER_GRAPH;
            }
            else {
                return null;
            }
        });

}
