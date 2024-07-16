package com.espressif.espblufi.data;

public class Segment {
    private int time;
    private int power;
    private int mode;

    public Segment(int time, int power, int mode) {
        this.time = time;
        this.power = power;
        this.mode = mode;
    }

    public Segment() {

    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
