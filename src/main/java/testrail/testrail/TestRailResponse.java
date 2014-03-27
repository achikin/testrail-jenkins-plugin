package testrail.testrail;

/**
 * Created by Drew on 3/20/14.
 */
public class TestRailResponse {
    private Integer status;
    public Integer getStatus() { return status; }
    
    private String body;
    public String getBody() { return body; }

    public TestRailResponse(Integer status, String body) {
        this.status = status;
        this.body = body;
    }
}
