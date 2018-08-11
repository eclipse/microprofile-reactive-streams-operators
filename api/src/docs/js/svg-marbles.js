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

function SvgMarbles() {

  let graphs = {};

  let graphPrototype = {
    colors: ["#F1948A", "#82E0AA", "#5DADE2", "#F7DC6F"],
    width: 500,
    marble: {
      radius: 20
    },
    stream: {
      spacing: 50,
    },
    op: {
      height: 40,
      spacing: 80
    }
  };

  this.term = {
    type: "term"
  };

  this.n = element => {
    return {
      type: "next",
      element: element
    };
  };

  this.none = {
    type: "none"
  };

  function argsToArray(args) {
    let array = new Array(args.length);
    Object.keys(args).forEach((k, idx) => {
      array[idx] = args[idx];
    });
    return array;
  }

  this.ins = function() {
    return {
      type: "stream",
      streamType: "in",
      events: argsToArray(arguments)
    };
  };

  this.out = function() {
    return {
      type: "stream",
      streamType: "out",
      events: argsToArray(arguments)
    };
  };

  this.sub = function() {
    return {
      type: "stream",
      streamType: "sub",
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
      graphs[key] = deepMerge(graphPrototype, gs[key]);
    });
  };

  function drawStreamLine(graph, stage) {
    let end = graph.width;
    stage.box.line(0, 0, end, 0).stroke({ width: 2 });
    stage.box.polygon([[end - 10, 0], [end - 15, 10], [end, 0], [end - 15, -10]])
      .fill({color: "black"}).stroke({ width: 1 });
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
    console.log(stage.box.y(), graph.opStagePosition);
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
      if (event.type === "next") {
        stage.box.circle(radius * 2)
          .fill({color: graph.colors[event.color]})
          .stroke({width: 2, color: "black"})
          .center(position, 0);
        stage.box.text(event.element.toString())
          .font({anchor: "middle", size: "16pt"})
          .move(position, -8);
        drawMarbleLine(stage.box, position, arrowStart, arrowEnd);
      } else if (event.type === "term") {
        stage.box.line(position, -radius, position, radius)
          .stroke({width: 2, color: "black"});
        drawMarbleLine(stage.box, position, arrowStart, arrowEnd);
      }

      position += spacing;
    });
  }

  function drawOp(graph, stage) {
    stage.box.rect(graph.width, graph.op.height)
      .fill("none").stroke({width: 1});
    // Todo, positioning here doesn't actually vertically align it
    stage.box.text(stage.description).font({anchor: "middle", family: "Source Code Pro", size: "18pt"})
      .move(graph.width / 2, graph.op.height / 4);
  }

  this.drawSingle = (element, graph) => {
    computeTotalLength(graph);
    computeColors(graph);

    let svg = SVG(element);
    let position = 0;

    // First, render the lines and the op stage
    graph.stages.forEach(stage => {
      let box = svg.nested();
      stage.box = box;
      if (stage.type === "stream") {
        position += (graph.stream.spacing / 2);
        box.move(0, position);
        drawStreamLine(graph, stage);
        position += (graph.stream.spacing / 2);
      } else if (stage.type === "op") {
        let boxTop = position + (graph.op.spacing - graph.op.height) / 2;
        graph.opStagePosition = boxTop;
        box.move(0, boxTop);
        drawOp(graph, stage);
        position += graph.op.spacing;
      }
    });
    svg.size(graph.width + 20, position);

    // Now that we know where the op stage is, render the marbles
    graph.stages.forEach(stage => {
      if (stage.type === "stream") {
        drawMarbles(graph, stage);
      }
    });
  };

  this.draw = (element, document) => {
    Object.keys(graphs).forEach(key => {
      let graphElement = document.createElement("div");
      graphElement.classList.add("diagram");
      graphElement.id = key;
      this.drawSingle(graphElement, graphs[key]);
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
            if (event.type === "next") {
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

}
