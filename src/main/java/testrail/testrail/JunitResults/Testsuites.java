package testrail.testrail.JunitResults;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by achikin on 7/29/14.
 */
@XmlRootElement
public class Testsuites {
    private int failures;
    private int errors;
    private int skipped;
    private int tests;
    private List<Testsuite> suites;
    @XmlElement(name = "testsuite")
    public void setSuites(List<Testsuite> suites) { this.suites = suites; }
    public List<Testsuite> getSuites() { return this.suites; }
    public boolean hasSuites() { return this.suites != null && this.suites.size() > 0; }
}
