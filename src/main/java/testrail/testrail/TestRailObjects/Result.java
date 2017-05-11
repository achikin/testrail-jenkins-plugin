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
 * Created by Drew on 3/25/2014.
 */
public class Result {
    private int caseId;
    private int statusId;
    private Float elapsed;
    private String message;
    private String text;

    public Result(int caseId, int statusId, String message, Float elapsed, String text) {
        this.caseId = caseId;
        this.statusId = statusId;
        this.message = message;
        this.elapsed = elapsed;
        this.text = text;
    }

    public void setCaseId(int caseId) { this.caseId = caseId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    public void setElapsed(float timeInSeconds) { this.elapsed = timeInSeconds; }
    public void setMessage(String message) { this.message = message; }
    public void setText(String text) { this.text = text; }

    public int getCaseId() { return this.caseId; }
    public int getStatusId() { return this.statusId; }
    public Float getElapsed() { return this.elapsed; }
    public String getMessage() { return this.message; }
    public String getText() { return this.text; }

    public String getElapsedTimeString() {
        return elapsed.intValue() + "s";
    }
}
