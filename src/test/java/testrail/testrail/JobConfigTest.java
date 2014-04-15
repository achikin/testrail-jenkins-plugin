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
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import hudson.model.FreeStyleProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.xml.sax.SAXException;
import testrail.testrail.TestRailObjects.ElementNotFoundException;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

public class JobConfigTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    HtmlPage page;
    FreeStyleProject project;
    @Before
    public void initialize() throws IOException, SAXException, ElementNotFoundException {
        project = j.createFreeStyleProject("testProject");
        TestRailNotifier notifier = new TestRailNotifier("","","");
        project.getPublishersList().add(notifier);
        page = j.createWebClient().goTo("job/testProject/configure");
    }

    @Test
    public void projectFieldExists() {
        WebAssert.assertInputPresent(page, "_.testrailProject");
    }

    @Test public void suiteFieldExists() {
        WebAssert.assertInputPresent(page, "_.testrailSuite");
    }

    @Test public void resultsGlobFieldExists() {
        WebAssert.assertInputPresent(page, "_.junitResultsGlob");
    }

    @Test public void warnOnNoProject() {
        WebAssert.assertInputContainsValue(page, "_.testrailProject", "");
        WebAssert.assertTextPresent(page, "Please set a project name.");
    }

    @Test public void warnOnNoSuite() {
        WebAssert.assertInputContainsValue(page, "_.testrailSuite", "");
        WebAssert.assertTextPresent(page, "Please set a suite name.");
    }

    @Test public void warnOnNoResultsGlob() {
        WebAssert.assertInputContainsValue(page, "_.junitResultsGlob", "");
        WebAssert.assertTextPresent(page, "Please select test result path.");
    }

    @Test public void warnOnBadGlobalConfigAndNonEmptyProject() throws IOException, SAXException {
        TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
        TestRailClient testrail = Mockito.mock(TestRailClient.class);
        Mockito.when(testrail.serverReachable()).thenReturn(false);
        Mockito.when(testrail.authenticationWorks()).thenReturn(false);
        descriptor.setTestrailInstance(testrail);
        page = j.createWebClient().goTo("job/testProject/configure");

        String projectName = "foo";
        HtmlTextInput host = page.getElementByName("_.testrailProject");
        host.setValueAttribute(projectName);

        // WebAssert.assertTextPresent(page, "Project " + projectName + " not found on TestRail server.");
        // BUGBUG: Is showing "Please set a project name".
        WebAssert.assertTextPresent(page, "Please fix your TestRail configuration in Manage Jenkins -> Configure System.");
    }

    // The same "fix your configuration" message shows up on the page for project and suite fields.

    @Test public void warnOnInvalidProject() throws IOException, SAXException, ElementNotFoundException {
        TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
        TestRailClient testrail = Mockito.mock(TestRailClient.class);
        Mockito.when(testrail.serverReachable()).thenReturn(true);
        Mockito.when(testrail.authenticationWorks()).thenReturn(true);
        Mockito.when(testrail.getProjectId(anyString())).thenThrow(new ElementNotFoundException("fake project name"));
        descriptor.setTestrailInstance(testrail);
        page = j.createWebClient().goTo("job/testProject/configure");

        String projectName = "foo";
        HtmlTextInput project = page.getElementByName("_.testrailProject");
        project.setValueAttribute(projectName);

        WebAssert.assertTextPresent(page, "Project " + projectName + " not found on TestRail server.");
    }

    @Test public void warnOnInvalidSuite() throws IOException, ElementNotFoundException, SAXException {
        TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
        TestRailClient testrail = Mockito.mock(TestRailClient.class);
        Mockito.when(testrail.serverReachable()).thenReturn(true);
        Mockito.when(testrail.authenticationWorks()).thenReturn(true);
        Mockito.when(testrail.getProjectId(anyString())).thenReturn(1);
        //Mockito.when(testrail.getSuiteId(any(Integer.class), any(String.class))).thenThrow(new ElementNotFoundException("fake suite name"));
        Mockito.when(testrail.getSuiteId(eq(1), anyString())).thenThrow(new ElementNotFoundException("fake suite name"));
        descriptor.setTestrailInstance(testrail);
        page = j.createWebClient().goTo("job/testProject/configure");

        HtmlTextInput project = page.getElementByName("_.testrailProject");
        project.setValueAttribute("foo");
        String suiteName = "bar";
        HtmlTextInput suite = page.getElementByName("_.testrailSuite");
        suite.setValueAttribute(suiteName);

        WebAssert.assertTextPresent(page, "Suite " + suiteName + " not found on TestRail server.");
    }

    @Test public void noWarningOnValidProject() throws IOException, ElementNotFoundException, SAXException {
        TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
        TestRailClient testrail = Mockito.mock(TestRailClient.class);
        Mockito.when(testrail.serverReachable()).thenReturn(true);
        Mockito.when(testrail.authenticationWorks()).thenReturn(true);
        Mockito.when(testrail.getProjectId(anyString())).thenReturn(1);
        Mockito.when(testrail.getSuiteId(eq(1), anyString())).thenThrow(new ElementNotFoundException("fake suite name"));
        descriptor.setTestrailInstance(testrail);
        page = j.createWebClient().goTo("job/testProject/configure");

        String projectName = "foo";
        HtmlTextInput project = page.getElementByName("_.testrailProject");
        project.setValueAttribute(projectName);

        WebAssert.assertTextNotPresent(page, "Project " + projectName + " not found on TestRail server.");
        WebAssert.assertTextNotPresent(page, "Please fix your TestRail configuration in Manage Jenkins -> Configure System.");
    }

    @Test public void noWarningOnValidSuite() throws IOException, ElementNotFoundException, SAXException {
        TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
        TestRailClient testrail = Mockito.mock(TestRailClient.class);
        Mockito.when(testrail.serverReachable()).thenReturn(true);
        Mockito.when(testrail.authenticationWorks()).thenReturn(true);
        Mockito.when(testrail.getProjectId(anyString())).thenReturn(1);
        Mockito.when(testrail.getSuiteId(eq(1), anyString())).thenReturn(1);
        descriptor.setTestrailInstance(testrail);
        page = j.createWebClient().goTo("job/testProject/configure");

        HtmlTextInput project = page.getElementByName("_.testrailProject");
        project.setValueAttribute("foo");
        String suiteName = "bar";
        HtmlTextInput suite = page.getElementByName("_.testrailSuite");
        suite.setValueAttribute(suiteName);

        WebAssert.assertTextNotPresent(page, "Suite " + suiteName + " not found on TestRail server.");
        WebAssert.assertTextNotPresent(page, "Please fix your TestRail configuration in Manage Jenkins -> Configure System.");
    }
}
