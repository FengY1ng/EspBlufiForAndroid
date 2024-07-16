package com.espressif.espblufi.design;

import com.espressif.espblufi.data.Plan;
import com.espressif.espblufi.data.Segment;

import java.util.List;

public interface TimeRemainingOutput {

    void outputRemainingTime(List<Segment> myTask,int index);

}
