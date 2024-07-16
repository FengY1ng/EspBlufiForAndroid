package com.espressif.espblufi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.espressif.espblufi.data.Plan;
import com.espressif.espblufi.data.Segment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class CreatePlanFragment extends Fragment {

    private EditText planNameEditText;
    private LineChart chart;
    private Button savePlanButton;
    private TextView durationTextView;
    private SeekBar durationSeekBar;
    private TextView bubbleText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_plan, container, false);
        planNameEditText = view.findViewById(R.id.edit_plan_name);
        chart = view.findViewById(R.id.chart);
        savePlanButton = view.findViewById(R.id.btn_save_plan);
        // durationTextView = view.findViewById(R.id.tv_duration);
        durationSeekBar = view.findViewById(R.id.customSeekBar);

        bubbleText = new TextView(getContext());
        bubbleText.setBackgroundColor(Color.WHITE); // 设置气泡背景
        bubbleText.setTextColor(Color.BLACK); // 设置文字颜色
        setupSeekBar();

        setupChart();
        setupDurationSeekBar();
        setupButton();
        return view;
    }

    private void setupSeekBar() {
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateBubble(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                bubbleText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                bubbleText.setVisibility(View.GONE);
            }
        });
    }

    private void updateBubble(int progress) {
        bubbleText.setText(String.format(Locale.US, "%d min", progress));
        if (bubbleText.getParent() == null) {
            ((ViewGroup) getView()).addView(bubbleText);
        }
        bubbleText.setX(durationSeekBar.getX() + durationSeekBar.getThumb().getBounds().exactCenterX());
        bubbleText.setY(durationSeekBar.getY() - 60); // 调整气泡显示的位置
    }

    private void setupChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i <= 60; i++) {
            entries.add(new Entry(i, 7));  // 初始强度设置为7
        }

        LineDataSet dataSet = new LineDataSet(entries, "Intensity");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();  // Refresh chart

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);  // one unit in the x-axis is one minute

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(20f);  // 设置强度范围0-20

        chart.getAxisRight().setEnabled(false);

        resetChartData(30);  // 默认为30分钟

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showAdjustmentDialog(e);
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    private void resetChartData(int minutes) {
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
    }

    private void showAdjustmentDialog(final Entry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adjust Intensity");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf((int) entry.getY()));
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float newValue = Float.parseFloat(input.getText().toString());
                entry.setY(newValue);
                for (int i = (int) entry.getX() + 1; i < chart.getLineData().getDataSetByIndex(0).getEntryCount(); i++) {
                    Entry nextEntry = chart.getLineData().getDataSetByIndex(0).getEntryForIndex(i);
                    nextEntry.setY(newValue);
                }
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setupDurationSeekBar() {
        durationSeekBar.setMax(60);  // 最长60分钟
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // durationTextView.setText("Duration: " + progress + " minutes");
                resetChartData(progress);
                if (progress < chart.getLineData().getEntryCount()) {
                    while (chart.getLineData().getEntryCount() > progress) {
                        LineDataSet dataSet = (LineDataSet) chart.getLineData().getDataSetByIndex(0);
                        dataSet.removeLast();
                    }
                } else {
                    LineDataSet dataSet = (LineDataSet) chart.getLineData().getDataSetByIndex(0);
                    for (int i = dataSet.getEntryCount(); i <= progress; i++) {
                        dataSet.addEntry(new Entry(i, dataSet.getEntryForIndex(dataSet.getEntryCount() - 1).getY()));
                    }
                }
                chart.notifyDataSetChanged();
                chart.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setupButton() {
        savePlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String planName = planNameEditText.getText().toString();
                if (planName.isEmpty()) {
                    Toast.makeText(getContext(), "请输入方案名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                savePlan(planName);
            }
        });
    }

    private void savePlan(String planName) {
        // 提取图表数据
        LineDataSet dataSet = (LineDataSet) chart.getLineData().getDataSetByIndex(0);
        List<Entry> entries = dataSet.getValues();  // 正确方法调用
        List<JSONObject> planData = new ArrayList<>();

        try {
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                JSONObject segment = new JSONObject();
                segment.put("time", (int) entry.getX());  // 时间以分钟为单位
                segment.put("power", (int) entry.getY()); // 功率
                segment.put("mode", 1); // 暂时假设模式为1，实际应该由用户选择

                // 判断是否需要新的分区
                if (i == 0 || entries.get(i - 1).getY() != entry.getY()) {
                    planData.add(segment);
                } else {
                    // 更新最后一个分区的时间
                    JSONObject lastSegment = planData.get(planData.size() - 1);
                    lastSegment.put("time", (int) entry.getX());
                }
            }

            // 将数据发送到 Bmob
            JSONObject plan = new JSONObject();
            plan.put("planName", planName);
            plan.put("planData", new JSONArray(planData));

            // 假设有一个方法来处理 Bmob 的数据发送
            submitPlanToBmob(plan);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error creating plan data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitPlanToBmob(JSONObject plan) {
        try {
            String planName = plan.getString("planName");
            JSONArray segmentsJson = plan.getJSONArray("planData");

            List<Segment> segments = new ArrayList<>();
            int lastEndTime = 0; // 上一个分区的结束时间初始化为0

            for (int i = 0; i < segmentsJson.length(); i++) {
                JSONObject segmentJson = segmentsJson.getJSONObject(i);
                Segment segment = new Segment();

                int endTime = segmentJson.getInt("time"); // 当前分区的结束时间
                if (i == 0) {
                    segment.setTime(endTime); // 如果是第一个分区，直接使用endTime
                } else {
                    segment.setTime(endTime - lastEndTime); // 否则，使用当前结束时间减去上一个分区的结束时间
                }
                lastEndTime = endTime; // 更新上一个分区的结束时间为当前分区的结束时间

                segment.setPower(segmentJson.getInt("power"));
                segment.setMode(segmentJson.getInt("mode"));
                segments.add(segment);
            }

            Plan planObject = new Plan();
            planObject.setPlanName(planName);
            planObject.setPlanData(segments);
            planObject.save(new SaveListener<String>() {
                @Override
                public void done(String objectId, BmobException e) {
                    if (e == null) {
                        Log.d("Save Success", "Plan saved with ID: " + objectId);
                        Toast.makeText(getContext(), "Plan saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Save Error", "Failed to save plan: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to save plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing plan data.", Toast.LENGTH_SHORT).show();
        }
    }

}
