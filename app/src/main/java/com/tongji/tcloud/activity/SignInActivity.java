package com.tongji.tcloud.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.tongji.tcloud.R;
import com.tongji.tcloud.model.RequestHelper;
import com.tongji.tcloud.model.RequestResult;
import com.tongji.tcloud.utils.ProgressGenerator;


public class SignInActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";
    private int btnClicked;
    private EditText editUserName;
    private EditText editPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_sign_in);

        editUserName = (EditText) findViewById(R.id.signIn_editUserName);
        editPassword = (EditText) findViewById(R.id.signIn_editPassword);

        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        final ActionProcessButton btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);
        btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressGenerator.start(btnSignIn);
                btnClicked=1;
            }
        });

        final ActionProcessButton btnRegister = (ActionProcessButton) findViewById(R.id.btnRegister);
        btnRegister.setMode(ActionProcessButton.Mode.ENDLESS);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressGenerator.start(btnRegister);
                btnClicked=2;
            }
        });
    }

    public void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void gotoRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onComplete() {
        //按钮特效显示完成，此处进行后续逻辑
        if(btnClicked==1){
            //登录成功
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            RequestResult requestResult=(new RequestHelper()).Login(editUserName.getText().toString(),editPassword.getText().toString());
            if(requestResult.success){
                Toast.makeText(this, requestResult.msg, Toast.LENGTH_LONG).show();
                gotoMain();
            }
            else{
                Toast.makeText(this, requestResult.error, Toast.LENGTH_LONG).show();
            }
        }
        else{
            gotoRegister();
        }
    }

}
