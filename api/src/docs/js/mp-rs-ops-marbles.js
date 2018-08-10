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

const m = new SvgMarbles();
const n = m.n, term = m.term, none = m.none;
const ins = m.ins, out = m.out, op = m.op, sub = m.sub;

// @formatter:off
m.addGraphs({
  map: {
    stages: [
      ins(n(1),  n(5),  n(3),  term ),
      op(".map(i -> i * 10)"),
      out(n(10), n(50), n(30), term )
    ]
  },
  filter: {
    stages: [
      ins(n(1), n(7), n(5), n(2), term ),
      op(".filter(i -> i < 3)"),
      out(n(1), none, none, n(2), term )
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
  }
});
// @formatter:on
