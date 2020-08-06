/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.testrail.TestRail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Drew on 3/25/2014.
 */
public class TestRailResults {

    private List<TestRailResult> results;

    public TestRailResults() {
        this.results = new ArrayList<TestRailResult>();
    }

    public void setResults(ArrayList<TestRailResult> results) { this.results = results; }
    public void addResult(TestRailResult result) { this.results.add(result); }
    public List<TestRailResult> getResults() { return this.results; }
    public void merge(TestRailResults other) { this.results.addAll(other.getResults()); }
}
