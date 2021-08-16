package org.example.model;

import java.time.Duration;

/**
 * @author yeesheng on 15/08/2021
 * @project RateLimiterExample
 */
public class Test {
    private String id;
    private String name;
    private int permitPerSecond;
    private Duration runDuration;

    public Test(String id, String name, int permitPerSecond) {
        this.id = id;
        this.name = name;
        this.permitPerSecond = permitPerSecond;
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

    public Duration getRunDuration() {
        return runDuration;
    }

    public void setRunDuration(Duration runDuration) {
        this.runDuration = runDuration;
        //Duration d = Duration.parse("PT1H30M")
    }
}
