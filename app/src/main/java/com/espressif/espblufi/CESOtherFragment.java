package com.espressif.espblufi;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CESOtherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CESOtherFragment extends Fragment {
    private LineChart heartRateChart;
    private View rootView;
    private Timer timer;
    private Handler handler;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CESOtherFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CESOtherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CESOtherFragment newInstance(String param1, String param2) {
        CESOtherFragment fragment = new CESOtherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_c_e_s_other, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        heartRateChart = rootView.findViewById(R.id.heart_rate_chart);
        handler = new Handler(Looper.getMainLooper());
        setUpHeartRateChart();
        startUpdatingHeartRate();
    }

    private void setUpHeartRateChart() {
        // 设置LineChart样式
        heartRateChart.getDescription().setEnabled(false);
        heartRateChart.setTouchEnabled(true);
        heartRateChart.setDragEnabled(true);
        heartRateChart.setScaleEnabled(true);
        heartRateChart.setPinchZoom(true);

        // x轴
        XAxis xAxis = heartRateChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // 设置间隔
        xAxis.setLabelCount(10); // 设置显示的标签数量

        // y轴
        YAxis yAxis = heartRateChart.getAxisLeft();
        yAxis.setAxisMinimum(60f); // y轴最小值
        yAxis.setAxisMaximum(100f); // y轴最大值

        // 坐标轴颜色
        xAxis.setTextColor(Color.GREEN);
        yAxis.setTextColor(Color.GREEN);

        // 线的样式
        LineDataSet dataSet = new LineDataSet(null, "Heart Rate Data");
        dataSet.setColor(Color.RED);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 设置曲线为平滑曲线

        LineData data = new LineData(dataSet);
        heartRateChart.setData(data);
        heartRateChart.invalidate(); // 刷新图表
    }

    //实时更新函数
    private void startUpdatingHeartRate() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> addHeartRateData());
            }
        }, 0, 1000); // 每秒更新一次数据
    }

    //添加心率数据
    private void addHeartRateData() {
        LineData data = heartRateChart.getLineData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createHeartRateSet();
                data.addDataSet(set);
            }

            // 从硬件设备获取数据
            data.addEntry(new Entry(set.getEntryCount(), 70 + (float)(Math.random() * 20)), 0);
            data.notifyDataChanged();

            heartRateChart.notifyDataSetChanged();
            heartRateChart.setVisibleXRangeMaximum(100); // 设置x轴显示的最大数量
            heartRateChart.moveViewToX(data.getEntryCount() - 101); // 将新增的值放在最左侧

            heartRateChart.invalidate(); // 刷新图表
        }
    }

    //对曲线的参数的具体设置
    private LineDataSet createHeartRateSet() {
        LineDataSet set = new LineDataSet(null, "Heart Rate Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 设置曲线为平滑曲线
        set.setDrawValues(false); // 不显示每个点的值
        return set;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.cancel(); // 停止定时任务
    }
}
