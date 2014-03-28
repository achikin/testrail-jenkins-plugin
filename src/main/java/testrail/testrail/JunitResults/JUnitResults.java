package testrail.testrail.JunitResults;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import hudson.util.DirScanner;
import hudson.util.FileVisitor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Drew on 3/24/2014.
 */
public class JUnitResults {
    private FilePath baseDir;
    private PrintStream logger;
    private String[] Files;
    private List<Testsuite> Suites;

    public JUnitResults(FilePath baseDir, String fileMatchers, PrintStream logger) throws IOException, JAXBException, InterruptedException {
        this.baseDir = baseDir;
        this.logger = logger;
        slurpTestResults(fileMatchers);
    }

    public void slurpTestResults(String fileMatchers) throws IOException, JAXBException, InterruptedException {
        Suites = new ArrayList<Testsuite>();
        JAXBContext jaxbContext = JAXBContext.newInstance(Testsuite.class);
        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        final DirScanner scanner = new DirScanner.Glob(fileMatchers, null);
        logger.println("Scanning " + baseDir);

        baseDir.act(new FilePath.FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                Testsuite suite = null;
                logger.println("processing " + f.getName());
                scanner.scan(f, new FileVisitor() {
                    @Override
                    public void visit(File file, String s) throws IOException {
                        Testsuite suite = null;
                        logger.println("processing " + file.getName());
                        try {
                            suite = (Testsuite) jaxbUnmarshaller.unmarshal(file);
                        } catch (JAXBException e) {
                            e.printStackTrace();
                        }
                        Suites.add(suite);
                    }
                });
                return null;
            }});
        }

    /*    scanner.scan(new File(baseDir.toURI()), new FileVisitor() {
                    @Override
                    public void visit(File file, String s) throws IOException {
                        Testsuite suite = null;
                        logger.println("processing " + file.getName());
                        try {
                            suite = (Testsuite) jaxbUnmarshaller.unmarshal(file);
                        } catch (JAXBException e) {
                            e.printStackTrace();
                        }
                        Suites.add(suite);
                    }
                });*/
    //}

    public List<Testsuite> getSuites() {
        return this.Suites;
    }

    public String[] getFiles() { return this.Files; }
}
