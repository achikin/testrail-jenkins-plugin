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
package org.jenkinsci.plugins.testrail.JUnit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by Drew on 3/26/2014.
 */
public class Failure {
    private String type;
    private String message;
    private String text;

    @XmlAttribute
    public void setType(String type) { this.type = type; }
    @XmlAttribute
    public void  setMessage(String message) { this.message = message; }
    @XmlValue
    public void setText(String text) { this.text = text; }

    public String getType() { return this.type; }
    public String getMessage() { return this.message; }
    public String getText() { return this.text; }
}
