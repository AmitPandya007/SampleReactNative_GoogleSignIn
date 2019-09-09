package com.samplereact.model;

import java.io.Serializable;

public class Model_Steps implements Serializable {

    private int step;
    private String startTime;
    private String endTime;

    public Model_Steps(String startTime, String endTime, int step) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
