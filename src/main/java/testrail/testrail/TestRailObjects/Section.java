package testrail.testrail.TestRailObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Drew on 3/24/2014.
 */
public class Section {
    private int id;
    @JsonProperty("suite_id")
    private int suiteId;
    private String name;
    // private int parentId;
    // private int depth;

    public void setId(int id) {
        this.id = id;
    }
    public void  setSuiteId(int suiteId) { this.suiteId = suiteId; }
    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }
    public int getSuiteId() { return this.suiteId; }
    public String getName() {
        return this.name;
    }
}
