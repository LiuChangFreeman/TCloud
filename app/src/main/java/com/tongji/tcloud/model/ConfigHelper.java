package com.tongji.tcloud.model;

import android.content.Context;

import java.io.InputStream;
import java.util.Properties;

public class ConfigHelper {
    static String value;
    public static String getProperties(Context context, String key){
        Properties props = new Properties();
        try {
            InputStream in = context.getAssets().open("property.properties");
            props.load(in);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        value = props.getProperty(key);
        return value;
    }
}
