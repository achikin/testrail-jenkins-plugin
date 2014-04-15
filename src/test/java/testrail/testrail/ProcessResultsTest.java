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
        Mockito.when(testrail.addRun(eq(1), eq(1), anyString())).thenReturn(1);
        Mockito.when(testrail.addSection(anyString(), eq(1), eq(1))).thenAnswer(new Answer<Section>() {
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
        WebAssert.assertTextPresent(page, "Successfully uploaded test reaults");
    }

    @Test
    public void noResults() throws ExecutionException, InterruptedException, IOException, ElementNotFoundException, SAXException {
        TestRailNotifier notifier = new TestRailNotifier("project","suite","*.xml");
        project.getPublishersList().add(notifier);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();
    }

    @Test public void resultsNotNested() throws ExecutionException, InterruptedException, IOException {
        TestRailNotifier notifier = new TestRailNotifier("project","suite","*.xml");
        project.getPublishersList().add(notifier);
        project.getBuildersList().add(
            new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                    String result = "<testsuite name=\"section1\"><testcase name=\"case1\"></testcase></testsuite>";
                    build.getWorkspace().child("result.xml").write(result, "UTF-8");
                    return true;
                }
            }
        );
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();

        // Should probably also verify that some mocks were hit.
    }

    @Test public void nestedResults() throws ExecutionException, InterruptedException, IOException, SAXException {
        TestRailNotifier notifier = new TestRailNotifier("project","suite","**/*.xml");
        project.getPublishersList().add(notifier);
        project.getBuildersList().add(
            new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                    String result1 = "<testsuite name=\"section1\"><testcase name=\"case1\"></testcase></testsuite>";
                    build.getWorkspace().child("result.xml").write(result1, "UTF-8");
                    String result2 = "<testsuite name=\"section2\"><testcase name=\"case1\"></testcase></testsuite>";
                    build.getWorkspace().child("nested").child("more_results.xml").write(result2, "UTF-8");
                    String result3 = "<testsuite name=\"section2\"><testcase name=\"case1\"></testcase></testsuite>";
                    build.getWorkspace().child("nested2").child("more_nesting").child("still_more_results.xml").write(result3, "UTF-8");
                    return true;
                }
            }
        );
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();

        // Should probably also verify that some mocks were hit.
    }

    @Test public void parseSuccess() throws ExecutionException, InterruptedException, IOException, SAXException {
        TestRailNotifier notifier = new TestRailNotifier("project","suite","*.xml");
        project.getPublishersList().add(notifier);
        project.getBuildersList().add(
            new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                    String result = "<testsuite name=\"section1\"><testcase name=\"case1\"></testcase></testsuite>";
                    build.getWorkspace().child("result.xml").write(result, "UTF-8");
                    return true;
                }
            }
        );
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();

        // Should probably also verify that some mocks were hit.
    }

    @Test public void parseMultipleResultsInFile() throws ExecutionException, InterruptedException {
        TestRailNotifier notifier = new TestRailNotifier("project","suite","*.xml");
        project.getPublishersList().add(notifier);
        project.getBuildersList().add(
            new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                    String result = "<testsuite name=\"section2\"><testcase name=\"case1\"></testcase><testcase name=\"case2\"></testcase><testcase name=\"case3\">ERRROR</testcase></testsuite>";
                    build.getWorkspace().child("results.xml").write(result, "UTF-8");
                    return true;
                }
            }
        );
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();

        // Should probably also verify that some mocks were hit.
    }

    @Test public void parseError() throws ExecutionException, InterruptedException, IOException, SAXException {
        TestRailNotifier notifier = new TestRailNotifier("project","suite","*.xml");
        project.getPublishersList().add(notifier);
        project.getBuildersList().add(
            new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                    String result = "<testsuite name=\"section1\"><testcase name=\"case1\">This failed in some way.</testcase></testsuite>";
                    build.getWorkspace().child("result.xml").write(result, "UTF-8");
                    return false;
                }
            }
        );
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();

        // Should probably also verify that some mocks were hit.
    }

    @Test public void processSingleResultFromSlave() throws Exception {
        DumbSlave slave = j.createSlave();
        SlaveComputer computer = slave.getComputer();
        computer.connect(false).get(); // apparently this spins until connected
        LabelAtom label = slave.getSelfLabel();
        TestRailNotifier notifier = new TestRailNotifier("project", "suite", "*.xml");
        project.getPublishersList().add(notifier);
        project.setAssignedLabel(label);
        project.getBuildersList().add(
            new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                    String result = "<testsuite name=\"section1\"><testcase name=\"case1\"></testcase></testsuite>";
                    build.getWorkspace().child("result.xml").write(result, "UTF-8");
                    return true;
                }
            }
        );
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        build.run();

        // Should probably also verify that some mocks were hit.
    }

    // There aren't any tests to see what happens when we update existing results yet.
}
