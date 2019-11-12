package com.yunlinker.auth;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lemon on 2017/8/16.
 */

public class WebPermissionManager {

    public static String[] LocationPermissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    public static String[] UpImgPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static String[] StoragePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static String[] GetContactsPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS};

    private static WebPermissionManager instance = null;
    private Activity ma = null;
    private Map<String,OnPermissionBack> codeMap = null;
    private int requestCode = 5000;
    public static WebPermissionManager getInstance(Activity a) {
        synchronized (WebPermissionManager.class) {
            if (instance == null) {
                instance = new WebPermissionManager();
                instance.ma = a;
                instance.codeMap = new HashMap<>();
            }
        }
        return instance;
    }



    public void CheckPermission(String[] permissions, OnPermissionBack back) {
        ArrayList<String> nops = new ArrayList<>();
        for(String per:permissions) {
            if(ContextCompat.checkSelfPermission(ma,per)!= PackageManager.PERMISSION_GRANTED) {
                nops.add(per);
            }
        }
        if(nops.size()>0) {
            requestCode ++;
            codeMap.put(String.valueOf(requestCode), back);
            ActivityCompat.requestPermissions(ma,nops.toArray(new String[nops.size()]),requestCode);
        } else {
            if(back!=null)
                back.success();
        }
    }

    //授权后回调
    public interface OnPermissionBack {
        public void success();
        public void error();
    }



    //授权结果
    public void authBack(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        OnPermissionBack b = codeMap.get(String.valueOf(requestCode));
        for(int p:grantResults) {
            if(p != PackageManager.PERMISSION_GRANTED) {
                b.error();
                return;
            }
        }
        if(b!=null) {
            b.success();
        }
    }

}
