package testrail.testrail;

import hudson.Extension;
import hudson.Launcher;
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

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link TestRailNotifier} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #testrailSuite})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class TestRailNotifier extends Notifier {

    private final String testrailProject;
    private final String testrailSuite;
    private final String junitResults;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TestRailNotifier(String testrailProject, String testrailSuite, String junitResults) {
        this.testrailProject = testrailProject;
        this.testrailSuite = testrailSuite;
        this.junitResults = junitResults;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getTestrailProject() { return this.testrailProject; }
    public String getTestrailSuite() { return this.testrailSuite; }
    public String getJunitResults() { return this.junitResults; }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException {
        TestRailClient testrail = new TestRailClient(
                getDescriptor().getTestrailHost(),
                getDescriptor().getTestrailUser(),
                getDescriptor().getTestrailPassword());

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
        String base = String.valueOf(build.getWorkspace());
        String [] includesDirs = { this.junitResults };
        JUnitResults junitResults = null;
        try {
            junitResults = new JUnitResults(base, includesDirs);
        } catch (JAXBException e) {
            listener.getLogger().println(e.getMessage());
        }
        List<Testsuite> suites = junitResults.getSuites();
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
            listener.getLogger().println("Successfully uploaded test reaults.");
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
        private String testrailHost;
        private String testrailUser;
        private String testrailPassword;

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
            TestRailClient testrail = new TestRailClient(getTestrailHost(),
                    getTestrailUser(),
                    getTestrailPassword());
            if (!testrail.serverReachable() || !testrail.authenticationWorks()) {
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
            TestRailClient testrail = new TestRailClient(getTestrailHost(),
                    getTestrailUser(),
                    getTestrailPassword());
            if (!testrail.serverReachable() || !testrail.authenticationWorks()) {
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

        public FormValidation doCheckJunitResults(@QueryParameter String value)
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
            TestRailClient testrail = new TestRailClient(value, "", "");
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
                TestRailClient testrail = new TestRailClient(testrailHost, value, testrailPassword);
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
                TestRailClient testrail = new TestRailClient(testrailHost, testrailUser, value);
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
            return "TestRail";
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
        public String getTestrailHost() { return testrailHost; }
        public String getTestrailUser() { return testrailUser; }
        public String getTestrailPassword() { return testrailPassword; }
    }
}
