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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import testrail.testrail.JunitResults.Failure;
import testrail.testrail.JunitResults.JUnitResults;
import testrail.testrail.JunitResults.Testcase;
import testrail.testrail.JunitResults.Testsuite;
import testrail.testrail.TestRailObjects.ElementNotFoundException;
import testrail.testrail.TestRailObjects.ExistingTestCases;
import testrail.testrail.TestRailObjects.Result;
import testrail.testrail.TestRailObjects.Results;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class TestRailNotifier extends Notifier {

    private String testrailProject;
    private String testrailSuite;
    private String junitResultsGlob;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TestRailNotifier(String testrailProject, String testrailSuite, String junitResultsGlob) {
        this.testrailProject = testrailProject;
        this.testrailSuite = testrailSuite;
        this.junitResultsGlob = junitResultsGlob;
    }

    public void setTestrailProject(String project) { this.testrailProject = project;}
    public String getTestrailProject() { return this.testrailProject; }
    public void setTestrailSuite(String suite) { this.testrailSuite = suite; }
    public String getTestrailSuite() { return this.testrailSuite; }
    public void setJunitResultsGlob(String glob) { this.junitResultsGlob = glob; }
    public String getJunitResultsGlob() { return this.junitResultsGlob; }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        TestRailClient  testrail = getDescriptor().getTestrailInstance();
        testrail.setHost(getDescriptor().getTestrailHost());
        testrail.setUser(getDescriptor().getTestrailUser());
        testrail.setPassword(getDescriptor().getTestrailPassword());

        ExistingTestCases testCases = null;
        try {
            testCases = new ExistingTestCases(testrail, this.testrailProject, this.testrailSuite);
        } catch (ElementNotFoundException e) {
            listener.getLogger().println("Cannot find project on TestRail server. Please check your Jenkins job and system configurations.");
            return false;
        }

        String[] caseNames = null;
        try {
            caseNames = testCases.listTestCases();
        } catch (ElementNotFoundException e) {
            listener.getLogger().println("Failed to list test cases");
            listener.getLogger().println("Element not found:" + e.getMessage());
        }

        listener.getLogger().println("Munging test result files.");
        Results results = new Results();

        // FilePath doesn't have a read method. We want to actually process the files on the master
        // because during processing we talk to TestRail and slaves might not be able to.
        // So we'll copy the result files to the master and munge them there:
        //
        // Create a temp directory.
        // Do a base.copyRecursiveTo() with file masks into the temp dir.
        // process the temp files.
        // it looks like the destructor deletes the temp dir when we're finished
        FilePath tempdir = new FilePath(Util.createTempDir());
        // This is an ugly way to not process old results.
        // I know that for a new result foo.xml there might be old results for that test
        // named foo.0.xml, foo.1.xml, etc. This excludes those. I hope that it won't
        // also exclude new vaid test results.
        String excludes = "**/*.*0.xml, **/*.*1.xml, **/*.*2.xml, **/*.*3.xml, **/*.*4.xml, **/*.*5.xml, **/*.*6.xml, **/*.*7.xml, **/*.*8.xml, **/*.*9.xml";
        build.getWorkspace().copyRecursiveTo(junitResultsGlob, excludes, tempdir);

        JUnitResults actualJunitResults = null;
        try {
            actualJunitResults = new JUnitResults(tempdir, this.junitResultsGlob, listener.getLogger());
        } catch (JAXBException e) {
            listener.getLogger().println(e.getMessage());
        }
        List<Testsuite> suites = actualJunitResults.getSuites();
        Iterator<Testsuite> testsuiteIterator = suites.iterator();
        while (testsuiteIterator.hasNext()) {
            Testsuite suite = testsuiteIterator.next();
            String junitSuiteName = suite.getName();
            List<Testcase> cases = suite.getCases();

            if (cases == null || cases.isEmpty()) { continue; }
            Iterator<Testcase> iterator = cases.iterator();
            while (iterator.hasNext()) {
                Testcase junitCase = iterator.next();

                String junitCaseName = junitCase.getName();
                int testrailCaseId;

                try {
                    testrailCaseId = testCases.getCaseId(junitSuiteName, junitCaseName);
                } catch (ElementNotFoundException e) {
                    int sectionId;
                    try {
                        sectionId = testCases.getSectionId(junitSuiteName);
                    } catch (ElementNotFoundException e2) {
                        try {
                            sectionId = testCases.addSection(junitSuiteName);
                        } catch (ElementNotFoundException e3) {
                            listener.getLogger().println("Unable to add test section " + junitSuiteName);
                            listener.getLogger().println(e.getMessage());
                            return false;
                        }
                    }
                    testrailCaseId = testCases.addCase(junitCaseName, sectionId);
                }

                int testrailCaseStatus;
                String testrailCaseComment = null;
                Failure testrailCaseFailure = junitCase.getFailure();
                if (testrailCaseFailure != null)  {
                    testrailCaseStatus = 5; // Failed
                    testrailCaseComment = testrailCaseFailure.getText();
                } else {
                    testrailCaseStatus = 1; // Passed
                }
                results.addResult(new Result(testrailCaseId, testrailCaseStatus, testrailCaseComment));
            }
        }

        listener.getLogger().println("Uploading results to TestRail.");
        String runComment = "Automated results from Jenkins: " + BuildWrapper.all().jenkins.getRootUrl() + "/" + build.getUrl().toString();
        int runId = testrail.addRun(testCases.getProjectId(), testCases.getSuiteId(), runComment);
        TestRailResponse response = testrail.addResultsForCases(runId, results);
        boolean buildResult = (200 == response.getStatus());
        if (buildResult) {
            listener.getLogger().println("Successfully uploaded test results.");
        } else {
            listener.getLogger().println("Failed to add results to TestRail.");
            listener.getLogger().println("status: " + response.getStatus());
            listener.getLogger().println("body :\n" + response.getBody());
        }
        testrail.closeRun(runId);

        return buildResult;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE; //null;
    }

    /**
     * Descriptor for {@link TestRailNotifier}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/TestRailRecorder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String testrailHost = "";
        private String testrailUser = "";
        private String testrailPassword = "";
        private TestRailClient testrail = new TestRailClient("", "", "");

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckTestrailProject(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a project name.");
            }
            testrail.setHost(getTestrailHost());
            testrail.setUser(getTestrailUser());
            testrail.setPassword(getTestrailPassword());
            if (!getTestrailHost().isEmpty() || ! getTestrailUser().isEmpty() || !getTestrailPassword().isEmpty() || !testrail.serverReachable() || !testrail.authenticationWorks()) {
                return FormValidation.warning("Please fix your TestRail configuration in Manage Jenkins -> Configure System.");
            }
            try {
                int projectId = testrail.getProjectId(value);
            } catch (ElementNotFoundException e) {
                return FormValidation.error("Project " + value + " not found on TestRail server.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestrailSuite(@QueryParameter String value,
                                                   @QueryParameter String testrailProject)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a suite name.");
            }
            testrail.setHost(getTestrailHost());
            testrail.setUser(getTestrailUser());
            testrail.setPassword(getTestrailPassword());
            if (!getTestrailHost().isEmpty() || ! getTestrailUser().isEmpty() || !getTestrailPassword().isEmpty() || !testrail.serverReachable() || !testrail.authenticationWorks()) {
                return FormValidation.warning("Please fix your TestRail configuration in Manage Jenkins -> Configure System.");
            } else {
                int projectId;
                try {
                    projectId = testrail.getProjectId(testrailProject);
                } catch (ElementNotFoundException e) {
                    return FormValidation.error("Project " + testrailProject + " not found on TestRail server.");
                }
                try {
                    int suiteId = testrail.getSuiteId(projectId, value);
                } catch (ElementNotFoundException e) {
                    return FormValidation.error("Suite " + value + " not found on TestRail server.");
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckJunitResultsGlob(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.warning("Please select test result path.");
            // TODO: Should we check to see if the files exist? Probably not.
            return FormValidation.ok();
        }

        public FormValidation doCheckTestrailHost(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.warning("Please add your TestRail host URI.");
            }
            // TODO: There is probably a better way to do URL validation.
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                return FormValidation.error("Host must be a valid URL.");
            }
            testrail.setHost(value);
            testrail.setUser("");
            testrail.setPassword("");
            if (!testrail.serverReachable()) {
                return FormValidation.error("Host is not reachable.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestrailUser(@QueryParameter String value,
                                                  @QueryParameter String testrailHost,
                                                  @QueryParameter String testrailPassword)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.warning("Please add your user's email address.");
            }
            if (testrailPassword.length() > 0) {
                testrail.setHost(testrailHost);
                testrail.setUser(value);
                testrail.setPassword(testrailPassword);
                if (testrail.serverReachable() && !testrail.authenticationWorks()){
                    return FormValidation.error("Invalid user/password combination.");
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestrailPassword(@QueryParameter String value,
                                                      @QueryParameter String testrailHost,
                                                      @QueryParameter String testrailUser)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.warning("Please add your password.");
            }
            if (testrailUser.length() > 0) {
                testrail.setHost(testrailHost);
                testrail.setUser(testrailUser);
                testrail.setPassword(value);
                if (testrail.serverReachable() && !testrail.authenticationWorks()){
                    return FormValidation.error("Invalid user/password combination.");
                }
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish test results to TestRail";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            testrailHost = formData.getString("testrailHost");
            testrailUser = formData.getString("testrailUser");
            testrailPassword = formData.getString("testrailPassword");

            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setTestrailHost)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public void setTestrailHost(String host) { this.testrailHost = host; }
        public String getTestrailHost() { return testrailHost; }
        public void setTestrailUser(String user) { this.testrailUser = user; }
        public String getTestrailUser() { return testrailUser; }
        public void setTestrailPassword(String password) { this.testrailPassword = password; }
        public String getTestrailPassword() { return testrailPassword; }
        public void setTestrailInstance(TestRailClient trc) { testrail = trc; }
        public TestRailClient getTestrailInstance() { return testrail; }
    }
}
