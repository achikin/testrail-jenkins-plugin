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
package testrail.testrail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import testrail.testrail.TestRailObjects.*;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Drew on 3/19/14.
 */
public class TestRailClient {
    private String host;
    private String user;
    private String password;
    private ObjectMapper objectMapper;

    public void setHost(String host) { this.host = host; }
    public void setUser(String user) { this.user = user; }
    public void setPassword(String password) {this.password = password; }
    public String getHost() { return this.host; }
    public String getUser() { return this.user; }
    public String getPassword() { return this.password; }

    public TestRailClient(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private HttpClient setUpHttpClient(HttpMethod method) {
        HttpClient httpclient = new HttpClient();
        httpclient.getParams().setAuthenticationPreemptive(true);
        httpclient.getState().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(this.user, this.password)
        );
        method.setDoAuthentication(true);
        method.addRequestHeader("Content-Type", "application/json");
        return httpclient;
    }

    private TestRailResponse httpGet(String path) throws IOException {
        TestRailResponse result;
        GetMethod get = new GetMethod(host + "/" + path);
        HttpClient httpclient = setUpHttpClient(get);

        try {
            Integer status = httpclient.executeMethod(get);
            String body = new String(get.getResponseBody(), get.getResponseCharSet());
            result = new TestRailResponse(status, body);
        } finally {
            get.releaseConnection();
        }

        return result;
    }

    private TestRailResponse httpPost(String path, String payload)
            throws UnsupportedEncodingException, IOException, HTTPException {
        TestRailResponse result;
        PostMethod post = new PostMethod(host + "/" + path);
        HttpClient httpclient = setUpHttpClient(post);

        try {
            StringRequestEntity requestEntity = new StringRequestEntity(
                    payload,
                    "application/json",
                    "UTF-8"
            );
            post.setRequestEntity(requestEntity);
            Integer status = httpclient.executeMethod(post);
            String body = new String(post.getResponseBody(), post.getResponseCharSet());
            result = new TestRailResponse(status, body);
        } finally {
            post.releaseConnection();
        }

        return result;
    }

    public boolean serverReachable() throws IOException {
        boolean result = false;
        HttpClient httpclient = new HttpClient();
        GetMethod get = new GetMethod(host);
        try {
            httpclient.executeMethod(get);
            result = true;
        } catch (java.net.UnknownHostException e) {
            // nop - we default to result == false
        } finally {
            get.releaseConnection();
        }
        return result;
    }

    public boolean authenticationWorks() throws IOException {
        TestRailResponse response = httpGet("/index.php?/api/v2/get_projects");
        return (200 == response.getStatus());
    }

    public int getProjectId(String projectName) throws IOException, ElementNotFoundException {
        String body = httpGet("/index.php?/api/v2/get_projects").getBody();
        Project[] projects = this.objectMapper.readValue(body, Project[].class);
        for(int i = 0; i < projects.length; i++) {
            if (projects[i].getName().equals(projectName)) {
                return projects[i].getId();
            }
        }
        throw new ElementNotFoundException(projectName);
    }

    public int getSuiteId(int projectId, String suiteName) throws IOException, ElementNotFoundException {
        String body = httpGet("/index.php?/api/v2/get_suites/" + projectId).getBody();
        Suite[] suites = this.objectMapper.readValue(body, Suite[].class);
        for (int i = 0; i < suites.length; i++) {
            if (suites[i].getName().equals(suiteName)) {
                return suites[i].getId();
            }
        }
        throw new ElementNotFoundException(suiteName);
    }

    public String getCasesString(int projectId, int suiteId) {
        String result = "index.php?/api/v2/get_cases/" + projectId + "&suite_id=" + suiteId;
        return result;
    }

    public Case[] getCases(int projectId, int suiteId) throws IOException, ElementNotFoundException {
        // "/#{project_id}&suite_id=#{suite_id}#{section_string}"
        String body = httpGet("index.php?/api/v2/get_cases/" + projectId + "&suite_id=" + suiteId).getBody();
        return this.objectMapper.readValue(body, Case[].class);
    }

    public Section[] getSections(int projectId, int suiteId) throws IOException, ElementNotFoundException {
        String body = httpGet("index.php?/api/v2/get_sections/" + projectId + "&suite_id=" + suiteId).getBody();
        return this.objectMapper.readValue(body, Section[].class);
    }

    public Section addSection(String sectionName, int projectId, int suiteId, Integer parentId) throws IOException, ElementNotFoundException {
        Section section = new Section();
        section.setName(sectionName);
        section.setSuiteId(suiteId);
        section.setParentId(parentId);
        String payload = this.objectMapper.writeValueAsString(section);
        String body = httpPost("index.php?/api/v2/add_section/" + projectId , payload).getBody();
        return this.objectMapper.readValue(body, Section.class);
    }

    public Case addCase(String caseTitle, int sectionId) throws IOException {
        Case testcase = new Case();
        testcase.setTitle(caseTitle);
        String payload = this.objectMapper.writeValueAsString(testcase);
        String body = httpPost("index.php?/api/v2/add_case/" + sectionId, payload).getBody();
        return this.objectMapper.readValue(body, Case.class);
    }

    public TestRailResponse addResultsForCases(int runId, Results results) throws IOException {
        String payload = this.objectMapper.writeValueAsString(results);
        TestRailResponse response = httpPost("index.php?/api/v2/add_results_for_cases/" + runId, payload);
        return response;
    }

    public int addRun(int projectId, int suiteId, String description)
            throws JsonProcessingException, UnsupportedEncodingException, IOException {
        Run run = new Run();
        run.setSuiteId(suiteId);
        run.setDescription(description);
        String payload = this.objectMapper.writeValueAsString(run);
        String body = httpPost("index.php?/api/v2/add_run/" + projectId, payload).getBody();
        Run result = this.objectMapper.readValue(body, Run.class);
        return result.getId();
    }

    public boolean closeRun(int runId)
            throws JsonProcessingException, UnsupportedEncodingException, IOException {
        String payload = "";
        int status = httpPost("index.php?/api/v2/close_run/" + runId, payload).getStatus();
        return (200 == status);
    }
}
