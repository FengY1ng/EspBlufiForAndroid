package com.espressif.espblufi.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.espressif.espblufi.R;
import com.espressif.espblufi.bmob.BmobLoginServiceApplication;
import com.espressif.espblufi.bmob.Check;
import com.espressif.espblufi.bmob.MyAppUser;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class FindPassword extends AppCompatActivity {
    EditText AccountText;
    EditText PasswordText;
    EditText SMS_Code;

    Button ModifyButton;
    Button GetCode;
    TextView FindPasswordTitle;
    TextView LoginButton;

    TextView ShowButton;
    boolean ShowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_password);
        init();


        /**
         * 获取验证码
         */
        GetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取账号(Account)
                final String Account = AccountText.getText().toString().trim();
                if (TextUtils.isEmpty(Account)){
                    Toast.makeText(FindPassword.this,"请填写账号",Toast.LENGTH_SHORT).show();
                }else if (Check.PhoneCheck(Account) != true){
                    Toast.makeText(FindPassword.this,"请填写正确的手机号码",Toast.LENGTH_SHORT).show();
                }else {
                    BmobQuery<MyAppUser> userQuery = new BmobQuery<>();
                    userQuery.addWhereEqualTo("mobilePhoneNumber", Account);
                    userQuery.findObjects(new FindListener<MyAppUser>() {
                        @Override
                        public void done(List<MyAppUser> list, BmobException e) {
                            if (e == null) {
                                if (list.isEmpty()) {
                                    Toast.makeText(FindPassword.this,"该账户不存在",Toast.LENGTH_SHORT).show();
                                } else {
                                    SendSMS(Account);
                                }
                            }
                        }
                    });
                }
            }
        });

        /**
         * 修改密码
         */
        ModifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入的账号(Account)、密码(Password)、验证码(Code)
                final String Account = AccountText.getText().toString().trim();
                final String Password = PasswordText.getText().toString().trim();
                String Code = SMS_Code.getText().toString().trim();
                if (TextUtils.isEmpty(Account)){
                    Toast.makeText(FindPassword.this,"请填写账号",Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Code)){
                    Toast.makeText(FindPassword.this,"请填写验证码",Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Password)){
                    Toast.makeText(FindPassword.this,"请填写密码",Toast.LENGTH_SHORT).show();
                }else if (Password.length()<6){
                    Toast.makeText(FindPassword.this,"密码不得少于6位数",Toast.LENGTH_SHORT).show();
                }else if (Password.length()>16){
                    Toast.makeText(FindPassword.this,"密码不得多于16位数",Toast.LENGTH_SHORT).show();
                }else if (Check.PasswordCheck(Password) != true){
                    Toast.makeText(FindPassword.this,"密码最少包含3个字母",Toast.LENGTH_SHORT).show();
                }else {
                    // 短信验证码方式重置密码
                    BmobUser.resetPasswordBySMSCode(Code, Password, new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Toast.makeText(FindPassword.this,"密码修改成功",Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                Log.d(BmobLoginServiceApplication.TAG, "修改失败: "+e.toString());
                                Toast.makeText(FindPassword.this, e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        //跳转登陆
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowPassword == false) {
                    //密码不可见-->>密码可见
                    PasswordText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    PasswordText.setSelection(PasswordText.getText().toString().length());
                    ShowPassword = true;
                }else {
                    //密码可见-->>密码不可见
                    PasswordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    PasswordText.setSelection(PasswordText.getText().toString().length());
                    ShowPassword = false;
                }
            }
        });
    }

    private void init(){
        //账号、密码、验证码
        AccountText = findViewById(R.id.AccountText);
        PasswordText = findViewById(R.id.PasswordText);
        SMS_Code = findViewById(R.id.SMS_Code);

        //回到登录按钮、获取验证码按钮、修改密码按钮
        FindPasswordTitle = findViewById(R.id.FindPasswordTitle);
        LoginButton = findViewById(R.id.LoginButton);
        GetCode = findViewById(R.id.GetCode);
        ModifyButton = findViewById(R.id.ModifyButton);

        //设置标题字体样式(方舒整体 常规)
        FindPasswordTitle.setTypeface(Typeface.createFromAsset(getAssets(),"font/FZSTK.TTF"));
        //眼睛按钮
        ShowButton = findViewById(R.id.ShowButton);
        ShowPassword = false;
    }

    /**
     * 发送验证码
     * @param account：输入的手机号码
     *  SMS 为Bmob短信服务自定义的短信模板名字
     */
    private void SendSMS(String account){
        //发送短信验证码
        BmobSMS.requestSMSCode(account, BmobLoginServiceApplication.SMS_TEMPLATE_NAME, new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {
                    Toast.makeText(FindPassword.this,"验证码已发送",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(FindPassword.this,"短信发送失败"+"\n"+"错误代码："+e.getErrorCode(),Toast.LENGTH_LONG).show();
                }
            }
        });
        //设置按钮60s等待点击
        CountDownTimer timer = new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                GetCode.setEnabled(false);
                GetCode.setText("重新获取("+millisUntilFinished/1000+"s)");
            }

            @Override
            public void onFinish() {
                GetCode.setEnabled(true);
                GetCode.setText("获取验证码");
            }
        }.start();
    }
}
