package testrail.testrail.TestRailObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Drew on 3/24/2014.
 */
public class Case {
    private int id;
    private String title;
    private int sectionId;

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("section_id")
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public int getSectionId() { return this.sectionId; }
}
