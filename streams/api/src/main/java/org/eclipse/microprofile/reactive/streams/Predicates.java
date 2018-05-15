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

package org.eclipse.microprofile.reactive.streams;

import java.util.function.Predicate;

/**
 * Stateful predicates.
 */
final class Predicates {

  private Predicates() {
  }

  /**
   * Predicate used to implement skip with a filter function.
   */
  static class SkipPredicate<T> implements Predicate<T> {
    private final long toSkip;
    private long count = 0;

    SkipPredicate(long toSkip) {
      this.toSkip = toSkip;
    }

    @Override
    public boolean test(T t) {
      if (count < toSkip) {
        count++;
        return false;
      }
      else {
        return true;
      }
    }
  }

  /**
   * Predicate used to implement drop while with a filter function.
   */
  static class DropWhilePredicate<T> implements Predicate<T> {
    private final Predicate<? super T> predicate;
    private boolean dropping = true;

    DropWhilePredicate(Predicate<? super T> predicate) {
      this.predicate = predicate;
    }

    @Override
    public boolean test(T t) {
      if (dropping) {
        if (predicate.test(t)) {
          return false;
        }
        else {
          return true;
        }
      }
      else {
        return true;
      }
    }
  }

  /**
   * Predicate used to implement limit().
   * <p>
   * This returns false when the limit is reached, not exceeded, and is intended to be used with an inclusive takeWhile,
   * this ensures that the stream completes as soon as the limit is reached, rather than having to wait for the next
   * element before the stream is completed.
   * <p>
   * As a consequence, this can't be used with a limit of 0.
   */
  static class LimitPredicate<T> implements Predicate<T> {
    private final long limitTo;
    private long count = 0;

    LimitPredicate(long limitTo) {
      assert limitTo > 0;
      this.limitTo = limitTo;
    }

    @Override
    public boolean test(T t) {
      return ++count < limitTo;
    }
  }
}
