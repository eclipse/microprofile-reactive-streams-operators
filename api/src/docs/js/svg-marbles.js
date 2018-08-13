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

  this.graphs = {};

  let graphPrototype = {
    colors: ["#F1948A", "#82E0AA", "#5DADE2", "#F7DC6F", "#C39BD3", "#EB984E"],
    width: 500,
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

  this.term = {
    clazz: "marble",
    type: "term"
  };

  this.n = element => {
    return {
      clazz: "marble",
      type: "next",
      element: element
    };
  };

  this.nterm = element => {
    return {
      clazz: "marble",
      type: "nextterm",
      element: element
    };
  };

  this.e = effect => {
    return {
      clazz: "marble",
      type: "effect",
      effect: effect
    };
  };

  this.none = {
    clazz: "marble",
    type: "none"
  };

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
    if (args[0].clazz === undefined) {
      props = args[0];
      delete args[0];
    }
    let stage = {
      clazz: "stage",
      type: "stream",
      streamType: type,
      events: argsToArray(args)
    };
    return deepMerge(props, stage);
  }

  this.ins = function() {
    return stream(arguments, "in");
  };

  this.out = function() {
    return stream(arguments, "out");
  };

  this.sub = function() {
    return stream(arguments, "sub");
  };

  this.eff = function() {
    return {
      clazz: "stage",
      type: "effect",
      events: argsToArray(arguments)
    };
  };

  this.op = description => {
    return {
      type: "op",
      description: description
    };
  };

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

  this.addGraphs = (gs) => {
    Object.keys(gs).forEach(key => {
      this.graphs[key] = deepMerge(graphPrototype, gs[key]);
    });
  };

  function drawStreamLine(graph, stage) {
    let end = graph.width;
    stage.box.line(0, 0, end, 0).stroke({ width: 2 });
    stage.box.polygon([[end - 10, 0], [end - 15, 10], [end, 0], [end - 15, -10]])
      .fill({color: "black"}).stroke({ width: 1 });
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
      }

      position += spacing;
    });
  }

  function drawEffects(graph, stage) {
    let spacing = graph.width / graph.totalLength;
    let position = spacing / 2;
    const radius = graph.effect.radius.width;
    const offset = graph.effect.offset;

    // We are below the op
    const arrowStart = (graph.opStagePosition + graph.op.height) - stage.box.y();

    stage.events.forEach(effect => {

      if (effect.type === "effect") {
        stage.box.line(position, arrowStart, position, 0)
          .stroke({width: 1, color: "#888"});
        stage.box.line(position, 0, position + offset, 0)
          .stroke({width: 1, color: "#888"});
        stage.box.polygon([[position + offset - 5, 0], [position + offset - 7, -5], [position + offset, 0], [position + offset - 7, 5]])
          .fill({color: "#888"}).stroke({width: 1, color: "#888"});

        const effectCenter = position + offset + radius;

        stage.box.ellipse(radius * 2, graph.effect.radius.height * 2)
          .fill("none")
          .stroke({width: 2, color: "black"})
          .center(effectCenter, 0);
        stage.box.plain(effect.effect)
          .font({"text-anchor": "middle", "dominant-baseline": "central", size: "12pt", family: graph.fonts.code})
          .attr({x: effectCenter, y: 0});
      }

      position += spacing;
    });
  }

  function drawOp(graph, stage) {
    stage.box.rect(graph.width, graph.op.height)
      .fill("none").stroke({width: 1});
    stage.box.plain(stage.description)
      .font({"text-anchor": "middle", "dominant-baseline": "central", family: graph.fonts.code, size: graph.op.fontSize})
      // svg.js does some magic to work out the height of the text box before moving it, we don't want that since
      // we've used dominant-baseline central as the center.
      .attr({x: graph.width / 2, y: graph.op.height / 2});
  }

  this.drawSingle = (element, graph) => {
    computeTotalLength(graph);
    computeColors(graph);
    computerSubStreamLabels(graph);

    let svg = SVG(element);
    let position = 0;

    // First, render the lines and the op stage
    graph.stages.forEach(stage => {
      let box = svg.nested();
      stage.box = box;
      if (stage.type === "stream") {
        position += (graph.stream.spacing / 2);
        box.move(10, position);
        drawStreamLine(graph, stage);
        position += (graph.stream.spacing / 2);
      } else if (stage.type === "op") {
        let boxTop = position + (graph.op.spacing - graph.op.height) / 2;
        graph.opStagePosition = boxTop;
        box.move(10, boxTop);
        drawOp(graph, stage);
        position += graph.op.spacing;
      } else if (stage.type === "effect") {
        position += (graph.stream.spacing / 4);
        box.move(10, position);
        position += (graph.stream.spacing / 2);
      }
    });
    svg.size(graph.width + 20, position);

    // Now that we know where the op stage is, render the marbles
    graph.stages.forEach(stage => {
      if (stage.type === "stream") {
        drawMarbles(graph, stage);
      } else if (stage.type === "effect") {
        drawEffects(graph, stage);
      }
    });
    return {
      width: graph.width + 20,
      height: position
    };
  };

  this.draw = (element, document) => {
    Object.keys(this.graphs).forEach(key => {
      let graphElement = document.createElement("div");
      graphElement.classList.add("diagram");
      graphElement.id = key;
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
            if (event.type === "next" || event.type === "nextterm") {
              if (stage.streamType === "in") {
                currentColor += 1;
              }
              event.color = currentColor;
            }
          }
        }
      });
    }
  }

  function computerSubStreamLabels(graph) {
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

}
