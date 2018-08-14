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
 * Reactive Streams marble diagram DSL.
 */
function SvgMarbles() {

  function deepMerge(from, to) {
    Object.keys(from).forEach(key => {
      if (typeof from[key] === "object") {
        if (to[key] === undefined) {
          to[key] = {};
        }
        deepMerge(from[key], to[key]);
      } else {
        if (to[key] === undefined) {
          to[key] = from[key];
        }
      }
    });
    return to;
  }

  function argsToArray(args) {
    let array = new Array(args.length);
    Object.keys(args).forEach((k, idx) => {
      array[idx] = args[idx];
    });
    return array;
  }

  /**
   * The first argument to the stage may be a properties array. If it is, we extract it.
   */
  function stream(args, type) {
    let props = {};
    const argsArray = argsToArray(args);
    if (argsArray[0].clazz === undefined) {
      props = argsArray[0];
      argsArray.shift();
    }
    let stage = {
      clazz: "stage",
      type: "stream",
      streamType: type,
      events: argsArray
    };
    return deepMerge(props, stage);
  }

  /**
   * The DSL.
   */
  this.dsl = {
    /**
     * A terminator marble.
     */
    term: {
      clazz: "marble",
      type: "term"
    },

    /**
     * An error marble.
     */
    err: {
      clazz: "marble",
      type: "error"
    },

    /**
     * A next marble (passed to onNext).
     */
    n: (element, props) => {
      if (props === undefined) {
        props = {};
      }
      return deepMerge(props, {
        clazz: "marble",
        type: "next",
        element: element
      });
    },

    /**
     * A next terminating marble.
     */
    nterm: element => {
      return {
        clazz: "marble",
        type: "nextterm",
        element: element
      };
    },

    /**
     * An effect marble.
     */
    e: effect => {
      return {
        clazz: "marble",
        type: "effect",
        effect: effect
      };
    },

    /**
     * No marble.
     */
    none: {
      clazz: "marble",
      type: "none"
    },

    /**
     * A result marble.
     */
    r: value => {
      return {
        clazz: "marble",
        type: "result",
        value: value
      }
    },

    /**
     * An intermediate value marble.
     */
    i: value => {
      return {
        clazz: "marble",
        type: "intermediate",
        value: value
      }
    },

    /**
     * An in stream.
     */
    ins: function () {
      return stream(arguments, "in");
    },

    /**
     * An out stream.
     */
    out: function () {
      return stream(arguments, "out");
    },

    /**
     * A sub stream.
     */
    sub: function () {
      return stream(arguments, "sub");
    },

    /**
     * An effect stream.
     */
    eff: function () {
      return {
        clazz: "stage",
        type: "effect",
        events: argsToArray(arguments)
      };
    },

    /**
     * An operator description.
     */
    op: function () {
      const args = argsToArray(arguments);
      const description = args[0];
      args.shift();
      return {
        type: "op",
        description: description,
        events: args
      };
    },

    /**
     * A result stream.
     */
    res: function () {
      return {
        clazz: "stage",
        type: "result",
        events: argsToArray(arguments)
      };
    }
  };

  this.graphs = {};

  const graphPrototype = {
    colors: ["#F1948A", "#82E0AA", "#5DADE2", "#F7DC6F", "#C39BD3", "#EB984E"],
    width: 500,
    margin: {
      xl: 10,
      xr: 10,
      yt: 10,
      yb: 10
    },
    marble: {
      radius: 16
    },
    effect: {
      radius: {
        width: 30,
        height: 15
      },
      offset: 20
    },
    stream: {
      spacing: 50,
    },
    op: {
      height: 40,
      spacing: 80,
      fontSize: "18pt"
    },
    fonts: {
      code: "Source Code Pro"
    }
  };


  this.addGraphs = (gs) => {
    Object.keys(gs).forEach(key => {
      this.graphs[key] = deepMerge(graphPrototype, gs[key]);
    });
  };

  function drawStreamLine(graph, stage) {
    let end = graph.width;
    stage.box.line(0, 0, end, 0).stroke({width: 2});
    stage.box.polygon([[end - 10, 0], [end - 15, 10], [end, 0], [end - 15, -10]])
      .fill({color: "black"}).stroke({width: 1});
    if (stage.label !== undefined) {
      stage.box.text(stage.label)
        .font({size: "8pt", family: graph.fonts.code})
        .move(0, 5);
    }
  }

  function drawMarbleLine(box, position, arrowStart, arrowEnd) {
    box.line(position, arrowStart, position, arrowEnd)
      .stroke({width: 1, color: "#888"});
    box.polygon([[position, arrowEnd - 5], [position - 5, arrowEnd - 7], [position, arrowEnd],
      [position + 5, arrowEnd - 7]]).fill({color: "#888"}).stroke({width: 1, color: "#888"});
  }

  function drawMarbles(graph, stage) {
    let spacing = graph.width / graph.totalLength;
    let position = spacing / 2;
    const radius = graph.marble.radius;

    let arrowStart = 0;
    let arrowEnd = 0;
    // Find out where our box sits
    if (stage.box.y() < graph.opStagePosition) {
      // We are above the op
      arrowStart = radius + 1;
      arrowEnd = graph.opStagePosition - stage.box.y() - 1;
    } else {
      // We are below the op
      arrowStart = (graph.opStagePosition + graph.op.height) - stage.box.y();
      arrowEnd = -radius - 1;
    }

    stage.events.forEach(event => {
      if (event.type === "next" || event.type === "nextterm") {
        if (event.type === "nextterm") {
          stage.box.line(position, -radius - 8, position, radius + 8)
            .stroke({width: 3, color: "black"});
          drawMarbleLine(stage.box, position, arrowStart, arrowEnd - 7);
        } else {
          drawMarbleLine(stage.box, position, arrowStart, arrowEnd);
        }

        stage.box.circle(radius * 2)
          .fill({color: graph.colors[event.color]})
          .stroke({width: 2, color: "black"})
          .center(position, 0);
        stage.box.plain(event.element.toString())
          .font({"text-anchor": "middle", "dominant-baseline": "central", size: "16pt", family: graph.fonts.code})
          .attr({x: position, y: 0});
      } else if (event.type === "term") {
        stage.box.line(position, -radius, position, radius)
          .stroke({width: 3, color: "black"});
        drawMarbleLine(stage.box, position, arrowStart, arrowEnd);
      } else if (event.type === "error") {
        stage.box.line(position - radius, -radius, position + radius, radius)
          .stroke({width: 3, color: "black"});
        stage.box.line(position - radius, radius, position + radius, -radius)
          .stroke({width: 3, color: "black"});
        drawMarbleLine(stage.box, position, arrowStart, arrowEnd);
      } else if (event.type === "result") {
        const text = stage.box.plain(event.value)
          .font({"text-anchor": "end", "dominant-baseline": "central", family: graph.fonts.code, size: "16pt"})
          .attr({x: position + 20, y: graph.op.height / 2});
        let textBox = text.bbox();
        if (textBox.x < 10) {
          text.attr({"text-anchor": "start", x: 10});
          textBox = text.bbox();
        }
        stage.box.rect(textBox.width + 20, graph.op.height)
          .radius(5)
          .move(textBox.x - 10, 0)
          .stroke({width: 2, color: "black"})
          .fill("none");
        drawMarbleLine(stage.box, position, arrowStart, -1);
      }

      position += spacing;
    });
  }

  function drawEffects(graph, stage) {
    let spacing = graph.width / graph.totalLength;
    let position = spacing / 2;
    const offset = graph.effect.offset;

    // We are below the op
    const arrowStart = (graph.opStagePosition + graph.op.height) - stage.box.y();

    stage.events.forEach(effect => {

      if (effect.type === "effect") {
        const start = position + offset;
        stage.box.line(position, arrowStart, position, 0)
          .stroke({width: 1, color: "#888"});
        stage.box.line(position, 0, position + offset, 0)
          .stroke({width: 1, color: "#888"});
        stage.box.polygon([[start - 6, 0], [start - 8, -5], [start - 1, 0], [start - 8, 5]])
          .fill({color: "#888"}).stroke({width: 1, color: "#888"});

        const textBox = stage.box.plain(effect.effect)
          .font({"text-anchor": "start", "dominant-baseline": "central", size: "12pt", family: graph.fonts.code})
          .attr({x: start + 10, y: 0}).bbox();

        stage.box.ellipse(textBox.width + 20, graph.effect.radius.height * 2)
          .fill("none")
          .stroke({width: 2, color: "black"})
          .cy(0)
          .x(start);
      }

      position += spacing;
    });
  }

  /**
   * This should be invoked on stages after marbles are drawn, since this is likely to draw intemediate values
   * on top of marble lines, which we want.
   */
  function drawIntermediateValues(graph, stage) {
    const spacing = graph.width / graph.totalLength;
    let position = spacing / 2;
    const radius = graph.marble.radius;
    const offset = graph.op.height + radius / 2;

    stage.events.forEach(event => {

      let linkStart = radius;

      if (event.type === "intermediate") {
        const ellipse = stage.box.ellipse(radius * 2, radius * 2)
          .center(position, offset)
          .stroke({width: 2, color: "#888"})
          .fill({color: "white"});

        const textBox = stage.box.plain(event.value.toString())
          .font({
            "text-anchor": "middle",
            "dominant-baseline": "central",
            size: "16pt",
            family: graph.fonts.code,
            fill: "#888"
          })
          .attr({x: position, y: offset}).bbox();

        if (textBox.width > radius * 2) {
          ellipse.rx(textBox.width / 2 + 10);
        }

        linkStart = graph.op.height + radius / 2 + radius;
      }


      if (event.link !== undefined) {
        // Calculate what we're linking to
        const linkStage = graph.stages[event.link[0]];
        // only support op for now
        if (linkStage.type === "op") {
          const linkX = (spacing / 2) + event.link[1] * spacing;
          const linkY = linkStage.box.y() - stage.box.y();

          stage.box.path([
            "M", position, linkStart, "C", position + 20, linkStart + 60,
            linkX, linkY - 80, linkX, linkY
          ].join(" "))
            .fill("none")
            .stroke({width: 1, color: "#888"});

          stage.box.polygon([[linkX, linkY - 5], [linkX - 5, linkY - 7], [linkX, linkY],
            [linkX + 5, linkY - 7]]).fill({color: "#888"}).stroke({width: 1, color: "#888"});
        }

      }

      position += spacing;
    });


  }

  function drawOp(graph, stage) {
    stage.box.rect(graph.width, graph.op.height)
      .fill("none").stroke({width: 1});
    stage.box.plain(stage.description)
      .font({
        "text-anchor": "middle",
        "dominant-baseline": "central",
        family: graph.fonts.code,
        size: graph.op.fontSize
      })
      // svg.js does some magic to work out the height of the text box before moving it, we don't want that since
      // we've used dominant-baseline central as the center.
      .attr({x: graph.width / 2, y: graph.op.height / 2});
  }

  this.drawSingle = (element, graph) => {
    computeTotalLength(graph);
    computeColors(graph);
    computeSubStreamLabels(graph);
    computeLinks(graph);

    graph.marbleCoords = marbleCoords;
    const svg = SVG(element);
    graph.svg = svg;
    const marginX = graph.margin.xl;

    let position = graph.margin.yt;

    // First, render the lines and the op stage
    graph.stages.forEach(stage => {
      const box = svg.nested();
      stage.box = box;
      if (stage.type === "stream") {
        position += (graph.stream.spacing / 2);
        box.move(marginX, position);
        drawStreamLine(graph, stage);
        position += (graph.stream.spacing / 2);
      } else if (stage.type === "op") {
        const boxTop = position + (graph.op.spacing - graph.op.height) / 2;
        graph.opStagePosition = boxTop;
        box.move(marginX, boxTop);
        drawOp(graph, stage);
        position += graph.op.spacing;
      } else if (stage.type === "effect") {
        position += (graph.stream.spacing / 4);
        box.move(marginX, position);
        position += (graph.stream.spacing / 2);
      } else if (stage.type === "result") {
        const boxTop = position + (graph.op.spacing - graph.op.height) / 2;
        box.move(marginX, boxTop);
        position += graph.op.spacing;
      }
    });

    // Now that we know where the op stage is, render the marbles
    graph.stages.forEach(stage => {
      if (stage.type === "stream" || stage.type === "result") {
        drawMarbles(graph, stage);
      } else if (stage.type === "effect") {
        drawEffects(graph, stage);
      }
    });

    // And render intermediate values and links
    graph.stages.forEach(stage => {
      if (stage.type === "op" || stage.type === "stream") {
        drawIntermediateValues(graph, stage);
      }
    });

    svg.size(graph.width + marginX + graph.margin.xr, position + graph.margin.yb);
    if (graph.postProcess !== undefined) {
      graph.postProcess(graph);
    }
    return {
      width: graph.width + marginX + graph.margin.xr,
      height: position + graph.margin.yb
    };
  };

  function marbleCoords(stageN, eventN) {
    const spacing = this.width / this.totalLength;
    const x = this.margin.xl + (spacing / 2) + eventN * spacing;
    const y = this.stages[stageN].box.y();
    return [x, y];
  }

  this.draw = (element, document) => {
    Object.keys(this.graphs).forEach(key => {
      const a = document.createElement("a");
      a.href = "#" + key;
      a.textContent = key;
      const heading = document.createElement("h3");
      heading.appendChild(a);
      heading.id = key;
      element.appendChild(heading);
      const graphElement = document.createElement("div");
      graphElement.classList.add("diagram");
      this.drawSingle(graphElement, this.graphs[key]);
      element.appendChild(graphElement);
    });
  };

  function computeTotalLength(graph) {
    let length = 0;
    graph.stages.forEach(stage => {
      if (stage.type === "stream") {
        if (stage.events.length > length) {
          length = stage.events.length;
        }
      }
    });
    graph.totalLength = length;
  }

  function computeColors(graph) {
    let currentColor = -1;
    for (let i = 0; i < graph.totalLength; i++) {
      graph.stages.forEach(stage => {
        if (stage.type === "stream") {
          if (stage.events.length > i) {
            let event = stage.events[i];
            if (event.type === "next" || event.type === "nextterm" || event.type === "error") {
              if (stage.streamType === "in" || currentColor === -1) {
                currentColor += 1;
              }
              event.color = currentColor;
            }
          }
        }
      });
    }
  }

  function computeSubStreamLabels(graph) {
    let subStream = 0;
    graph.stages.forEach(stage => {
      if (stage.type === "stream" && stage.streamType === "sub") {
        subStream += 1;
        if (stage.label === undefined) {
          stage.label = "stream[" + subStream + "]";
        }
      }
    });
  }

  function computeLinks(graph) {
    for (let i = 0; i < graph.stages.length; i++) {
      const stage = graph.stages[i];
      if (stage.type === "op") {
        for (let j = 0; j < stage.events.length; j++) {
          const el = stage.events[j];
          if (el.type === "intermediate") {
            if (el.link === undefined) {
              el.link = [i, j + 1];
            }
          }
        }
      }
    }
  }

}
