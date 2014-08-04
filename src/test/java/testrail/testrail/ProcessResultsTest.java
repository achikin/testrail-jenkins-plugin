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

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;
import testrail.testrail.TestRailObjects.Case;
import testrail.testrail.TestRailObjects.ElementNotFoundException;
import testrail.testrail.TestRailObjects.Results;
import testrail.testrail.TestRailObjects.Section;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.*;

public class ProcessResultsTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    FreeStyleProject project;
    TestRailClient testrail;
    @Before
    public void setup() throws IOException, ElementNotFoundException {
        project = j.createFreeStyleProject("testProject");

        TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
        testrail = Mockito.mock(TestRailClient.class);
        Mockito.when(testrail.serverReachable()).thenReturn(true);
        Mockito.when(testrail.authenticationWorks()).thenReturn(true);
        Mockito.when(testrail.getProjectId(anyString())).thenReturn(1);
        Mockito.when(testrail.getSuiteId(eq(1), anyString())).thenReturn(1);
        Mockito.when(testrail.getCases(eq(1), eq(1))).thenReturn(new Case[]{});
        Mockito.when(testrail.addCase(anyString(), eq(1))).thenAnswer(new Answer<Case>() {
           public Case answer(InvocationOnMock invocation) throws Throwable {
               Object[] args = invocation.getArguments();
               Case result = new Case();
               result.setId(1);
               result.setTitle((String)args[0]);
               result.setSectionId((Integer)args[1]);
               return result;
           }
        });
        Mockito.when(testrail.getSections(eq(1), eq(1))).thenReturn(new Section[]{});
        Mockito.when(testrail.addRun(eq(1), eq(1), anyString(), anyString())).thenReturn(1);
        Mockito.when(testrail.addSection(anyString(), eq(1), eq(1), eq(1))).thenAnswer(new Answer<Section>() {
            public Section answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Section result = new Section();
                result.setName((String)args[0]);
                result.setId(1);
                result.setSuiteId((Integer)args[2]);
                return result;
            }
        });
        TestRailResponse resp = new TestRailResponse(200, "fake result");
        Mockito.when(testrail.addResultsForCases(eq(1), any(Results.class))).thenReturn(resp);

        descriptor.setTestrailInstance(testrail);
        descriptor.setTestrailHost("host");
        descriptor.setTestrailUser("user");
        descriptor.setTestrailPassword("password");
    }

    @After public void teardown() throws IOException, SAXException {
        HtmlPage page = j.createWebClient().goTo("job/testProject/lastBuild/console");
        System.out.println("page: " + page.asText());
        WebAssert.assertTextPresent(page, "Successfully uploaded test results");
    }
}
