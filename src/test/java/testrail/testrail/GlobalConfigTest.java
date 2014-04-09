package testrail.testrail;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class GlobalConfigTest {
        @Rule
        public JenkinsRule j = new JenkinsRule();

        JenkinsRule.WebClient webClient;
        HtmlPage page;
        @Before
        public void initialize() throws IOException, SAXException {
            page = j.createWebClient().goTo("configure");
            webClient = (JenkinsRule.WebClient) page.getWebClient();
            webClient.setJavaScriptEnabled(true);
            page.refresh();
        }

        @Test
        public void hostFieldExists() throws Exception {
            WebAssert.assertInputPresent(page, "_.testrailHost");
        }

        @Test public void userFieldExists() throws Exception {
            WebAssert.assertInputPresent(page, "_.testrailUser");
        }

        @Test public void passwordFieldExists() throws Exception {
            WebAssert.assertInputPresent(page, "_.testrailPassword");
        }

        @Test public void warnOnEmptyHost() throws Exception {
            WebAssert.assertInputContainsValue(page, "_.testrailHost", "");
            WebAssert.assertTextPresent(page, "Please add your TestRail host URI.");
        }

        @Test public void warnOnEmptyUser() throws Exception {
            WebAssert.assertInputContainsValue(page, "_.testrailUser", "");
            WebAssert.assertTextPresent(page, "Please add your user's email address.");
        }

        @Test public void warnOnEmptyPassword() throws Exception {
            WebAssert.assertInputContainsValue(page, "_.testrailPassword", "");
            WebAssert.assertTextPresent(page, "Please add your password.");
        }

        @Test public void warnOnBadHostURL() throws Exception {
            HtmlTextInput host = page.getElementByName("_.testrailHost");
            host.setValueAttribute("foo");
            page.getElementByName("_.testrailUser").focus();
            WebAssert.assertInputContainsValue(page, "_.testrailHost", "foo");
            WebAssert.assertTextPresent(page, "Host must be a valid URL.");
        }

        // BUGBUG: This apparently isn't the way to mock trc calls.
        @Test public void warnOnUnreachableHost() throws Exception {
            TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
            TestRailClient testrail = Mockito.mock(TestRailClient.class);
            Mockito.when(testrail.serverReachable()).thenReturn(false);
            descriptor.setTestrailInstance(testrail);

            HtmlTextInput host = page.getElementByName("_.testrailHost");
            host.setValueAttribute("http://unreachable");
            page.getElementByName("_.testrailUser").focus();

            WebAssert.assertInputContainsValue(page, "_.testrailHost", "http://unreachable");
            WebAssert.assertTextPresent(page, "Host is not reachable.");
        }

        @Test public void noWarningOnReachableHost() throws Exception {
            TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
            TestRailClient testrail = Mockito.mock(TestRailClient.class);
            Mockito.when(testrail.serverReachable()).thenReturn(true);
            descriptor.setTestrailInstance(testrail);

            HtmlTextInput host = page.getElementByName("_.testrailHost");
            host.setValueAttribute("http://reachable");
            page.getElementByName("_.testrailUser").focus();

            WebAssert.assertInputContainsValue(page, "_.testrailHost", "http://reachable");
            WebAssert.assertTextNotPresent(page, "Please add your TestRail host URI.");
            WebAssert.assertTextNotPresent(page, "Host must be a valid URL");
            WebAssert.assertTextNotPresent(page, "Host is not reachable.");
        }

        @Test public void warnOnInvalidUserAndPasswordCombo() throws Exception {
            TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
            TestRailClient testrail = Mockito.mock(TestRailClient.class);
            Mockito.when(testrail.serverReachable()).thenReturn(true);
            Mockito.when(testrail.authenticationWorks()).thenReturn(false);
            descriptor.setTestrailInstance(testrail);

            HtmlTextInput host = page.getElementByName("_.testrailHost");
            host.setValueAttribute("http://reachable");
            HtmlTextInput user = page.getElementByName("_.testrailUser");
            user.setValueAttribute("valid_user");
            HtmlPasswordInput password = page.getElementByName("_.testrailPassword");
            password.setValueAttribute("invalid_password");
            page.getElementByName("_.testrailHost").focus();

            WebAssert.assertTextPresent(page, "Invalid user/password combination.");
        }

        @Test public void saveChangesToHost() throws Exception {
            TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
            HtmlTextInput host = page.getElementByName("_.testrailHost");
            host.setValueAttribute("foo");
            j.submit(page.getFormByName("config"));
            assertEquals("foo", descriptor.getTestrailHost());
        }

        @Test public void saveChangesToUser() throws Exception {
            TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
            HtmlTextInput host = page.getElementByName("_.testrailUser");
            host.setValueAttribute("foo");
            j.submit(page.getFormByName("config"));
            assertEquals("foo", descriptor.getTestrailUser());
        }

        @Test public void saveChangesToPassword() throws Exception {
            TestRailNotifier.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(TestRailNotifier.DescriptorImpl.class);
            HtmlPasswordInput host = page.getElementByName("_.testrailPassword");
            host.setValueAttribute("foo");
            j.submit(page.getFormByName("config"));
            assertEquals("foo", descriptor.getTestrailPassword());
        }
}
