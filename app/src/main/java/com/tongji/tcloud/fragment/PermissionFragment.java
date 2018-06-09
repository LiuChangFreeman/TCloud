package com.tongji.tcloud.fragment;

import com.baoyz.actionsheet.ActionSheet;
import com.tongji.tcloud.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tongji.tcloud.R;
import com.tongji.tcloud.activity.FolderActivity;
import com.tongji.tcloud.adapter.FolderAdapter;
import com.tongji.tcloud.adapter.PermissionAdapter;
import com.tongji.tcloud.model.*;

import static android.content.Context.MODE_PRIVATE;

public class PermissionFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView listView;
    private FoldersResult foldersResult;
    private static List<Map<String,Object>> list;
    private PermissionAdapter adapter;
    private static String currentPath;
    public String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_folder, container,false);
        listView = (ListView)view.findViewById(R.id.listView);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RequestResult requestResult= (new RequestHelper()).Login("root","123456");
        foldersResult=(new RequestHelper()).ReadPermission("/home",username);
        currentPath="/home";
        list=getData(foldersResult);
        adapter = new PermissionAdapter(getActivity(),foldersResult.data.subdic,list,
                R.layout.list_item_permission,
                new String[]{"item_image","item_tv_main","item_tv_time",
                        "item_rd","item_wr","item_only"},
                new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time,
                        R.id.item_rd, R.id.item_wr, R.id.item_only}
        );
        adapter.setListener(oprationListener);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return view;
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(!currentPath.equals("/home")){
                currentPath=currentPath.substring(0,currentPath.lastIndexOf('/'));
                foldersResult=(new RequestHelper()).GetFolders(currentPath);
                list=getData(foldersResult);
                adapter = new  PermissionAdapter(getActivity(),foldersResult.data.subdic,list,
                        R.layout.list_item_permission,
                        new String[]{"item_image","item_tv_main","item_tv_time",
                                "item_rd","item_wr","item_only"},
                        new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time,
                        R.id.item_rd, R.id.item_wr, R.id.item_only}
                );
                adapter.setListener(oprationListener);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            }
            return true;
        }
        else {
            return onKeyDown(keyCode,event);
        }
    }

    private  PermissionAdapter.OprationListener oprationListener=new  PermissionAdapter.OprationListener(){
        @Override
        public void readonly(final Directory directory) {
            RequestResult requestResult= (new RequestHelper()).SetPermission(directory.path,username,"只读");
            if(requestResult.success){
                for (Directory item: foldersResult.data.subdic) {
                    if(item==directory){
                        item.permission="只读";
                    }
                }
                Map<String,Object> map= list.get(foldersResult.data.subdic.indexOf(directory));
                map.put("item_rd","#ffffff");
                map.put("item_wr","#d6d6d6");
                map.put("item_only","#d6d6d6");
                adapter.notifyDataSetChanged();
                String notice=String.format("用户 %s 路径 %s 权限设置为:%s成功!",username,directory.path,"只读");
                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(), requestResult.error, Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void write(final Directory directory) {
            RequestResult requestResult= (new RequestHelper()).SetPermission(directory.path,username,"读写");
            if(requestResult.success){
                for (Directory item: foldersResult.data.subdic) {
                    if(item==directory){
                        item.permission="读写";
                    }
                }
                Map<String,Object> map= list.get(foldersResult.data.subdic.indexOf(directory));
                map.put("item_rd","#d6d6d6");
                map.put("item_wr","#ffffff");
                map.put("item_only","#d6d6d6");
                adapter.notifyDataSetChanged();
                String notice=String.format("用户 %s 路径 %s 权限设置为:%s成功!",username,directory.path,"读写");
                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(), requestResult.error, Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void only(final Directory directory) {
            RequestResult requestResult= (new RequestHelper()).SetPermission(directory.path,username,"隐藏");
            if(requestResult.success){
                for (Directory item: foldersResult.data.subdic) {
                    if(item==directory){
                        item.permission="隐藏";
                    }
                }
                Map<String,Object> map= list.get(foldersResult.data.subdic.indexOf(directory));
                map.put("item_rd","#d6d6d6");
                map.put("item_wr","#d6d6d6");
                map.put("item_only","#ffffff");
                adapter.notifyDataSetChanged();
                String notice=String.format("用户 %s 路径 %s 权限设置为:%s成功!",username,directory.path,"隐藏");
                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(), requestResult.error, Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        final Directory selected=foldersResult.data.subdic.get(position);
        if(selected.type.equals("folder")){
            foldersResult=(new RequestHelper()).ReadPermission(selected.path,username);;
            currentPath=selected.path;
            list=getData(foldersResult);
            adapter = new  PermissionAdapter(getActivity(),foldersResult.data.subdic,list,
                    R.layout.list_item_permission,
                    new String[]{"item_image","item_tv_main","item_tv_time",
                            "item_rd","item_wr","item_only"},
                    new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time,
                            R.id.item_rd, R.id.item_wr, R.id.item_only}
            );
            adapter.setListener(oprationListener);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
    }
    private List<Map<String,Object>> getData(FoldersResult foldersResult){
        List<Directory> data=foldersResult.data.subdic;
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        for (Directory item:data) {
            Map<String,Object> map = new HashMap<String,Object>();
            if(item.type.equals("file")){
                map.put("item_image",R.drawable.file);
            }
            else{
                map.put("item_image",R.drawable.folder_unshared);
            }
            map.put("item_tv_main",item.path.substring(item.path.lastIndexOf('/') + 1));
            map.put("item_tv_time",item.time);
            if(item.permission.equals("只读")){
                map.put("item_rd","#ffffff");
            }
            else{
                map.put("item_rd","#d6d6d6");
            }
            if(item.permission.equals("读写")){
                map.put("item_wr","#ffffff");
            }
            else{
                map.put("item_wr","#d6d6d6");
            }
            if(item.permission.equals("隐藏")){
                map.put("item_only","#ffffff");
            }
            else{
                map.put("item_only","#d6d6d6");
            }
            list.add(map);
        }
        return list;
    }
}

