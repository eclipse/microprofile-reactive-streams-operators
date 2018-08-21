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

const path = require("path");
const projectBaseDir = path.normalize(__dirname + "/../../..");
const hashesFile = projectBaseDir + "/src/docs/js/marble-diagram-hashes.json";

const crypto = require("crypto");
const fs = require("fs");

if (!fs.existsSync(hashesFile)) {
  console.log(hashesFile + " not found! Cannot verify that marble diagrams are up to date.");
  process.exit(-1);
}

const files = JSON.parse(fs.readFileSync(hashesFile)).files;
let conflicts = [];
Object.keys(files).forEach(file => {
  const path = projectBaseDir + "/" + file;
  if (!fs.existsSync(path)) {
    conflicts.push(file);
  } else {
    const md5sum = crypto.createHash('md5');
    const data = fs.readFileSync(file);
    md5sum.update(data);
    const md5 = md5sum.digest("hex");
    if (files[file] !== md5) {
      conflicts.push(file);
    }
  }
});

if (conflicts.length !== 0) {
  console.log("Marble diagram input or output files have changed since diagrams were last generated:");
  conflicts.forEach(file => {
    console.log("  " + file);
  });
  console.log("To fix this, run:");
  console.log("  mvn -Pmarble-diagrams verify");
  console.log("If you are getting this error from a CI build, the reason is that the MicroProfile CI and release");
  console.log("environment does not have the necessary dependencies installed to run the diagram generation process,");
  console.log("hence the diagrams are checked into revision control, and this script ensures they are up to date.");
  console.log("After running the above maven command, add the modified diagrams and marble-diagram-hashes.json file");
  console.log("to git and commit.");
  process.exit(-1);
}
