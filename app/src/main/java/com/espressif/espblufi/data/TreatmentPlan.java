package com.espressif.espblufi.data;

public class TreatmentPlan {


    private int id;
    private int music_id;
    private String plan_name;
    private int ces_mode;
    private int ces_width;
    private int treatment_time;

    public TreatmentPlan(int music_id, String plan_name, int ces_mode, int ces_width, int treatment) {
        this.music_id = music_id;
        this.plan_name = plan_name;
        this.ces_mode = ces_mode;
        this.ces_width = ces_width;
        this.treatment_time = treatment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMusic_id() {
        return music_id;
    }

    public void setMusic_id(int music_id) {
        this.music_id = music_id;
    }

    public String getPlan_name() {
        return plan_name;
    }

    public void setPlan_name(String plan_name) {
        this.plan_name = plan_name;
    }

    public int getCes_mode() {
        return ces_mode;
    }

    public void setCes_mode(int ces_mode) {
        this.ces_mode = ces_mode;
    }

    public int getCes_width() {
        return ces_width;
    }

    public void setCes_width(int ces_width) {
        this.ces_width = ces_width;
    }

    public int getTreatment() {
        return treatment_time;
    }

    public void setTreatment(int treatment) {
        this.treatment_time = treatment;
    }

    @Override
    public String toString() {
        return plan_name;
    }
}
