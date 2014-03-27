package testrail.testrail.JunitResults;

import org.apache.tools.ant.DirectoryScanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Drew on 3/24/2014.
 */
public class JUnitResults {
    private String baseDir;
    private String[] Files;
    private List<Testsuite> Suites;

    public JUnitResults(String baseDir, String[] fileMatchers) throws IOException, JAXBException {
        this.baseDir = baseDir;
        slurpTestResults(fileMatchers);
    }

    private void ScanForFiles(String[] fileMatchers) {
        DirectoryScanner scanner = new DirectoryScanner();
        String[] includesDirs = fileMatchers;
        scanner.setIncludes(includesDirs);
        scanner.setBasedir(this.baseDir);
        scanner.scan();
        this.Files = scanner.getIncludedFiles();
    }

    private void slurpTestResults(String[] fileMatchers) throws IOException, JAXBException {
        Suites = new ArrayList<Testsuite>();

        ScanForFiles(fileMatchers);

        JAXBContext jaxbContext = JAXBContext.newInstance(Testsuite.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        for (int i = 0; i < Files.length; i++) {
            Testsuite suite = (Testsuite) jaxbUnmarshaller.unmarshal(new File(this.baseDir, Files[i]));
            Suites.add(suite);
        }
    }

    public List<Testsuite> getSuites() {
        return this.Suites;
    }

    public String[] getFiles() { return this.Files; }
}
