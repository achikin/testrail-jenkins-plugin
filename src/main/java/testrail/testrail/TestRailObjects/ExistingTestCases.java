package testrail.testrail.TestRailObjects;

import testrail.testrail.TestRailClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Drew on 3/24/2014.
 */
public class ExistingTestCases {
    private TestRailClient testRailClient;
    private String project;
    private int projectId;
    private String suite;
    private int suiteId;
    private List<Case> cases;
    private List<Section> sections;;

    public ExistingTestCases(TestRailClient testRailClient, String project, String suite)
            throws IOException, ElementNotFoundException {
        this.project = project;
        this.testRailClient = testRailClient;
        this.projectId = testRailClient.getProjectId(this.project);
        this.suite = suite;
        this.suiteId = testRailClient.getSuiteId(this.projectId, this.suite);
        this.cases = new ArrayList<Case>(Arrays.asList(testRailClient.getCases(this.projectId, this.suiteId)));
        this.sections = new ArrayList<Section>(Arrays.asList(testRailClient.getSections(this.projectId, this.suiteId)));
    }

    public int getProjectId() {
        return this.projectId;
    }

    public int getSuiteId() {
        return this.suiteId;
    }

    public List<Case> getCases() {
        return this.cases;
    }

    public List<Section> getSections() {
        return this.sections;
    }

    private String getSectionName(int sectionId) throws ElementNotFoundException {
        Iterator<Section> iterator = sections.iterator();
        while (iterator.hasNext()) {
            Section section = iterator.next();
            if (section.getId() == sectionId) {
                return section.getName();
            }
        }
        throw new ElementNotFoundException("sectionId: " + sectionId);
    }

    public int getCaseId(String sectionName, String caseName) throws ElementNotFoundException {
        Iterator<Case> caseIterator = cases.iterator();
        while (caseIterator.hasNext()) {
            Case testcase = caseIterator.next();
            if (testcase.getTitle().equals(caseName)) {
                Iterator<Section> sectionIterator = sections.iterator();
                while (sectionIterator.hasNext()) {
                    Section section = sectionIterator.next();
                    if (section.getName().equals(sectionName) && (testcase.getSectionId() == section.getId())) {
                        return testcase.getId();
                    }
                }
            }
        }
        throw new ElementNotFoundException(sectionName + ": " + caseName);
    }

    public int getSectionId(String sectionName) throws ElementNotFoundException {
        Iterator<Section> iterator = sections.iterator();
        while (iterator.hasNext()) {
            Section section = iterator.next();
            if (section.getName().equals(sectionName)) {
                return section.getId();
            }
        }
        throw new ElementNotFoundException(sectionName);
    }

    public int addSection(String sectionName) throws IOException, ElementNotFoundException {
        Section addedSection = testRailClient.addSection(sectionName, projectId, suiteId);
        sections.add(addedSection);
        return addedSection.getId();
    }

    public int addCase(String caseName, int sectionId) throws IOException {
        Case addedCase = testRailClient.addCase(caseName, sectionId);
        cases.add(addedCase);
        return addedCase.getId();
    }

    public String[] listTestCases() throws ElementNotFoundException {
        ArrayList<String> result = new ArrayList<String>();
        Iterator<Case> caseIterator = cases.iterator();
        while (caseIterator.hasNext()) {
            Case testcase = caseIterator.next();
            String sectionName = getSectionName(testcase.getSectionId());
            result.add(sectionName + ": " + testcase.getTitle());
        }
        return result.toArray(new String[result.size()]);
    }
}

