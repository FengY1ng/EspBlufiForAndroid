package com.espressif.espblufi.ui;


import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

public class Register extends AppCompatActivity {
    TextView RegisterTitle; //注册标题
    EditText AccountText;   //账号
    EditText PasswordText;  //密码
    EditText SMS_Code;      //验证码

    TextView LoginButton;   //回到登录按钮
    Button RegisterButton;  //注册按钮
    Button GetCode;         //获取验证码按钮

    TextView ShowButton;    //小眼睛按钮
    boolean ShowPassword;   //密码可见状态（初始不可见）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        init();

        //获取验证码
        GetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取客户端输入的账号
                final String Account = AccountText.getText().toString().trim();
                //isEmpty()方法判断是否为空
                if (TextUtils.isEmpty(Account)){
                    Toast.makeText(Register.this,"请填写手机号码",Toast.LENGTH_SHORT).show();
                }else if (Check.PhoneCheck(Account.trim()) != true){
                    Toast.makeText(Register.this,"请填写正确的手机号码",Toast.LENGTH_SHORT).show();
                }else {
                    BmobQuery<MyAppUser> userQuery = new BmobQuery<>();
                    userQuery.addWhereEqualTo("mobilePhoneNumber", Account);
                    userQuery.findObjects(new FindListener<MyAppUser>() {
                        @Override
                        public void done(List<MyAppUser> list, BmobException e) {
                            if (e == null) {
                                if (list.isEmpty()) {
                                    SendSMS(Account);
                                } else {
                                    Toast.makeText(Register.this,"该账号已注册",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });

        /**
         * 注册
         */
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //账号(Account)、密码(Password)
                final String Account = AccountText.getText().toString().trim();
                final String Password = PasswordText.getText().toString().trim();
                if (TextUtils.isEmpty(Password)){
                    Toast.makeText(Register.this,"请填写密码",Toast.LENGTH_SHORT).show();
                }else if (Password.length()<6){
                    Toast.makeText(Register.this,"密码不得少于6位数",Toast.LENGTH_SHORT).show();
                }else if (Password.length()>16){
                    Toast.makeText(Register.this,"密码不得多于16位数",Toast.LENGTH_SHORT).show();
                }else if (Check.PasswordCheck(Password) != true){
                    Toast.makeText(Register.this,"密码最少包含3个字母",Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(SMS_Code.getText().toString().trim())){
                    Toast.makeText(Register.this,"请填写验证码",Toast.LENGTH_SHORT).show();
                } else {
                    String mobile = Account;
                    String smsCode = SMS_Code.getText().toString().trim();
                    MyAppUser appUser = new MyAppUser();
                    //设置手机号码（必填）
                    appUser.setMobilePhoneNumber(mobile);
                    //设置用户名，如果没有传用户名，则默认为手机号码
                    appUser.setUsername(mobile);
                    //设置账户密码
                    appUser.setPassword(Password);
                    // 设置账户的nickname
                    appUser.setNickname(mobile);
                    appUser.signOrLogin(smsCode, new SaveListener<MyAppUser>() {
                        @Override
                        public void done(MyAppUser user, BmobException e) {
                            if (e == null) {
                                //注册成功，回到登录页面
                                Toast.makeText(Register.this,"注册成功",Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                Toast.makeText(Register.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

//                    //短信验证码效验
//                    BmobSMS.verifySmsCode(Account, SMS_Code.getText().toString().trim(), new UpdateListener() {
//                        @Override
//                        public void done(BmobException e) {
//                            if (e == null) {
//                                //将用户信息存储到Bmob云端数据
//                                final User_Table user = new User_Table();
//                                user.setAccount(Account);
//                                user.setPassword(Password);
//                                user.save(new SaveListener<String>() {
//                                    @Override
//                                    public void done(String s, BmobException e) {
//                                        if (e == null) {
//                                            //注册成功，回到登录页面
//                                            Toast.makeText(Register.this,"注册成功",Toast.LENGTH_SHORT).show();
//                                            finish();
//                                        }else {
//                                            Toast.makeText(Register.this,"注册失败",Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
//                            }else {
//                                SMS_Code.setText("");
//                                Toast.makeText(Register.this,"验证码错误"+e.getErrorCode(),Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                }
            }
        });

        //返回登陆界面
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

    public void init(){
        //注册标题(Title)、账号(Account)、密码(Password)、验证码(SMS_Code)
        Bmob.initialize(this,"8847293e15bf3afcc1dc731eca5a0b6e");
        RegisterTitle = findViewById(R.id.RegisterTitle);
        AccountText = findViewById(R.id.AccountText);
        PasswordText = findViewById(R.id.PasswordText);
        SMS_Code = findViewById(R.id.SMS_Code);

        //回到登录按钮(Login)、注册按钮(Register)、验证码获取按钮(GetCode)
        LoginButton = findViewById(R.id.LoginButton);
        RegisterButton = findViewById(R.id.RegisterButton);
        GetCode = findViewById(R.id.GetCode);

        //将密码文本初始设置为不可见状态
        ShowButton = findViewById(R.id.ShowButton);
        ShowPassword = false;

        //设置标题字体样式(方舒整体 常规)
        RegisterTitle.setTypeface(Typeface.createFromAsset(getAssets(),"font/FZSTK.TTF"));
        //设置按钮文本字体样式(方舒整体 常规)
        RegisterButton.setTypeface(Typeface.createFromAsset(getAssets(),"font/FZSTK.TTF"));
        //设置背景图片透明度(0~255，值越小越透明)
        RegisterButton.getBackground().setAlpha(100);
    }

    /**
     * 发送验证码
     * @param account：输入的手机号码
     *  SMS 为Bmob短信服务自定义的短信模板名字
     */
    private void SendSMS(String account){
        BmobSMS.requestSMSCode(account, BmobLoginServiceApplication.SMS_TEMPLATE_NAME, new QueryListener<Integer>() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {
                    Toast.makeText(Register.this,"验证码已发送",Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(Register.this,"发送验证码失败：" + e.getErrorCode() + "-" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        /**
         * 设置按钮60s等待
         * onTick()方法——>>计时进行时的操作
         *      ：显示倒计时，同时设置按钮不可点击
         * onFinish()方法——>>计时完成时的操作
         *      ：刷新原文本，同时设置按钮可以点击
         */
        CountDownTimer timer =new CountDownTimer(60000,1000) {
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
