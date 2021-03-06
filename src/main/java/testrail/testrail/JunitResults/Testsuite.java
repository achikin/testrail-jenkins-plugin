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
package testrail.testrail.JunitResults;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Drew on 3/24/2014.
 */
@XmlRootElement
public class Testsuite {
    private String name;
    private int failures;
    private int errors;
    private int skipped;
    private List<Testcase> cases;
    private List<Testsuite> suits;
    @XmlAttribute
    public void setName(String name) { this.name = name.trim(); }
    @XmlAttribute
    public void setFailures(int failures) { this.failures = failures; }
    @XmlAttribute
    public void setErrors(int errors) { this.errors = errors; }
    @XmlAttribute
    public void setSkipped(int skipped) { this.skipped = skipped; }
    @XmlElement(name = "testcase")
    public void setCases(List<Testcase> cases) { this.cases = cases; }
    @XmlElement(name = "testsuite")
    public void setSuits(List<Testsuite> suits) { this.suits = suits; }

    public String getName() { return this.name; }
    public List<Testcase> getCases() { return this.cases; }
    public List<Testsuite> getSuits() {return this.suits; }
    public int getFailures() { return this.failures; }
    public int getErrors() { return this.errors; }
    public int getSkipped() { return this.skipped; }
    public boolean hasSuits() { return this.suits != null; }
    public boolean hasCases() { return this.cases != null; }
}
