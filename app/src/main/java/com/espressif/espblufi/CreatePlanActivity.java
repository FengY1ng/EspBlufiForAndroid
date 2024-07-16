package com.espressif.espblufi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.espressif.espblufi.data.Plan;
import com.espressif.espblufi.data.Segment;

import java.util.ArrayList;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class CreatePlanActivity extends AppCompatActivity {
    private EditText editPlanName, editTime, editPower, editMode;
    private Button btnCreatePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_plan);

        if(findViewById(R.id.fragment_container) != null){
            if(savedInstanceState != null){
                return;
            }

            CreatePlanFragment firstFragment = new CreatePlanFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }

        /*editPlanName = findViewById(R.id.edit_plan_name);
        editTime = findViewById(R.id.edit_time);
        editPower = findViewById(R.id.edit_power);
        editMode = findViewById(R.id.edit_mode);
        btnCreatePlan = findViewById(R.id.btn_create_plan);

        btnCreatePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlan();
            }
        });*/
    }

/*    private void createPlan() {
        String planName = editPlanName.getText().toString();
        int time = Integer.parseInt(editTime.getText().toString());
        int power = Integer.parseInt(editPower.getText().toString());
        int mode = Integer.parseInt(editMode.getText().toString());

        Plan plan = new Plan();
        plan.setPlanName(planName);
        ArrayList<Segment> segments = new ArrayList<>();
        segments.add(new Segment(time, power, mode));
        plan.setPlanData(segments);

        plan.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Toast.makeText(CreatePlanActivity.this, "治疗方案保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreatePlanActivity.this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/
}
