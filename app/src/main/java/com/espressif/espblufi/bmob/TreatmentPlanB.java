package com.espressif.espblufi.bmob;

import cn.bmob.v3.BmobObject;

public class TreatmentPlanB extends BmobObject {
    private int musicId;
    private String planName;
    private int cesMode;
    private int cesWidth;
    private int treatment;

    // Getters and setters
    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public int getCesMode() {
        return cesMode;
    }

    public void setCesMode(int cesMode) {
        this.cesMode = cesMode;
    }

    public int getCesWidth() {
        return cesWidth;
    }

    public void setCesWidth(int cesWidth) {
        this.cesWidth = cesWidth;
    }

    public int getTreatment() {
        return treatment;
    }

    public void setTreatment(int treatment) {
        this.treatment = treatment;
    }
}
