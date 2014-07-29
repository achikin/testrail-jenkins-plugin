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
        JAXBContext jaxbContext = JAXBContext.newInstance(Testsuites.class);
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
                        Testsuites suites = null;
                        logger.println("processing " + file.getName());
                        try {
                            suites = (Testsuites) jaxbUnmarshaller.unmarshal(file);
                        } catch (JAXBException e) {
                            e.printStackTrace();
                        }
                        if (suites.hasSuites()) {
                            for (Testsuite suite : suites.getSuites()) {
                                Suites.add(suite);
                            }
                        }
                    }
                });
                return null;
            }
        });
    }

    public List<Testsuite> getSuites() {
        return this.Suites;
    }

    public String[] getFiles() { return this.Files; }
}
