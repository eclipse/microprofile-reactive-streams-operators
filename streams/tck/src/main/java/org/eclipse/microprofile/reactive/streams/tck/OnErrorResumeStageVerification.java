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

package org.eclipse.microprofile.reactive.streams.tck;

import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for OnErrorResume.
 */
public class OnErrorResumeStageVerification extends AbstractStageVerification {

  OnErrorResumeStageVerification(ReactiveStreamsTck.VerificationDeps deps) {
    super(deps);
  }

  @Test
  public void onErrorShouldCatchErrorFromSource() {
    AtomicReference<Throwable> exception = new AtomicReference<>();
    assertEquals(await(ReactiveStreams.failed(new Exception("failed"))
        .onErrorResume(err -> {
          exception.set(err);
          return "foo";
        })
        .toList()
        .run(getEngine())), Collections.singletonList("foo"));
    assertEquals(exception.get().getMessage(), "failed");
  }

  @Test
  public void onErrorShouldCatchErrorFromStage() {
    AtomicReference<Throwable> exception = new AtomicReference<>();
    assertEquals(await(ReactiveStreams.of("a", "b", "c")
      .map(word -> {
        if (word.equals("b")) {
          throw new RuntimeException("failed");
        }
        return word.toUpperCase();
      })
      .onErrorResume(err -> {
        exception.set(err);
        return "foo";
      })
      .toList()
      .run(getEngine())), Arrays.asList("A", "foo"));
    assertEquals(exception.get().getMessage(), "failed");
  }


  @Test(expectedExceptions = RuntimeException.class)
  public void onErrorResumeStageShouldPropagateRuntimeExceptions() {
    await(ReactiveStreams.failed(new Exception("source-failure"))
        .onErrorResume(t -> {
          throw new RuntimeException("failed");
        })
        .toList()
        .run(getEngine()));
  }


  @Override
  List<Object> reactiveStreamsTckVerifiers() {
    return Collections.emptyList();
  }


}
