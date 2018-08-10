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
    spacing: 50,
    op: {
      height: 40
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

  function drawStreamLine(svg, graph, stage) {
    let end = graph.width;
    svg.line(0, 0, end, 0).stroke({ width: 2 });
    svg.polygon([[end - 10, 0], [end - 15, 10], [end, 0], [end - 15, -10]])
      .fill({color: "black"}).stroke({ width: 1 });

    let spacing = graph.width / graph.totalLength;
    let position = spacing / 2;
    const radius = graph.marble.radius;
    stage.events.forEach(event => {
      if (event.type === "next") {
        svg.circle(radius * 2)
          .fill({color: graph.colors[event.color]})
          .stroke({width: 2, color: "black"})
          .center(position, 0);
        svg.text(event.element.toString())
          .font({anchor: "middle", size: "16pt"})
          .move(position, -8);
      } else if (event.type === "term") {
        svg.line(position, -radius, position, radius)
          .stroke({width: 2, color: "black"});
      }
      position += spacing;
    });
  }

  function drawOp(svg, graph, description) {
    svg.rect(graph.width, graph.op.height)
      .fill("none").stroke({width: 1});
    // Todo, positioning here doesn't actually vertically align it
    svg.text(description).font({anchor: "middle", family: "Source Code Pro", size: "18pt"})
      .move(graph.width / 2, graph.op.height / 4);
  }

  this.drawSingle = (element, graph) => {
    computeTotalLength(graph);
    computeColors(graph);

    let svg = SVG(element);
    let position = 0;
    graph.stages.forEach(stage => {
      position += graph.spacing;
      let box = svg.nested();
      box.move(0, position);
      if (stage.type === "stream") {
        drawStreamLine(box, graph, stage);
      } else if (stage.type === "op") {
        drawOp(box, graph, stage.description);
        position += graph.op.height;
      }
    });
    svg.size(graph.width + 20, position + graph.spacing);
  };

  this.draw = (element, document) => {
    Object.keys(graphs).forEach(key => {
      let graphElement = document.createElement("div");
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
