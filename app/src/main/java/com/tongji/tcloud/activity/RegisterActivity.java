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


public class RegisterActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";
    private EditText editUserName;
    private EditText editPassword_1;
    private EditText editPassword_2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_register);

        editUserName = (EditText) findViewById(R.id.register_editUserName);
        editPassword_1 = (EditText) findViewById(R.id.register_editPassword_1);
        editPassword_2 = (EditText) findViewById(R.id.register_editPassword_2);

        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        final ActionProcessButton btnSubmit = (ActionProcessButton) findViewById(R.id.register_btnSubmit);
        btnSubmit.setMode(ActionProcessButton.Mode.ENDLESS);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressGenerator.start(btnSubmit);
            }
        });
    }

    public void gotoMain() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onComplete() {
        //按钮特效显示完成，此处进行密码检查
        if(!editPassword_1.getText().toString().equals(editPassword_2.getText().toString())){
            Toast.makeText(this, "两次密码不一致!", Toast.LENGTH_LONG).show();
            return;
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RequestResult requestResult=(new RequestHelper()).Register(editUserName.getText().toString()
                ,editPassword_1.getText().toString());
        if(requestResult.success){
            Toast.makeText(this, requestResult.msg, Toast.LENGTH_LONG).show();
            gotoMain();
        }
        else{
            Toast.makeText(this, requestResult.error, Toast.LENGTH_LONG).show();
        }
    }

}
