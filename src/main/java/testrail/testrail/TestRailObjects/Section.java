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
package testrail.testrail.TestRailObjects;

/**
 * Created by Drew on 3/24/2014.
 */
public class Section {
    private int id;
    private int suiteId;
    private String name;
    private String parentId;

    public void setId(int id) {
        this.id = id;
    }
    public void  setSuiteId(int suiteId) { this.suiteId = suiteId; }
    public void setName(String name) {
        this.name = name.trim();
    }
    public void setParentId(String id) { this.parentId = id; }

    public int getId() {
        return this.id;
    }
    public int getSuiteId() { return this.suiteId; }
    public String getName() {
        return this.name;
    }
    public String getParentId() { return this.parentId; }
}
