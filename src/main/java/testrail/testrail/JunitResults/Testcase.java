package testrail.testrail.JunitResults;
/*
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
*/

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Drew on 3/24/2014.
 */
public class Testcase {
    private String name;
    // float time; // not sure how to convert to TestRail time yet.
    private Failure failure;

    @XmlAttribute
    public void setName(String name) { this.name = name; }
    @XmlElement(name = "failure")
    public void setFailure(Failure failure) { this.failure = failure; }

    public String getName() { return this.name; }
    public Failure getFailure() { return this.failure; }
}
