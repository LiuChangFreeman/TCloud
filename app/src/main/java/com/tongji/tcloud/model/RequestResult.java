package com.tongji.tcloud.model;

import java.util.Map;

public class RequestResult {
    public boolean success;
    public String msg;
    public int errorCode;
    public String error;
    public Map<String,String> data;
}
