package testrail.testrail.TestRailObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Drew on 3/25/2014.
 */
public class Results {

    private List<Result> results;

    @JsonIgnore
    public Results() {
        this.results = new ArrayList<Result>();
    }

    public void setResults(ArrayList<Result> results) { this.results = results; }
    public void addResult(Result result) { this.results.add(result); }
    public List<Result> getResults() {return this.results; }
}
