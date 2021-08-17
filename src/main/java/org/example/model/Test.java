package org.example.model;

/**
 * @author yeesheng on 15/08/2021
 * @project RateLimiterExample
 */
public class Test {
    private String id;
    private String name;
    private int permitPerSecond;
    private boolean enableSoakTest;

    public Test(String id, String name, int permitPerSecond, boolean enableSoakTest) {
        this.id = id;
        this.name = name;
        this.permitPerSecond = permitPerSecond;
        this.enableSoakTest = enableSoakTest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPermitPerSecond() {
        return permitPerSecond;
    }

    public void setPermitPerSecond(int permitPerSecond) {
        this.permitPerSecond = permitPerSecond;
    }

    public boolean isEnableSoakTest() {
        return enableSoakTest;
    }

    public void setEnableSoakTest(boolean enableSoakTest) {
        this.enableSoakTest = enableSoakTest;
    }

}
