package com.yunlinker.baseclass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.yunlinker.auth.WebPermissionManager;
import com.yunlinker.manager.ActivityManager;
import com.yunlinker.upimage.UpImger;
import com.yunlinker.zxing.android.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.yunlinker.auth.WebPermissionManager.UpImgPermissions;
import static com.yunlinker.config.WebConfig.AssestRoot;
import static com.yunlinker.config.WebConfig.saveKey;

/**
 * Created by lemon on 2017/7/27.
 */

public class BaseInspect {

    protected BaseActivity mactivity;
    protected BaseWebView mweb;
    private static final int REQUEST_CODE_SCAN = 0x0000;

    public BaseInspect(BaseWebView w, BaseActivity a) {
        mactivity = a;
        mweb = w;

    }

    //打开控制器
    @JavascriptInterface
    public void go(final String url) {
        ActivityManager.getInstance().start(url);
    }

    //关闭控制器
    @JavascriptInterface
    public void close(String count) {
        int c = Integer.parseInt(count);
        if(c<1)
            c=1;
        ActivityManager.getInstance().back(c);
    }

    @JavascriptInterface
    public void gotop(String url){
        if(TextUtils.isEmpty(url)){
            ActivityManager.getInstance().finishButFirst();
        }else{
            ActivityManager.getInstance().finishButTop();
            if(!url.contains("http:"))
                url = AssestRoot+url;
            final String fUrl = url;
            mactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mweb.loadUrl(fUrl);
                }
            });
        }
    }
    @JavascriptInterface
    public void topgo(final String url){
        if(!TextUtils.isEmpty(url)){
            ActivityManager.getInstance().finishButFirst();
            ActivityManager.getInstance().start(url);
        }
    }


    //数据存储
    @JavascriptInterface
    public void storageValue(String obj) {
        try{
            JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("storageValue",jb.getString("code"));
            SharedPreferences sp = mactivity.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            String key = jb.getString("key");
            String value = jb.getString("value");
            editor.putString(key, value);
            editor.apply();
            mweb.setValue("storageValue", value);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //数据读取
    @JavascriptInterface
    public void storage(String obj) {
        try {
            JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("storage",jb.getString("code"));
            SharedPreferences sp = mactivity.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
            String val = sp.getString(jb.getString("key"), "");
            mweb.setValue("storage", val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void getVersion(String code){
        try{
            mweb.setInsCode("getVersion",code);
            PackageManager pm = mactivity.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mactivity.getPackageName(), 0);
            mweb.setValue("getVersion", "{\"versionName\":\""+pi.versionName+"\",\"versionCode\":\"" + pi.versionCode + "\",\"versionType\":\"android\"}");
        }catch (Exception e){}
    }

    //comfirm弹窗
    @JavascriptInterface
    public void confirm(String obj) {
        try {
            JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("confirm",jb.getString("code"));
            showConfirm(jb.getString("mess"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showConfirm(String mess){
        mactivity.dialog = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(mactivity);
        builder.setMessage(mess);
        builder.setCancelable(false);
        builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mweb.setValue("confirm","1");
                dialog.cancel();
            }
        });
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mweb.setValue("confirm","0");
                dialog.cancel();
            }
        });
        mactivity.dialog = builder.create();
        mactivity.dialog.show();
    }

    //alert弹窗
    @JavascriptInterface
    public void alert(String obj) {
        try {
            JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("alert",jb.getString("code"));
            showAlert(jb.getString("mess"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String mess){
        mactivity.dialog = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(mactivity);
        builder.setMessage(mess);
        builder.setCancelable(false);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mweb.setValue("alert","1");
                dialog.cancel();
            }
        });
        mactivity.dialog = builder.create();
        mactivity.dialog.show();
    }

    //message弹窗
    @JavascriptInterface
    public void message(String mess) {
        Toast toast = Toast.makeText(mactivity.getApplicationContext(), mess,
                Toast.LENGTH_SHORT);
        //可以控制toast显示的位置
        toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
        toast.show();
    }


    //获取缓存大小
    @JavascriptInterface
    public void getCacheSize(String code) {
        mweb.setInsCode("getCacheSize", code);
        String gs = BaseTools.getCacheSize(mactivity);
        mweb.setValue("getCacheSize",gs);
    }
    //清除缓存
    @JavascriptInterface
    public void clearCache(String code) {
        mweb.setInsCode("clearCache", code);
        BaseTools.clearAllCache(mactivity);
        mweb.setValue("clearCache","1");
    }


    @JavascriptInterface
    public void upimg(String obj) {
        try{
            final JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("upimg",jb.getString("code"));
            WebPermissionManager.getInstance(mactivity).CheckPermission(UpImgPermissions, new WebPermissionManager.OnPermissionBack() {
                @Override
                public void success() {
                    UpImger.getInstance().upimgs(jb, mactivity, mweb);
                }

                @Override
                public void error() {}
            });
        }catch(Exception e){}
    }

    @JavascriptInterface
    public void scanf(String code) {
        if (ContextCompat.checkSelfPermission(mactivity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mactivity.requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_SCAN);
        } else {
            Intent intent = new Intent(mactivity, CaptureActivity.class);
            mactivity.startActivityForResult(intent, REQUEST_CODE_SCAN);
            BaseActivity.tempcode = code;
        }

    }


    @JavascriptInterface
    public void uploadFile(String urls){
        try {
            JSONObject jb = new JSONObject(urls);
            String url = jb.getString("urls");


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }





}
