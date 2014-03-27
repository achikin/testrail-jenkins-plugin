package testrail.testrail.TestRailObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Drew on 3/24/2014.
 */
public class Run {
    private int suiteId;
    private int id;
    private String description;

    @JsonProperty("suite_id")
    public void setSuiteId(int suiteId) {
        this.suiteId = suiteId;
    }
    public void setId(int id) { this.id = id; }
    @JsonProperty("description")
    public void setDescription(String description) { this.description = description; }

    public int getSuiteId() {
        return this.suiteId;
    }
    public int getId() {
        return this.id;
    }
    public String getDescription() { return this.description; }
}
