package com.yunlinker.hhgys;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yunlinker.baseclass.BaseActivity;
import com.yunlinker.manager.ActivityManager;
import com.yunlinker.manager.HttpManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.yunlinker.config.WebConfig.checkUpdateUrl;
import static com.yunlinker.config.WebConfig.downloadUrl;
import static com.yunlinker.myApp.checkUpdate;

public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //创建时加入栈管理中
        ActivityManager.getInstance().add(MainActivity.this);

        if(!checkUpdate) {
//            checkUpdate();
            checkUpdate = true;
        }
    }




    //检查更新
    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = HttpManager.GET(checkUpdateUrl);
                try {
                    JSONObject obj = new JSONObject(res);
                    if(obj.has("code") && obj.getInt("code")==1) {
                        JSONArray data = obj.getJSONArray("data");
                        if(data.length()>0) {
                            JSONObject o = data.getJSONObject(0);
                            PackageManager pm = getApplicationContext().getPackageManager();
                            try {
                                PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
                                if(o.getInt("sysvalue") > pi.versionCode) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setMessage("发现新版本，是否更新？");
                                            builder.setCancelable(false);
                                            builder.setNegativeButton("更新", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    final Uri uri = Uri.parse(downloadUrl);
                                                    final Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                                    MainActivity.this.startActivity(it);
                                                    dialog.cancel();
                                                }
                                            });
                                            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    });
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
