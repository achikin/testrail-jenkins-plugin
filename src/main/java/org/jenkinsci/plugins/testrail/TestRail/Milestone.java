package org.jenkinsci.plugins.testrail.TestRail;

/**
 * Created by achikin on 7/30/14.
 */
public class Milestone {
    private String id;
    private String name;
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name.trim(); }
    public String getId() { return this.id; }
    public String getName() { return  this.name; }
}
