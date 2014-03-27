package testrail.testrail.JunitResults;

/*
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
*/

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Drew on 3/24/2014.
 */
/*
@JacksonXmlRootElement(localName = "testsuite")
public class Testsuite {
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    // float time; // not sure how I want to convert to TestRail time yet.
    @JacksonXmlProperty(isAttribute = true)
    private int failures;
    @JacksonXmlProperty(isAttribute = true)
    private int errors;
    @JacksonXmlProperty(isAttribute = true)
    private int skipped;

    @JacksonXmlProperty(localName = "testcase", isAttribute = false)
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Testcase> cases;
    public void setName(String name) { this.name = name; }
    public void setCases(List<Testcase> cases) { this.cases = cases; }
    public void setFailures(int failures) { this.failures = failures; }
    public void setErrors(int errors) { this.errors = errors; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public String getName() { return this.name; }
    public List<Testcase> getCases() { return this.cases; }
    public int getFailures() { return this.failures; }
    public int getErrors() { return this.errors; }
    public int getSkipped() { return this.skipped; }

} */
@XmlRootElement // do I need to tell it the element is "testsuite"?
public class Testsuite {
    private String name;
    private int failures;
    private int errors;
    private int skipped;
    private List<Testcase> cases;

    @XmlAttribute
    public void setName(String name) { this.name = name; }
    @XmlAttribute
    public void setFailures(int failures) { this.failures = failures; }
    @XmlAttribute
    public void setErrors(int errors) { this.errors = errors; }
    @XmlAttribute
    public void setSkipped(int skipped) { this.skipped = skipped; }
    @XmlElement(name = "testcase")
    public void setCases(List<Testcase> cases) { this.cases = cases; }

    public String getName() { return this.name; }
    public List<Testcase> getCases() { return this.cases; }
    public int getFailures() { return this.failures; }
    public int getErrors() { return this.errors; }
    public int getSkipped() { return this.skipped; }




}
