package com.tongji.tcloud.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class RequestHelper {
    public RequestResult Login(String username,String password){
        RequestResult requestResult=new RequestResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpPost httpPost = new HttpPost(host+"/login");
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpPost.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("mobile", "true"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                requestResult= JSON.parseObject(result, RequestResult.class);
            }
            SharedPreferences preferences = App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
            List<Cookie> theCookies =  ((AbstractHttpClient) httpClient).getCookieStore().getCookies();
            StringBuilder sb = new StringBuilder();
            for(int k=0; k<theCookies.size(); k++)
            {
                sb.append(theCookies.get(k).getName()).append("=").append(theCookies.get(k).getValue()).append(";");
            }
            preferences.edit().putString("cookies",sb.toString()).apply();
            preferences.edit().putString("username",username).apply();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return  requestResult;
    }
    public RequestResult LoginOff(String username){
        RequestResult requestResult=new RequestResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpGet httpGet = new HttpGet(host+"/logoff?username="+username);
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpGet.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                requestResult= JSON.parseObject(result, RequestResult.class);
            }
            SharedPreferences preferences = App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
            preferences.edit().remove("cookies").apply();
            preferences.edit().remove("username").apply();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return requestResult;
    }
    public RequestResult Register(String username,String password){
        RequestResult requestResult=new RequestResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpPost httpPost = new HttpPost(host+"/register");
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpPost.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("mobile", "true"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                requestResult= JSON.parseObject(result, RequestResult.class);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            httpClient.getConnectionManager().shutdown();
        }
        return  requestResult;
    }
    public RequestResult ChangePassword(String username,String oldpassword,String newpassword1,String newpassword2){
        RequestResult requestResult=new RequestResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpPost httpPost = new HttpPost(host+"/changepassword");
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpPost.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("oldpassword", oldpassword));
        params.add(new BasicNameValuePair("newpassword1", newpassword1));
        params.add(new BasicNameValuePair("newpassword2", newpassword2));
        params.add(new BasicNameValuePair("mobile", "true"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                requestResult= JSON.parseObject(result, RequestResult.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return  requestResult;
    }
    public FoldersResult GetFolders(String path){
        FoldersResult foldersResult=new FoldersResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpGet httpGet = new HttpGet(host+"/main?path="+path);
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpGet.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                foldersResult= JSON.parseObject(result, FoldersResult.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return  foldersResult;
    }
    public RequestResult Delete(List<String> path){
        RequestResult requestResult=new RequestResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpPost httpPost = new HttpPost(host+"/multiple");
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpPost.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("mobile", "true"));
        params.add(new BasicNameValuePair("submit", "删除"));
        for (String item:path) {
            params.add(new BasicNameValuePair("path", item));
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                requestResult= JSON.parseObject(result, RequestResult.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return  requestResult;
    }
    public FoldersResult ReadPermission(String path,String username){
        FoldersResult foldersResult=new FoldersResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpGet httpGet = new HttpGet(host+"/permission?path="+path+"&username="+username);
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpGet.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                foldersResult= JSON.parseObject(result, FoldersResult.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return foldersResult;
    }
    public RequestResult SetPermission(String path,String username,String permission){
        RequestResult requestResult=new RequestResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpPost httpPost = new HttpPost(host+"/permission");
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpPost.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("submit", permission));
        params.add(new BasicNameValuePair("path", path));
        params.add(new BasicNameValuePair("mobile", "true"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                requestResult= JSON.parseObject(result, RequestResult.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return  requestResult;
    }
    public UsersResult GetUsers(){
        UsersResult usersResult=new UsersResult();
        HttpClient httpClient=new DefaultHttpClient();
        String host=ConfigHelper.getProperties(App.getAppContext(), "host");
        HttpGet httpGet = new HttpGet(host+"/manage");
        SharedPreferences sharedPreferences =  App.getAppContext().getSharedPreferences("cookies", MODE_PRIVATE);
        httpGet.setHeader("Cookie", sharedPreferences.getString("cookies", ""));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                String result = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                usersResult= JSON.parseObject(result, UsersResult.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return  usersResult;
    }
}
