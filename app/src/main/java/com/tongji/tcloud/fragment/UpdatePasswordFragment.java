package com.tongji.tcloud.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.tongji.tcloud.R;
import com.tongji.tcloud.activity.RegisterActivity;
import com.tongji.tcloud.activity.SignInActivity;
import com.tongji.tcloud.model.App;
import com.tongji.tcloud.model.RequestHelper;
import com.tongji.tcloud.model.RequestResult;
import com.tongji.tcloud.utils.ProgressGenerator;

import static android.content.Context.MODE_PRIVATE;

public class UpdatePasswordFragment extends Fragment implements ProgressGenerator.OnCompleteListener {

    public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";

    private EditText editPassword0;
    private EditText editPassword1;
    private EditText editPassword2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ac_update_password, container, false);

        editPassword0 = (EditText) view.findViewById(R.id.update_editPassword0);
        editPassword1 = (EditText) view.findViewById(R.id.update_editPassword1);
        editPassword2 = (EditText) view.findViewById(R.id.update_editPassword2);

        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        final ActionProcessButton btnUpdatePassword = (ActionProcessButton) view.findViewById(R.id.btnUpdatePassword);
        btnUpdatePassword.setMode(ActionProcessButton.Mode.ENDLESS);
        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressGenerator.start(btnUpdatePassword);
            }
        });
        return view;
    }

    @Override
    public void onComplete() {
        //按钮特效显示完成，此处进行密码检查及更新
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        String username=sharedPreferences.getString("username", "");
        RequestResult requestResult=(new RequestHelper()).ChangePassword(username
                ,editPassword0.getText().toString()
                ,editPassword1.getText().toString()
                ,editPassword2.getText().toString());
        if(requestResult.success){
            Toast.makeText(getActivity(), requestResult.msg, Toast.LENGTH_LONG).show();
            sharedPreferences.edit().remove("cookies").apply();
            sharedPreferences.edit().remove("username").apply();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(getActivity(), requestResult.error, Toast.LENGTH_LONG).show();
        }
    }

}
