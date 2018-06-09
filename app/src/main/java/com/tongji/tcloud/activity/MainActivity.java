package com.tongji.tcloud.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.tongji.tcloud.R;
import com.tongji.tcloud.fragment.DemoFragment;
import com.tongji.tcloud.fragment.FolderFragment;
import com.tongji.tcloud.fragment.PermissionFragment;
import com.tongji.tcloud.fragment.UpdatePasswordFragment;
import com.tongji.tcloud.model.App;
import com.tongji.tcloud.model.RequestHelper;
import com.tongji.tcloud.model.User;
import com.tongji.tcloud.model.UsersResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActionSheet.ActionSheetListener {
    private DrawerLayout mDrawerLayout;

    private RelativeLayout rlFileManager, rlPasswordManager, rlUserManager;

    private int currentSelectItem = R.id.rl_fileManager;

    private UpdatePasswordFragment updatePasswordFragment;
    private PermissionFragment userFragment;
    private FolderFragment fileFragment;
    private Fragment fragment;
    private boolean IsRoot=false;

    private List<User> userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        String username=sharedPreferences.getString("username", "");
        if(username.equals("root")){
            IsRoot=true;
        }
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        initLeftMenu();//初始化左边菜单

        fileFragment = new FolderFragment();
        fragment =fileFragment;
		getSupportFragmentManager().beginTransaction().add(R.id.content_frame,fileFragment).commit();
    }

    private void initLeftMenu() {

        rlFileManager = (RelativeLayout) findViewById(R.id.rl_fileManager);
        rlPasswordManager = (RelativeLayout) findViewById(R.id.rl_password);

        rlUserManager = (RelativeLayout) findViewById(R.id.rl_user);
        if(!IsRoot){
            rlUserManager.setVisibility(View.INVISIBLE);
        }
        rlFileManager.setOnClickListener(onLeftMenuClickListener);
        rlPasswordManager.setOnClickListener(onLeftMenuClickListener);
        rlUserManager.setOnClickListener(onLeftMenuClickListener);

        rlFileManager.setSelected(true);
    }

    private OnClickListener onLeftMenuClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentSelectItem != v.getId()) {//防止重复点击
                currentSelectItem = v.getId();

                rlFileManager.setSelected(false);
                rlPasswordManager.setSelected(false);
                rlUserManager.setSelected(false);

                changeFragment(v.getId());//设置fragment显示切换
                switch (v.getId()) {
                    case R.id.rl_fileManager:
                        rlFileManager.setSelected(true);
                        break;
                    case R.id.rl_password:
                        rlPasswordManager.setSelected(true);
                        break;
                    case R.id.rl_user:
                        rlUserManager.setSelected(true);
                        break;
                }
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        }
    };

    /**
     * 改变fragment的显示
     *
     * @param resId
     */
    private void changeFragment(int resId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();//开启一个Fragment事务

        hideFragments(transaction);//隐藏所有fragment
        if (resId == R.id.rl_fileManager) {
            if (fileFragment == null) {
                fileFragment = new FolderFragment();
                transaction.add(R.id.content_frame, fileFragment);
            } else {
                transaction.show(fileFragment);
            }
            fragment =fileFragment;
        } else if (resId == R.id.rl_password) {
            if (updatePasswordFragment == null) {
                updatePasswordFragment = new UpdatePasswordFragment();
                transaction.add(R.id.content_frame, updatePasswordFragment);
            } else {
                transaction.show(updatePasswordFragment);
            }
            fragment = updatePasswordFragment;
        } else if (resId == R.id.rl_user) {
            UsersResult usersResult=new RequestHelper().GetUsers();
            userList=new ArrayList<>();
            String users[]=new String[usersResult.data.users.size()];
            for(User user:usersResult.data.users) {
                userList.add(user);
                users[userList.indexOf(user)]=user.username;
            }
            ActionSheet.createBuilder(this, getSupportFragmentManager())
                    .setCancelButtonTitle("取消")
                    .setOtherButtonTitles(users)
                    .setCancelableOnTouchOutside(true)
                    .setListener(this).show();
        }
        transaction.commitAllowingStateLoss();//一定要记得提交事务
    }

    /**
     * 显示之前隐藏所有fragment
     *
     * @param transaction
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (fileFragment != null)//不为空才隐藏,如果不判断第一次会有空指针异常
            transaction.hide(fileFragment);
        if (updatePasswordFragment != null)
            transaction.hide(updatePasswordFragment);
        if (userFragment != null)
            transaction.hide(userFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(fragment instanceof FolderFragment){
            return ((FolderFragment) fragment).onKeyDown(keyCode, event);
        }
        else if(fragment instanceof PermissionFragment){
            return ((PermissionFragment) fragment).onKeyDown(keyCode, event);
        }
        else{
            return onKeyDown(keyCode,event);
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();//开启一个Fragment事务
        hideFragments(transaction);
        String username=userList.get(index).username;
        if (userFragment == null) {
            userFragment = new PermissionFragment();
            transaction.add(R.id.content_frame, userFragment);
        } else {
            transaction.show(userFragment);
        }
        userFragment.username=username;
        fragment =userFragment;
        Toast.makeText(getApplicationContext(), "当前用户:"+username,Toast.LENGTH_SHORT).show();
        transaction.commitAllowingStateLoss();
    }
}
