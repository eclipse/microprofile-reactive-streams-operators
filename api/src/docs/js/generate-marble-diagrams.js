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
 * Run this script in Puppeteer (ie, headless Chrome) to actually render each diagram.
 */
const htmlPage = "file://" + __dirname + "/puppeteer.html";
const outputDir = __dirname + "/../../../target/apidocs/org/eclipse/microprofile/reactive/streams/doc-files";

const shell = require("shelljs");
shell.mkdir("-p", outputDir);

const puppeteer = require("puppeteer");

(async () => {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  // Important to wait until networkidle0, otherwise fonts won't be loaded.
  await page.goto(htmlPage, {waitUntil: "networkidle0"});

  const keys = await page.evaluate(() => {
    window.marbles = createMarbles();
    return Object.keys(marbles.graphs);
  });

  const renderDiagrams = async (keys) => {
    if (keys.length > 0) {
      const key = keys[0];
      console.log("Rendering marble diagram for " + key);
      const dimensions = await page.evaluate((key) => {
        const diagram = window.document.getElementById("diagram");
        diagram.innerHTML = "";
        return window.marbles.drawSingle(diagram, window.marbles.graphs[key]);
      }, key);
      await page.screenshot({path: outputDir + "/" + key + ".png", clip: {
        x: 0,
        y: 0,
        width: dimensions.width,
        height: dimensions.height
      }});
      keys.shift();
      await renderDiagrams(keys);
    }
  };

  await renderDiagrams(keys);
  await browser.close();
})().catch(error => {
  console.log(error);
  process.exit(1);
});
