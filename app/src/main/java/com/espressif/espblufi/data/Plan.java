package com.espressif.espblufi.data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobObject;

public class Plan extends BmobObject {
    private String planName;
    private Integer planId;
    private List<Segment> planData;
    private Integer planMusic;

    public Integer getPlanMusic() {
        return planMusic;
    }

    public void setPlanMusic(Integer planMusic) {
        this.planMusic = planMusic;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public List<Segment> getPlanData() {
        return planData;
    }

    public void setPlanData(List<Segment> planData) {
        this.planData = planData;
    }
}