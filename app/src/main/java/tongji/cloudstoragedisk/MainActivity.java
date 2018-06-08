package tongji.cloudstoragedisk;

import android.annotation.SuppressLint;
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tongji.cloudstoragedisk.Model.App;
import tongji.cloudstoragedisk.Model.ConfigHelper;
import tongji.cloudstoragedisk.Model.Directory;
import tongji.cloudstoragedisk.Model.FolderAdapter;
import tongji.cloudstoragedisk.Model.FoldersResult;
import tongji.cloudstoragedisk.Model.PermisionUtils;
import tongji.cloudstoragedisk.Model.RequestHelper;
import tongji.cloudstoragedisk.Model.RequestResult;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private ListView listView;
    private ProgressBar progressBar;
    private static FoldersResult foldersResult;
    private static RequestResult requestResult;
    private List<Map<String,Object>> list;
    private FolderAdapter adapter;
    private RequestHelper requestHelper;
    private String currentPath;
    int totalSize;
    int finishedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        progressBar =(ProgressBar)findViewById(R.id.progress);
        //RequestResult requestResult= (new RequestHelper()).Login("root","123456");
        foldersResult=(new RequestHelper()).GetFolders("/home");
        currentPath="/home";
        listView = (ListView) findViewById(R.id.listView);
        list=getData(foldersResult);
        adapter = new FolderAdapter(this,foldersResult.data.subdic,list,
                R.layout.list_item,
                new String[]{"item_image","item_tv_main","item_tv_time"},
                new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time}
        );
        adapter.setListener(oprationListener);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(!currentPath.equals("/home")){
                currentPath=currentPath.substring(0,currentPath.lastIndexOf('/'));
                foldersResult=(new RequestHelper()).GetFolders(currentPath);
                list=getData(foldersResult);
                adapter = new FolderAdapter(this,foldersResult.data.subdic,list,
                        R.layout.list_item,
                        new String[]{"item_image","item_tv_main","item_tv_time"},
                        new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time}
                );
                adapter.setListener(oprationListener);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            }
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private FolderAdapter.OprationListener oprationListener=new FolderAdapter.OprationListener(){
        @Override
        public void download(final Directory directory) {
            new Thread() {
                @SuppressLint("SdCardPath")
                public void run() {
                    try {
                        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
                        String path=URLEncoder.encode(directory.path, HTTP.UTF_8);
                        String filename = directory.path.substring(directory.path.lastIndexOf('/') + 1);
                        downloadFile(host+"/download?path="+ path,"/sdcard/Download/",filename);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        @Override
        public void upload(final Directory directory) {
            new Thread() {
                public void run() {

                }
            }.start();
        }
        @Override
        public void delete(final Directory directory) {
            List<String> paths=new ArrayList<String>();
            paths.add(directory.path);
            requestResult=(new RequestHelper()).Delete(paths);
            Toast.makeText(MainActivity.this, requestResult.msg, Toast.LENGTH_SHORT).show() ;
            if(requestResult.success){
                list.remove(foldersResult.data.subdic.indexOf(directory));
                foldersResult.data.subdic.remove(directory);
                adapter.notifyDataSetChanged();
            }
        }
    };
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        final Directory selected=foldersResult.data.subdic.get(position);
        if(selected.type.equals("folder")){
            foldersResult=(new RequestHelper()).GetFolders(selected.path);
            currentPath=selected.path;
            list=getData(foldersResult);
            adapter = new FolderAdapter(this,foldersResult.data.subdic,list,
                    R.layout.list_item,
                    new String[]{"item_image","item_tv_main","item_tv_time"},
                    new int[]{R.id.item_image,R.id.item_tv_main,R.id.item_tv_time}
            );
            adapter.setListener(oprationListener);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
//        else{
//            new Thread() {
//                @SuppressLint("SdCardPath")
//                public void run() {
//                    try {
//                        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
//                        String path=URLEncoder.encode(selected.path, HTTP.UTF_8);
//                        String filename = selected.path.substring(selected.path.lastIndexOf('/') + 1);
//                        downloadFile(host+"/download?path="+ path,"/sdcard/Download/",filename);
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }.start();
//        }
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
                        Toast.makeText(MainActivity.this, "文件下载开始", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setMax(totalSize);
                    case 1:
                        progressBar.setProgress(finishedSize);
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this, "文件下载完成", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case -1:
                        String error = msg.getData().getString("error");
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };
    public void downloadFile(String url, String path,String filename){
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            InputStream inputStream = conn.getInputStream();
            this.totalSize = conn.getContentLength();
            PermisionUtils.verifyStoragePermissions(this);
            FileOutputStream fileOutputStream = new FileOutputStream(path + filename);
            byte buffer[] = new byte[4096];
            finishedSize = 0;
            sendMsg(0);
            while (true) {
                int result = inputStream.read(buffer);
                if (result == -1) {
                    break;
                }
                fileOutputStream.write(buffer, 0, result);
                fileOutputStream.flush();
                finishedSize += result;
                sendMsg(1);
            }
            sendMsg(2);
            inputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void uploadFile(String url, String path,String filename){
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            InputStream inputStream = conn.getInputStream();
            this.totalSize = conn.getContentLength();
            PermisionUtils.verifyStoragePermissions(this);
            FileOutputStream fileOutputStream = new FileOutputStream(path + filename);
            byte buffer[] = new byte[4096];
            finishedSize = 0;
            sendMsg(0);
            while (true) {
                int result = inputStream.read(buffer);
                if (result == -1) {
                    break;
                }
                fileOutputStream.write(buffer, 0, result);
                fileOutputStream.flush();
                finishedSize += result;
                sendMsg(1);
            }
            sendMsg(2);
            inputStream.close();
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
