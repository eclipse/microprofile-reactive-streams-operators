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

package org.eclipse.microprofile.reactive.messaging.tck.arquillian;

import org.eclipse.microprofile.reactive.messaging.tck.framework.TckMessagingManager;
import org.eclipse.microprofile.reactive.messaging.tck.spi.TckContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class TckArquillianDeployListener {

  private final TckContainer tckContainer;

  private final Map<Class<?>, List<DeploymentDescription>> activeDeployments = new ConcurrentHashMap<>();

  private final Archive<?> frameworkClasses = ShrinkWrap.create(JavaArchive.class)
      .addPackage(TckMessagingManager.class.getPackage().getName());

  private final Archive<?> frameworkArchive = ShrinkWrap.create(JavaArchive.class)
      .addPackage(TckMessagingManager.class.getPackage().getName())
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");


  public TckArquillianDeployListener() {
    Iterator<TckContainer> containers = ServiceLoader.load(TckContainer.class).iterator();

    if (containers.hasNext()) {
      this.tckContainer = containers.next();
    }
    else {
      throw new RuntimeException("No " + TckContainer.class.getName() +
          " found. To run this TCK, you must provide an implementation of " +
          TckContainer.class + " via the JDK service loader mechanism.");
    }
  }

  public void onBeforeDeploy(@Observes BeforeDeploy beforeDeploy, TestClass testClass) throws DeploymentException {

    String[] topicNames = getTopicsUsedByTestClass(testClass);

    DeployableContainer<?> container = beforeDeploy.getDeployableContainer();
    DeploymentDescription description = beforeDeploy.getDeployment();

    List<DeploymentDescription> deployed = new ArrayList<>();
    activeDeployments.put(testClass.getJavaClass(), deployed);

    List<DeploymentDescription> deployments = tckContainer.createDeployments(topicNames);
    for (DeploymentDescription deployable: deployments) {
      if (!tckContainer.mergeArchives() || deployable.isDescriptorDeployment() || description.isDescriptorDeployment()) {
        deployed.add(deployable);
        container.deploy(deployable.getDescriptor());
      }
      else {
        description.getArchive().merge(deployable.getArchive());
      }
    }

    if (description.isArchiveDeployment()) {
      description.getArchive().merge(frameworkClasses);
    }
    else {
      deployed.add(new DeploymentDescription("reactive-messaging-tck-framework.jar", frameworkArchive));
      container.deploy(frameworkArchive);
    }
  }

  public void onAfterUnDeploy(@Observes AfterUnDeploy afterUnDeploy, TestClass testClass) throws DeploymentException {

    String[] topicNames = getTopicsUsedByTestClass(testClass);

    DeployableContainer<?> container = afterUnDeploy.getDeployableContainer();

    List<DeploymentDescription> deployments = activeDeployments.remove(testClass.getJavaClass());
    if (deployments == null) {
      throw new IllegalStateException("After undeploy on test class that wasn't deployed?");
    }
    for (DeploymentDescription deployable: deployments) {
      if (deployable.isDescriptorDeployment()) {
        container.undeploy(deployable.getDescriptor());
      }
      else {
        container.undeploy(deployable.getArchive());
      }
    }

    tckContainer.teardownTopics(topicNames);
  }

  private String[] getTopicsUsedByTestClass(TestClass testClass) {
    Topics topics = testClass.getAnnotation(Topics.class);
    String[] topicNames;
    if (topics != null) {
      topicNames = topics.value();
    }
    else {
      topicNames = new String[0];
    }
    return topicNames;
  }

}
