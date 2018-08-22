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

function createMarbles() {
  const marbles = new SvgMarbles();
  const dsl = marbles.dsl;
  const n = dsl.n, e = dsl.e, term = dsl.term, none = dsl.none, nterm = dsl.nterm, r = dsl.r, i = dsl.i, err = dsl.err;
  const ins = dsl.ins, out = dsl.out, op = dsl.op, sub = dsl.sub, eff = dsl.eff, res = dsl.res;

  // @formatter:off
  marbles.addGraphs({
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
    flatMapRsPublisher: {
      stages: [
        ins(n(1), none, none, none, n(2), none, none, none, term ),
        sub(none, n(4), n(5), term                               ),
        sub(none, none, none, none, none, n(3), n(6), term       ),
        op(".flatMapRsPublisher(i -> stream[i])"),
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
        ins(n(1), n(7), n(5), n(2), n(4) ),
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
        ins(n(1), n(3), n(6), n(2), n(4) ),
        op(".takeWhile(i -> i < 4)"),
        out(n(1), n(3), term )
      ]
    },
    dropWhile: {
      stages: [
        ins(n(1), n(3), n(6), n(2), n(3), term ),
        op(".dropWhile(i -> i < 4)"),
        out(none, none, n(6), n(2), n(3), term )
      ]
    },
    forEach: {
      stages: [
        ins(n(1),      n(5),      n(3),      term     ),
        op(".forEach(i -> f(i))"),
        eff(e("f(1)"), e("f(5)"), e("f(3)"), none     ),
        res(none,      none,      none,      r("null"))
      ]
    },
    ignore: {
      stages: [
        ins(n(1), n(5), n(3), term),
        op(".ignore()"),
        res(none, none, none, r("null"))
      ]
    },
    "reduce-identity": {
      stages: [
        ins(none, n(1), n(5), n(3), n(2), term),
        op(".reduce(0, (i, next) -> i + next)",
          i(0), i(1), i(6), i(9), i(11)),
        res(none, none, none, none, none, r("11"))
      ]
    },
    reduce: {
      stages: [
        ins(n(1), n(5), n(3), n(2), term),
        op(".reduce((i, next) -> i + next)",
            i(1), i(6), i(9), i(11)),
        res(none, none, none, none, r("Optional.of(11)"))
      ]
    },
    findFirst: {
      stages: [
        ins(n(3), n(7), n(2)),
        op(".findFirst()"),
        res(r("Optional.of(3)"))
      ]
    },
    collect: {
      op: {
        fontSize: "14pt"
      },
      stages: [
        ins(n(3), n(7), n(2), term),
        op(".collect(ArrayList::new, List::add)",
            i("[3]"), i("[3,7]"), i("[3,7,2]")),
        res(none, none, none, r("[3, 7, 2]"))
      ]
    },
    toList: {
      stages: [
        ins(n(3), n(7), n(2), term),
        op(".toList()"),
        res(none, none, none, r("[3, 7, 2]"))
      ]
    },
    onError: {
      stages: [
        ins(n(1), n(5), n(3), err, none),
        op(".onError(err -> f(err))"),
        eff(none, none, none, e("f(err)")),
        out(n(1), n(5), n(3), err )
      ]
    },
    onErrorResume: {
      stages: [
        ins(n(1), n(5), n(3), err),
        op(".onErrorResume(err -> 4)"),
        out(n(1), n(5), n(3), nterm(4))
      ]
    },
    onErrorResumeWith: {
      op: {
        fontSize: "12pt"
      },
      stages: [
        ins(n(1), n(5), err),
        ins({label: "alternate"}, none, none, none, n(3), n(4), term),
        op(".onErrorResumeWith(err -> alternate)"),
        out(n(1), n(5), none, n(3), n(4), term )
      ]
    },
    onErrorResumeWithRsPublisher: {
      op: {
        fontSize: "12pt"
      },
      stages: [
        ins(n(1), n(5), err),
        ins({label: "alternate"}, none, none, none, n(3), n(4), term),
        op(".onErrorResumeWithRsPublisher(err -> alternate)"),
        out(n(1), n(5), none, n(3), n(4), term )
      ]
    },
    onTerminate: {
      stages: [
        ins(n(1), n(5), n(3), term, none),
        op(".onTerminate(() -> action())"),
        eff(none, none, none, e("action()")),
        out(n(1), n(5), n(3), term )
      ]
    },
    onComplete: {
      stages: [
        ins(n(1), n(5), n(3), term, none),
        op(".onComplete(() -> action())"),
        eff(none, none, none, e("action()")),
        out(n(1), n(5), n(3), term )
      ]
    },
    "of-single": {
      stages: [
        op("ReactiveStreams.of(3)"),
        ins(n(3), term, none, none, none )
      ]
    },
    "of-many": {
      stages: [
        op("ReactiveStreams.of(3, 1, 5, 6)"),
        ins(n(3), n(1), n(5), n(6), term)
      ]
    },
    empty: {
      stages: [
        op("ReactiveStreams.empty()"),
        ins(term, none, none, none)
      ]
    },
    ofNullable: {
      stages: [
        op("ReactiveStreams.ofNullable(null)"),
        ins(term, none, none, none)
      ]
    },
    fromIterable: {
      op: {
        fontSize: "11pt"
      },
      stages: [
        op("ReactiveStreams.fromIterable(Arrays.asList(3, 1, 5, 6))"),
        ins(n(3), n(1), n(5), n(6), term)
      ]
    },
    failed: {
      op: {
        fontSize: "14pt"
      },
      stages: [
        op("ReactiveStreams.failed(new Exception())"),
        ins(err, none, none, none)
      ]
    },
    identity: {
      stages: [
        ins(n(3), n(1), n(5), n(6), term),
        op("ReactiveStreams.builder()"),
        out(n(3), n(1), n(5), n(6), term)
      ]
    },
    iterate: {
      op: {
        fontSize: "14pt"
      },
      stages: [
        op("ReactiveStreams.iterate(1, i -> i + 2)"),
        ins(n(1, {link: [0, 1]}), n(3, {link: [0, 2]}), n(5, {link: [0, 3]}), n(7, {link: [0, 4]}), n(9))
      ]
    },
    generate: {
      op: {
        fontSize: "14pt"
      },
      stages: [
        op("ReactiveStreams.generate(() -> 5)"),
        ins(n(5), n(5), n(5), n(5), n(5))
      ]
    },
    concat: {
      stages: [
        ins({label: "stream1"}, none, n(3), n(5), term),
        ins({label: "stream2"}, none, none, none, none, n(4), n(2), n(1), term),
        op(".concat(stream1, stream2)"),
        out(none, n(3), n(5), none, n(4), n(2), n(1), term )
      ]
    },

    /**
     * This is the example diagram, with labels explaining what everything is.
     */
    example: {
      margin: {
        xl: 120,
        xr: 130,
        yt: 80,
        yb: 10
      },
      stages: [
        ins(n(1), n(2), err),
        ins({label: "alternate"}, none, none, none, n(3), term),
        op(".operator(err -> alternate)", none, none, i(7)),
        eff(e("f(1)", e("f(2)"))),
        out(n(1), n(2), none, n(3), term),
        res(none, none, none, none, r("\"result\""))
      ],
      postProcess: graph => {
        const svg = graph.svg;
        function label(text, labelX, labelY, element, elementR) {
          const box = svg.text(text)
            .font({size: "12pt", family: "sans"})
            .move(labelX, labelY)
            .bbox();

          const elementX = element[0];
          const elementY = element[1];

          // First calculate where the line from the label starts, we put it on the bounding box
          // of the label closest to the element
          let lx, ly = 0;
          if (box.x > elementX) {
            lx = box.x;
          } else if (box.x2 < elementX) {
            lx = box.x2;
          } else {
            lx = (box.x + box.x2) / 2;
          }
          if (box.y > elementY) {
            ly = box.y;
          } else if (box.y2 < elementY) {
            ly = box.y2;
          } else {
            ly = (box.y + box.y2) / 2;
          }

          // Now calculate where the line from the element starts. elementX and elementY should be
          // the center points of the element, we want it elementR radius from that in the direction
          // of the label start point.
          // length of line to the centre using pythagoras
          const d = Math.sqrt(Math.pow(lx - elementX, 2) + Math.pow(ly - elementY, 2));
          // ratio of the distance that we want vs the distance to the centre
          const t = ((d - elementR) / d);
          // apply the ratio to the points
          const ex = (1 - t) * lx + t * elementX;
          const ey = (1 - t) * ly + t * elementY;

          svg.line(lx, ly, ex, ey)
            .stroke({color: "#888", width: 1});

        }
        const radius = graph.marble.radius + 5;
        label("elements\npassed\nto onNext", 20, 20, graph.marbleCoords(0, 0), radius);
        const alt = graph.marbleCoords(1, 0);
        label("labelled\nsubstream\nused by\noperator", 10, alt[1] - 50, [graph.margin.xl, alt[1]], 5);
        const op = graph.marbleCoords(2, 0);
        label("example\noperator\ninvocation", 10, op[1], [graph.margin.xl, op[1] + graph.op.height / 2], 5);
        const err = graph.marbleCoords(0, 2);
        label("stream\nterminates\nwith error", err[0] - 150, 10, err, radius);
        const ins = graph.marbleCoords(0, 3);
        label("input\nstream", ins[0] - 20, 30, ins, 5);
        const term = graph.marbleCoords(1, 4);
        label("stream\nterminates\nnormally", term[0] + 60, ins[1] - 30, term, 10);
        const out = graph.marbleCoords(4, 0);
        label("output\nstream", 20, out[1] - 10, [graph.margin.xl, out[1]], 5);
        const eff = graph.marbleCoords(3, 0);
        label("side-effect\nexecuted by\noperator", eff[0] - 10, out[1] + 30, [eff[0] + 50, eff[1]], radius);
        const inter = graph.marbleCoords(2, 2);
        label("intermediate\noperator value\npassed to next\ncomputation", inter[0] - 50, out[1] + 30, [inter[0], inter[1] + graph.op.height], radius + 15);
        const result = graph.marbleCoords(5, 4);
        label("result value\nredeemed by\nCompletionStage", result[0] + 30, eff[1] - 20, [result[0] + 5, result[1]], 10);

      }
    },

  });
  // @formatter:on

  return marbles;

}

