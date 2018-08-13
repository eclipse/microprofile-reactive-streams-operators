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

/**
 * This is where all the digrams themselves are declared. It's included by index.html and
 * puppeteer.html along with svg-marbles.js to actually render the diagrams.
 */

const m = new SvgMarbles();
const n = m.n, e = m.e, term = m.term, none = m.none, nterm = m.nterm;
const ins = m.ins, out = m.out, op = m.op, sub = m.sub, eff = m.eff;

// @formatter:off
m.addGraphs({
  map: {
    stages: [
      ins(n(1),  n(5),  n(3),  term ),
      op(".map(i -> i * 10)"),
      out(n(10), n(50), n(30), term )
    ]
  },
  peek: {
    stages: [
      ins(n(1),      n(5),      n(3),      term ),
      op(".peek(i -> f(i))"),
      eff(e("f(1)"), e("f(5)"), e("f(3)"), none ),
      out(n(1),      n(5),      n(3),      term )
    ]
  },
  filter: {
    stages: [
      ins(n(1), n(7), n(5), n(2), term ),
      op(".filter(i -> i < 3)"),
      out(n(1), none, none, n(2), term )
    ]
  },
  distinct: {
    stages: [
      ins(n(1), n(6), n(6), n(2), n(1), n(3), term ),
      op(".distinct()"),
      out(n(1), n(6), none, n(2), none, n(3), term )
    ]
  },
  flatMap: {
    stages: [
      ins(n(1), none, none, none, n(2), none, none, none, term ),
      sub(none, n(4), n(5), term                               ),
      sub(none, none, none, none, none, n(3), n(6), term       ),
      op(".flatMap(i -> stream[i])"),
      out(none, n(4), n(5), none, none, n(3), n(6), none, term )
    ]
  },
  flatMapPublisher: {
    stages: [
      ins(n(1), none, none, none, n(2), none, none, none, term ),
      sub(none, n(4), n(5), term                               ),
      sub(none, none, none, none, none, n(3), n(6), term       ),
      op(".flatMapPublisher(i -> stream[i])"),
      out(none, n(4), n(5), none, none, n(3), n(6), none, term )
    ]
  },
  flatMapCompletionStage: {
    op: {
      fontSize: "12pt"
    },
    stages: [
      ins(n(1), none, n(5), none, n(4), none, term ),
      op(".flatMapCompletionStage(i -> asyncEcho(i + 2))"),
      out(none, n(3), none, n(7), none, n(6), term )
    ]
  },
  flatMapIterable: {
    op: {
      fontSize: "12pt"
    },
    stages: [
      ins(n(1), none, n(5), none, n(4), none, term ),
      op(".flatMapIterable(i -> Arrays.asList(i, i + 1))"),
      out(n(1), n(2), n(5), n(6), n(4), n(5), term )
    ]
  },
  limit: {
    stages: [
      ins(n(1), n(7), n(5), n(2), n(4), term ),
      op(".limit(2)"),
      out(n(1), nterm(7) )
    ]
  },
  skip: {
    stages: [
      ins(n(1), n(7), n(5), n(2), n(4), term ),
      op(".skip(3)"),
      out(none, none, none, n(2), n(4), term )
    ]
  },
  takeWhile: {
    stages: [
      ins(n(1), n(3), n(6), n(2), n(4), term ),
      op(".takeWhile(i -> i < 4)"),
      out(n(1), n(3), term )
    ]
  }


});
// @formatter:on
