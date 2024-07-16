package com.espressif.espblufi;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.espressif.espblufi.data.DataType;
import com.espressif.espblufi.data.Plan;
import com.espressif.espblufi.data.Segment;
import com.espressif.espblufi.design.TimeRemainingOutput;
import com.espressif.espblufi.service.MusicService;
import com.espressif.espblufi.ui.MainActivity;
import com.espressif.espblufi.utils.SendCommandThread;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import okhttp3.internal.concurrent.Task;

// Fragment是一种可以嵌入在Activity当中的UI片段
public class CESMainFragment extends Fragment implements TimeRemainingOutput {


    private ImageView musicImageView;
    private List<Plan> mTreatmentPlans;
    private List<String> mTreatmentPlanNames = new ArrayList<>();

    private TextView musicNameTextView, textView_info;
    private ImageView img_treatment_start, img_treatment_pause, img_treatment_stop;
    private SeekBar musicSeekBar;
    private Spinner musicSpinner;
    private int[] musicid;
    private int time = 8 * 60;
    private int musicSelcetid = R.raw.heart_one;

    private String TAG = "CESMainFragment";
    private LineChart lineChart;
    private View rootView;

    private CESMainActivity myCESMainActivity;
    private Plan myTreatmentPlan;
    private ArrayAdapter adapter;
    private Button bu_sendPlan;
    private boolean isSending = false;
    private SendCommandThread sendCommandThread;
    private String thread_Name = "控制指令发送线程";
    private boolean  flog = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_c_e_s_main, container, false);

        mTreatmentPlans = new ArrayList<>();
        // mTreatmentPlanNames = initTreatmentPlans(mTreatmentPlans);
        initTreatmentPlans();
        Log.d("Feng", "onCreateView: ");
        // Inflate the layout for this fragment
        return rootView;
    }

    //在视图创建完成后初始化相关信息
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

/*        myCESMainActivity.setMusic(2131755009, 1 * 60);
        myCESMainActivity.startMusic();*/

        lineChart = rootView.findViewById(R.id.line_chart);
        bu_sendPlan = rootView.findViewById(R.id.bu_sendPlan);
        textView_info = rootView.findViewById(R.id.textView_info);
        myCESMainActivity = ((CESMainActivity) getActivity());

        musicImageView = rootView.findViewById(R.id.musicImageView);
        musicNameTextView = rootView.findViewById(R.id.musicNameTextView);
        img_treatment_start = rootView.findViewById(R.id.img_tratment_play);
        img_treatment_pause = rootView.findViewById(R.id.img_tratment_pause);
        img_treatment_stop = rootView.findViewById(R.id.img_tratment_stop);
        musicSeekBar = rootView.findViewById(R.id.musicSeekBar);
        musicSpinner = rootView.findViewById(R.id.musicSpinner);
        setUpLineChart();
        sendCommandThread = new SendCommandThread(thread_Name);
        sendCommandThread.start();
        sendCommandThread.setOutputHandler(new TimeRemainingOutput() {
            @Override
            public void outputRemainingTime(List<Segment> mTask, int index) {
                if(flog )
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                         flog = false;

                            try {
                                sendCMDtoDevice(DataType.CMD_END);
                                Thread.sleep(1000);
                                sendPlantoDevice(changeTaskToPlan(mTask, index));
                                Thread.sleep(1000);
                                sendCMDtoDevice(DataType.CMD_START);
                                flog = true;
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }).start();

                }

            }
        });

        musicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        //myCESMainActivity.setMusic(musicSelcetid,time);
        // 使用ArrayAdapter将DataA对象列表中的name属性显示在Spinner中
        adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, mTreatmentPlanNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        musicSpinner.setAdapter(adapter);
        Log.d("Feng", mTreatmentPlans.toString());
        // textView_info.setText("治疗模式:"+myTreatmentPlan.getPlanData()+",治疗强度:"+myTreatmentPlan.getCes_width()+",治疗时长:"+myTreatmentPlan.getTreatment());
        // 设置Spinner选择监听器，返回选中的DataA对象
        musicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myTreatmentPlan = mTreatmentPlans.get(position);
                musicNameTextView.setText(myTreatmentPlan.getPlanName());
                textView_info.setText("治疗模式:" + myTreatmentPlan.getPlanData().get(0).getMode() + ",治疗强度:" + myTreatmentPlan.getPlanData().get(0).getPower() + ",治疗时长:" + myTreatmentPlan.getPlanData().get(0).getTime());
                // 在这里处理选中的DataA对象
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 当没有选择项时的处理
            }
        });

        bu_sendPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myTreatmentPlan != null) {
                    //Log.d("Feng", String.valueOf(myTreatmentPlan));
                    Log.d("Feng", String.valueOf(myTreatmentPlan.getPlanData().get(0).getTime()));
                    Log.d("Feng", String.valueOf(myTreatmentPlan.getPlanData().get(0).getMode()));
                    Log.d("Feng", String.valueOf(myTreatmentPlan.getPlanData().get(0).getPower()));
                    sendCommandThread.setTask(myTreatmentPlan.getPlanData());
                    sendCommandThread.pauseThread();
                    //isSending = true;
                }
            }
        });


        img_treatment_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start music playback
                if (myCESMainActivity.getMusicState() == MusicService.MUSIC_READY) {
                    sendCMDtoDevice(DataType.CMD_START);
                    myCESMainActivity.setMusic(myTreatmentPlan.getPlanMusic(), myTreatmentPlan.getPlanData().get(0).getTime() * 60);
                    myCESMainActivity.startMusic();
                    sendCommandThread.resumeThread();
                    Log.d(TAG, "musicPlayerService.startTreatmentMusicStart();");
                } else if (myCESMainActivity.getMusicState() == MusicService.MUSIC_PAUSE) {
                    sendCMDtoDevice(DataType.CMD_START);
                    // 绑定Service
                    myCESMainActivity.resumeMusic();
                    sendCommandThread.resumeThread();
                    Log.d(TAG, "musicPlayerService.resumeMusic();");
                }

            }
        });

        img_treatment_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pause music playback
                if (myCESMainActivity.getMusicState() == MusicService.MUSIC_PLAYING) {
                    myCESMainActivity.pauseMusic();
                    sendCMDtoDevice(DataType.CMD_PAUSE);
                    sendCommandThread.pauseThread();
                }
            }
        });

        img_treatment_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop music playback
                if (myCESMainActivity.getMusicState() != MusicService.MUSIC_READY) {
                    myCESMainActivity.stopMusic();
                    sendCMDtoDevice(DataType.CMD_END);
                    sendCommandThread.setTask(null);
                }
            }
        });

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update music playback progress
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle start tracking touch event
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle stop tracking touch event
            }
        });
    }

    @Override
    public void outputRemainingTime(List<Segment> myTask, int index) {
        sendCMDtoDevice(DataType.CMD_PAUSE);
        //!!!!需要先后开启加点延迟  比如放在一个线程中，先后间隔1s开启
        sendPlantoDevice(myTask.get(index));
        sendCMDtoDevice(DataType.CMD_START);
    }

    private Segment changeTaskToPlan(List<Segment> mTask, int index) {
        Segment res_Plan = null ;
        if (mTask != null && mTask.size() > index) {
            res_Plan = mTask.get(index);
        }

        return res_Plan;
    }

    private void initTreatmentPlans() {
        Log.d("BmobQuery", "Starting Bmob query");
        BmobQuery<Plan> query = new BmobQuery<>();
        query.findObjects(new FindListener<Plan>() {
            @Override
            public void done(List<Plan> list, BmobException e) {
                Log.d("BmobQuery", "Query completed");
                if (e == null) {
                    mTreatmentPlans.clear();
                    mTreatmentPlanNames.clear();
                    for (Plan plan : list) {
                        mTreatmentPlans.add(plan);
                        mTreatmentPlanNames.add(plan.getPlanName());
                    }
                    adapter.notifyDataSetChanged();  // 确保数据变更后更新适配器
                    if (!mTreatmentPlans.isEmpty()) {
                        updateTreatmentInfo(0);  // 更新治疗信息
                    } else {
                        Log.d("Feng", "数据为空");
                    }
                } else {
                    Toast.makeText(getActivity(), "获取方案数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (mTreatmentPlans.isEmpty()) {
                    mTreatmentPlanNames.add("No Plans Available");  // 提供默认无数据项
                    adapter.notifyDataSetChanged();  // 确保适配器更新
                }
                myTreatmentPlan = mTreatmentPlans.get(0);
                musicNameTextView.setText(myTreatmentPlan.getPlanName());
            }

        });
        Log.d("BmobQuery", "After executing the query");


/*        List<String> res_planName = new ArrayList<>();
        fetchTreatmentPlans();
        return res_planName;*/
        /*List<String> res_planName = new ArrayList<>();
        if(mTreatmentPlans!=null)
        {
            mTreatmentPlans.add(new TreatmentPlan(R.raw.heart_one,"治疗方案1",1,1,114514));
            res_planName.add("治疗方案1");
            mTreatmentPlans.add(new TreatmentPlan(R.raw.bzs01,"治疗方案2",1,12,1919810));
            res_planName.add("治疗方案2");
            mTreatmentPlans.add(new TreatmentPlan(R.raw.heart_one,"治疗方案3",0,14,5));
            res_planName.add("治疗方案3");
            mTreatmentPlans.add(new TreatmentPlan(R.raw.bzs01,"治疗方案4",0,8,8));
            res_planName.add("治疗方案4");
        }
        return res_planName;*/
    }

    private void updateTreatmentInfo(int position) {
        Plan selectedPlan = mTreatmentPlans.get(position);
        musicNameTextView.setText(selectedPlan.getPlanName());

        StringBuilder treatmentDetails = new StringBuilder();
        for (Segment segment : selectedPlan.getPlanData()) {
            if (treatmentDetails.length() > 0) {
                treatmentDetails.append(", ");
            }
            treatmentDetails.append(String.format("时间：%d, 功率：%d, 模式：%d", segment.getTime(), segment.getPower(), segment.getMode()));
        }
        textView_info.setText("详细治疗方案: " + treatmentDetails.toString());
    }

/*    private void setupDurationSeekBar() {
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    durationTextView.setText("Duration: " + progress + " min");
                    resetChartData(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: 在开始滑动时执行的操作
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: 在停止滑动时执行的操作
            }
        });
    }*/

/*    private void resetChartData(int minutes) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i <= minutes; i++) {
            // 设置每个时间点的强度为7
            entries.add(new Entry(i, 7)); // 使用常量强度7
        }

        LineDataSet dataSet = new LineDataSet(entries, "Intensity");
        dataSet.setColor(Color.BLUE); // 设置线的颜色
        dataSet.setValueTextColor(Color.BLACK); // 设置数据点文字颜色

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.notifyDataSetChanged(); // 通知数据已改变
        chart.invalidate(); // 刷新图表
    }*/


    private void setUpLineChart() {
        // 设置LineChart样式
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // x轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // 设置间隔
        xAxis.setLabelCount(10); // 设置显示的标签数量

        // y轴
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f); // y轴最小值
        yAxis.setAxisMaximum(100f); // y轴最大值

        // 坐标轴颜色
        xAxis.setTextColor(Color.GREEN);
        yAxis.setTextColor(Color.GREEN);

        // 线的样式
        LineDataSet dataSet = new LineDataSet(null, "Real-time Data");
        dataSet.setColor(Color.BLUE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 设置曲线为平滑曲线

        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.invalidate(); // 刷新图表
    }

    //
    private void sendCMDtoDevice(int cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", DataType.TYPE_CMD);
                        jsonObject.put("cmd", cmd);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] message = jsonObject.toJSONString().getBytes();
                    ((CESMainActivity) getActivity()).sendMessagetoDevice(message);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }

    private void sendPlantoDevice(Segment myTreatmentSegment) {
        if (myTreatmentSegment != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", DataType.TYPE_PRM);
                            jsonObject.put("mode", myTreatmentSegment.getMode());
                            jsonObject.put("intensity", myTreatmentSegment.getPower());
                            jsonObject.put("duration", myTreatmentSegment.getTime());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        byte[] message = jsonObject.toJSONString().getBytes();
                        ((CESMainActivity) getActivity()).sendMessagetoDevice(message);
                        Log.d(TAG, jsonObject.toJSONString());

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        }


    }




    //更新图表
    protected void updateChart(int number) {
        LineData data = lineChart.getLineData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), number), 0);
            data.notifyDataChanged();

            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(100); // 设置x轴显示的最大数量
            lineChart.moveViewToX(data.getEntryCount() - 101); // 将新增的值放在最左侧

            lineChart.invalidate(); // 刷新图表
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Real-time Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 设置曲线为平滑曲线
        set.setDrawValues(false); // 不显示每个点的值
        return set;
    }

    protected void updateProgress(int progress) {
        // 更新SeekBar的进度
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                musicSeekBar.setProgress(progress);
            }
        });
    }

    /**
     * 停止治疗
     *
     * @param view
     */
    public void onClickTStopImage(View view) {
    }

    /**
     * 暂停治疗
     *
     * @param view
     */
    public void onClickTPauseImage(View view) {
    }

    /**
     * 开始治疗
     *
     * @param view
     */
    public void onClickTPlayImage(View view) {
    }



}