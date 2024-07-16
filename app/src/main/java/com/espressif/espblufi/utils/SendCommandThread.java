package com.espressif.espblufi.utils;

import android.util.Log;

import com.espressif.espblufi.data.Segment;
import com.espressif.espblufi.design.TimeRemainingOutput;

import java.util.ArrayList;
import java.util.List;

public class SendCommandThread extends Thread{
    private List<Segment> task;
    private boolean running;
    private boolean paused;
    private TimeRemainingOutput outputHandler;
    public SendCommandThread(String name) {
        super(name);
        task = new ArrayList<>();
        running = true;
        paused = false;
    }
    public void setTask(List<Segment> newTask) {
        task = newTask;
        Log.d("1SendCommandThread","setTask");
        synchronized (this) {
            running = true;
            paused = false;
            notify();
        }
    }

    public void setOutputHandler(TimeRemainingOutput outputHandler) {
        this.outputHandler = outputHandler;
    }

    public void pauseThread() {
        paused = true;
    }

    public void resumeThread() {
        paused = false;
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (!running || paused) {
                    Log.d("1SendCommandThread","paused");
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(task!=null)
            {
                for (int i = 0; i < task.size(); i++) {
                    int remainingTime = task.get(i).getTime();
                    if (outputHandler != null) {
                        outputHandler.outputRemainingTime(task,i);
                        Log.d("1SendCommandThread","send1");
                    }

                    try {
                        Thread.sleep(remainingTime * 60 * 1000); // Convert minutes to milliseconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            synchronized (this) {
                running = false;
            }
        }
    }
}
