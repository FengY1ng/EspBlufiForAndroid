package com.espressif.espblufi.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.espblufi.CESMainActivity;
import com.espressif.espblufi.R;
import com.espressif.espblufi.bmob.MyAppUser;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity {
    //标题、账号、密码
    TextView LoginTitle;
    EditText AccountText;
    EditText PasswordText;

    //登陆按钮、注册按钮、密码找回按钮
    Button LoginButton;
    TextView RegisterButton;
    TextView FindPasswordButton;

    //眼睛按钮
    TextView ShowButton;
    //密码可见状态
    boolean ShowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        /**
         * 登陆监听
         */
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //账号(Account)、密码(Password)
                final String Account = AccountText.getText().toString().trim();
                final String Password = PasswordText.getText().toString().trim();
                if (TextUtils.isEmpty(Account)) {
                    Toast.makeText(LoginActivity.this, "请填写手机号码", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(Password)) {
                    Toast.makeText(LoginActivity.this, "请填写密码", Toast.LENGTH_SHORT).show();
                } else {
                    MyAppUser appUser = new MyAppUser();
                    appUser.setUsername(Account);
                    appUser.setPassword(Password);
                    appUser.login(new SaveListener<MyAppUser>() {
                        @Override
                        public void done(MyAppUser myAppUser, BmobException e) {
                            if (e == null) {
                                Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, CESMainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

//                    BmobQuery<User_Table> bmobQuery = new BmobQuery<>();
//                    bmobQuery.findObjects(new FindListener<User_Table>() {
//                        @Override
//                        public void done(List<User_Table> object, BmobException e) {
//                            if (e == null) {
//                                //判断信号量，若查找结束count和object长度相等，则没有查找到该账号
//                                int count=0;
//                                for (User_Table user_table : object) {
//                                    if (user_table.getAccount().equals(Account)) {
//                                        //已查找到该账号，检测密码是否正确
//                                        if (user_table.getPassword().equals(Password)) {
//                                            //密码正确，跳转（Home是登陆后跳转的页面）
//                                            Toast.makeText(Login.this, "登陆成功", Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent(Login.this,Home.class);
//                                            startActivity(intent);
//                                            break;
//                                        }else {
//                                            Toast.makeText(Login.this, "密码错误", Toast.LENGTH_SHORT).show();
//                                            break;
//                                        }
//                                    }
//                                    count++;
//                                }
//                                if (count >= object.size()){
//                                    Toast.makeText(Login.this,"该账号不存在",Toast.LENGTH_SHORT).show();
//                                }
//                            }else {
//                                Toast.makeText(Login.this,"该账号不存在",Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                }
            }
        });

        //跳转到密码找回界面
        FindPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, FindPassword.class);
                startActivity(intent);
            }
        });

        //跳转到注册界面
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent();
                intent.setClass(LoginActivity.this, Register.class);
                startActivity(intent);
            }
        });

        //密码可见和不可见
        ShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (   ShowPassword == false) {
                    PasswordText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    PasswordText.setSelection(PasswordText.getText().toString().length());
                    ShowPassword = true;
                }else {
                    PasswordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    PasswordText.setSelection(PasswordText.getText().toString().length());
                    ShowPassword = false;
                }
            }
        });
    }

    //View初始化
    public void init(){
        //Login标题(LoginTitle)、账号(AccountText)、密码(PasswordText)
        LoginTitle = findViewById(R.id.LoginTitle);
        AccountText = findViewById(R.id.AccountText);
        PasswordText = findViewById(R.id.PasswordText);

        //登录按钮(Login)、跳到注册按钮(Register)、跳到密码找回按钮(FindPassword)
        LoginButton = findViewById(R.id.LoginButton);
        RegisterButton = findViewById(R.id.RegisterButton);
        FindPasswordButton = findViewById(R.id.FindPasswordButton);
        ShowButton = findViewById(R.id.ShowButton);

        //密码初始状态为不可见（false不可见，true可见）
        ShowPassword = false;
        //设置Login标题字体样式(华文彩云)
        LoginTitle.setTypeface(Typeface.createFromAsset(getAssets(),"font/FZSTK.TTF"));
        //设置背景图片透明度(0~255，值越小越透明)
        LoginButton.getBackground().setAlpha(100);
    }
}
