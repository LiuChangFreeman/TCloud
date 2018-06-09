package com.tongji.tcloud.fragment;

import com.alibaba.fastjson.JSON;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.tongji.tcloud.R;

import android.content.Intent;
import android.net.Uri;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.tongji.tcloud.model.*;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class FolderFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final int REQUESTCODE_FROM_FRAGMENT = 1000;
    private ListView listView;
    private ProgressBar progressBar;
    private FoldersResult foldersResult;
    private RequestResult requestResult;
    private List<Map<String,Object>> list;
    private FolderAdapter adapter;
    private String currentPath;
    private Directory selectedDirectory;
    private String savePath;
    private String host;
    int totalSize;
    int finishedSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_folder, container,false);
        savePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/";
        host=ConfigHelper.getProperties(App.getAppContext(), "host");
        listView = (ListView)view.findViewById(R.id.listView);
        progressBar =(ProgressBar)view.findViewById(R.id.progress);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        foldersResult=(new RequestHelper()).GetFolders("/home");
        currentPath="/home";
        list=getData(foldersResult);
        adapter = new FolderAdapter(getActivity(),foldersResult.data.subdic,list,
                R.layout.list_item,
                new String[]{"item_image","item_tv_main","item_tv_time"},
                new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time}
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
                adapter = new FolderAdapter(getActivity(),foldersResult.data.subdic,list,
                        R.layout.list_item,
                        new String[]{"item_image","item_tv_main","item_tv_time"},
                        new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time}
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

    private FolderAdapter.OprationListener oprationListener=new FolderAdapter.OprationListener(){
        @Override
        public void download(final Directory directory) {
            new Thread() {
                public void run() {
                    try {
                        if(directory.type.equals("file")){
                            String path=URLEncoder.encode(directory.path, HTTP.UTF_8);
                            String filename = directory.path.substring(directory.path.lastIndexOf('/') + 1);
                            downloadFile(host+"/download?path="+ path,savePath,filename);
                        }
                        else{
                            Map<String,String> params=new HashMap<>();
                            params.put("submit","下载");
                            params.put("path",directory.path);
                            String fileName=directory.path.substring(directory.path.lastIndexOf('/')+1)+".zip";
                            downloadFolder(host+"/multiple",params,savePath+fileName);
                            UnzipHelper.unzip(savePath+fileName,savePath,true);
                            File zipfile=new File(savePath+fileName);
                            zipfile.delete();
                            Message msg = new Message();
                            msg.what = -1;
                            Bundle data = new Bundle();
                            data.putString("error","文件夹传输完毕!");
                            msg.setData(data);
                            handler.sendMessage(msg);
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        @Override
        public void upload(final Directory directory) {
            selectedDirectory=directory;
            if(selectedDirectory.type.equals("folder")) {
                String startPath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                new LFilePicker().withSupportFragment(FolderFragment.this)
                        .withRequestCode(REQUESTCODE_FROM_FRAGMENT)
                        .withStartPath(startPath)
                        .withMutilyMode(false)
                        .withIconStyle(Constant.ICON_STYLE_YELLOW)
                        .withTitle("选择文件")
                        .start();
            }
            else{
                Message msg = new Message();
                msg.what = -1;
                Bundle data = new Bundle();
                data.putString("error","非文件夹不能上传");
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }
        @Override
        public void delete(final Directory directory) {
            List<String> paths=new ArrayList<String>();
            paths.add(directory.path);
            requestResult=(new RequestHelper()).Delete(paths);
            if(requestResult.success){
                Toast.makeText(getActivity(), requestResult.msg, Toast.LENGTH_SHORT).show() ;
                list.remove(foldersResult.data.subdic.indexOf(directory));
                foldersResult.data.subdic.remove(directory);
                adapter.notifyDataSetChanged();
            }
            else{
                Toast.makeText(getActivity(), requestResult.error, Toast.LENGTH_SHORT).show() ;
            }
        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_FRAGMENT) {
                List<String> list = data.getStringArrayListExtra("paths");
                final String path = list.get(0);
                new Thread() {
                    public void run() {
                        Map<String, String> params = new HashMap<>();
                        params.put("path", selectedDirectory.path);
                        params.put("submit", "点我上传文件");
                        uploadFile(host + "/upload", params, path);
                    }
                }.start();
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        final Directory selected=foldersResult.data.subdic.get(position);
        if(selected.type.equals("folder")){
            foldersResult=(new RequestHelper()).GetFolders(selected.path);
            currentPath=selected.path;
            list=getData(foldersResult);
            adapter = new FolderAdapter(getActivity(),foldersResult.data.subdic,list,
                    R.layout.list_item,
                    new String[]{"item_image","item_tv_main","item_tv_time"},
                    new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time}
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
                if(item.permission.equals("只读")){
                    map.put("item_image",R.drawable.folder_unshared);
                }
                else{
                    map.put("item_image",R.drawable.folder_shared);
                }
            }
            map.put("item_tv_main",item.path.substring(item.path.lastIndexOf('/') + 1));
            map.put("item_tv_time",item.time);
            list.add(map);
        }
        return list;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (!Thread.currentThread().isInterrupted())
            {
                switch (msg.what)
                {
                    case 0:
                        Toast.makeText(getActivity(), "传输开始", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setMax(totalSize);
                    case 1:
                        progressBar.setProgress(finishedSize);
                        break;
                    case 2:
                        Toast.makeText(getActivity(), "传输完成", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case -1:
                        String error = msg.getData().getString("error");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };
    public void downloadFile(String url, String path,String filename){
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            //connection.setChunkedStreamingMode(51200);
            connection.connect();

            this.totalSize = connection.getHeaderFieldInt("Accept-Length", 0);
            InputStream inputStream = connection.getInputStream();
            PermisionUtils.verifyStoragePermissions(getActivity());
            FileOutputStream outputStream = new FileOutputStream(path + filename);
            byte buffer[] = new byte[4096];
            finishedSize = 0;
            sendMsg(0);
            int result;
            while ((result = inputStream.read(buffer))!=-1) {
                sendMsg(1);
                outputStream.write(buffer, 0, result);
                outputStream.flush();
                finishedSize += result;
            }
            sendMsg(2);
            inputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void downloadFolder(String url,Map<String, String> params,String path){
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (params != null) {
                for (String key : params.keySet()) {
                    stringBuilder.append(key).append("=");
                    stringBuilder.append(Uri.encode(params.get(key))).append("&");
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
            connection.setRequestProperty("Cookie", sharedPreferences.getString("cookies", ""));
            connection.setRequestProperty("Content-Length",String.valueOf(stringBuilder.length()));
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(4096);
            connection.setConnectTimeout(10 * 60 * 1000);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(stringBuilder.toString().getBytes("UTF-8"));
            outputStream.close();

            this.totalSize = connection.getHeaderFieldInt("Accept-Length", 0);
            InputStream inputStream = connection.getInputStream();
            PermisionUtils.verifyStoragePermissions(getActivity());
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            byte[] buffer = new byte[4096];
            finishedSize = 0;
            sendMsg(0);
            int result;
            while ((result = inputStream.read(buffer))!=-1) {
                sendMsg(1);
                fileOutputStream.write(buffer, 0, result);
                fileOutputStream.flush();
                finishedSize += result;
            }
            sendMsg(2);
            inputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void uploadFile(String url,Map<String, String> params,String path){
        String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (params != null) {
                for (String key : params.keySet()) {
                    stringBuilder.append("--").append(BOUNDARY).append("\r\n");
                    stringBuilder.append("Content-Disposition: form-data; name=\"")
                            .append(key).append("\"").append("\r\n");
                    stringBuilder.append("\r\n");
                    stringBuilder.append(params.get(key)).append("\r\n");
                }
            }
            File uploadFile=new File(path);
            stringBuilder.append("--").append(BOUNDARY).append("\r\n");
            stringBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(path.substring(path.lastIndexOf('/') + 1)).append("\"").append("\r\n");
            stringBuilder.append("Content-Type: application/octet-stream" + "\r\n");
            stringBuilder.append("\r\n");
            byte[] headerInfo = stringBuilder.toString().getBytes("UTF-8");
            byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
            connection.setRequestProperty("Cookie", sharedPreferences.getString("cookies", ""));
            connection.setRequestProperty("Content-Length",String.valueOf(headerInfo.length + uploadFile.length() + endInfo.length));
            connection.setChunkedStreamingMode(4096);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10 * 60 * 1000);

            OutputStream outputStream = connection.getOutputStream();
            InputStream inputStream = new FileInputStream(uploadFile);
            outputStream.write(headerInfo);
            this.totalSize = (int)uploadFile.length();
            byte[] buffer = new byte[4096];
            finishedSize=0;
            sendMsg(0);
            int result;
            while ((result = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, result);
                outputStream.flush();
                finishedSize += result;
                sendMsg(1);
            }
            outputStream.write(endInfo);
            outputStream.close();
            inputStream.close();
            if (connection.getResponseCode() == 200) {
                sendMsg(2);
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(),"utf-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String resopnse = bufferedReader.readLine();
                RequestResult requestResult= JSON.parseObject(resopnse,RequestResult.class);
                if(!requestResult.success){
                    Message msg = new Message();
                    msg.what = -1;
                    Bundle data = new Bundle();
                    data.putString("error",requestResult.error);
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }
            else{
                if(!requestResult.success){
                    Message msg = new Message();
                    msg.what = -1;
                    Bundle data = new Bundle();
                    data.putString("error","网络异常");
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void sendMsg(int flag)
    {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }
}

