package testrail.testrail.TestRailObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Drew on 3/25/2014.
 */
public class Result {
    @JsonProperty("case_id")
    private int caseId;
    @JsonProperty("status_id")
    private int statusId;
    @JsonProperty("comment")
    private String comment;

    @JsonIgnore
    public Result(int caseId, int statusId, String comment) {
        this.caseId = caseId;
        this.statusId = statusId;
        this.comment = comment;
    }

    public void setCaseId(int caseId) { this.caseId = caseId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    public void setComment(String comment) { this.comment = comment; }

    public int getCsaeId() { return this.caseId; }
    public int getStatusId() { return this.statusId; }
    public String getComment() { return this.comment; }
}
