package com.yunlinker;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liulishuo.filedownloader.FileDownloader;
import com.qiyukf.unicorn.api.ImageLoaderListener;
import com.qiyukf.unicorn.api.SavePowerConfig;
import com.qiyukf.unicorn.api.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.UICustomization;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.UnicornImageLoader;
import com.qiyukf.unicorn.api.UnreadCountChangeListener;
import com.qiyukf.unicorn.api.YSFOptions;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.yunlinker.baseclass.BaseActivity;
//import com.yunlinker.image.GlideApp;
import com.yunlinker.hhgys.R;
import com.yunlinker.manager.ActivityManager;
import com.yunlinker.hhgys.MainActivity;
import com.yunlinker.upimage.GlideImageLoader;
import com.lzy.imagepicker.ImagePicker;
import com.yunlinker.util.NotificationsUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.yunlinker.config.WebConfig.QQID;
import static com.yunlinker.config.WebConfig.QQSECRET;
import static com.yunlinker.config.WebConfig.QYSECRET;
import static com.yunlinker.config.WebConfig.UMKEY;
import static com.yunlinker.config.WebConfig.WXID;
import static com.yunlinker.config.WebConfig.WXSECRET;
import static com.yunlinker.config.WebConfig.saveKey;

/**
 * Created by lemon on 2017/8/21.
 */

public class myApp extends Application {
 //private Context mContext;
 private static Context mContext;
    private  NotificationManager notificationManager;
    private NotificationChannel mChannel;
    private static Handler mainHandler;
    private Handler mHandler = new Handler();
    public static void setMainhandler(Handler handler){
        mainHandler=handler;
    }
    private static myApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this,"5d68dfca4ca3574e5c00058e","Umeng", UMConfigure.DEVICE_TYPE_PHONE, "eba7418cd68169b15317daab47fc5a25");
        initImagePicker();
        UMConfigure.setLogEnabled(true);
        UMConfigure.setEncryptEnabled(false);
        initUmeng();
        UMShareAPI.get(this);
        mContext = getApplicationContext();
//        initQiyu();
        FileDownloader.setupOnApplicationOnCreate(this);
        // initdialog();
    }


    public static Context getContext() {
        return mContext;
    }


    private void initImagePicker() {
        //初始化图片选择器
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }

    private void initUmeng() {

        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(getApplicationContext(),UMKEY,"OnlineApp"));

        final PushAgent mPushAgent = PushAgent.getInstance(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //注册推送服务，每次调用register方法都会回调该接口
                mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);
                mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
                mPushAgent.register(new IUmengRegisterCallback() {

                    @Override
                    public void onSuccess(String deviceToken) {
                        //注册成功会返回device token
                        Log.e("deviceToken", deviceToken);
                        if(deviceToken!=null) {
                            SharedPreferences sp = getApplicationContext().getSharedPreferences(saveKey, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("deviceToken", deviceToken);
                            editor.apply();
                        }
                    }

                    @Override
                    public void onFailure(String s, String s1) {

                    }
                });

                UmengMessageHandler messageHandler = new UmengMessageHandler(){
                    @Override
                    public Notification getNotification(Context context, UMessage msg) {
                        switch (msg.builder_id) {
                            case 1:
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                    //准备intent
                                    Intent intent = new Intent();
                                    String action = "com.tamic.myapp.action";
                                    intent.setAction(action);
                                    final int NOTIFICATION_ID = 12234;

                                    Notification notification = null;

                                    String CHANNEL_ID = "my_channel_0";
                                    CharSequence name = "my_channel";
                                    String Description = "This is my channel";
                                    int importance = NotificationManager.IMPORTANCE_HIGH;

                                    mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                                    mChannel.setDescription(Description);
                                    mChannel.enableLights(true);
                                    mChannel.setLightColor(Color.RED);
                                    mChannel.enableVibration(true);
                                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                                    mChannel.setShowBadge(true);
                                    Uri mUri = Uri.parse("android.resource://"+ context.getPackageName() +"/"+ R.raw.umqqqq);
                                    mChannel.setSound(mUri,Notification.AUDIO_ATTRIBUTES_DEFAULT);
                                    notificationManager.createNotificationChannel(mChannel);

                                    notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle(msg.title)
                                            .setContentText(msg.text)
                                            .setTicker(msg.ticker)
                                            .setChannelId(CHANNEL_ID)
                                            .setAutoCancel(true)
                                            .setSound(Uri.parse("android.resource://"+ context.getPackageName() +"/"+R.raw.umqqqq))
                                            .build();
                                    return  notification;
                                }
                            default:
                                return super.getNotification(context, msg);
                        }

                    }
                };
                mPushAgent.setMessageHandler(messageHandler);

                UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
                    @Override
                    public void dealWithCustomAction(Context context, UMessage msg) {
                        // Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationManager.deleteNotificationChannel(mChannel.getId());
                        }
                        Log.e("1111111", "dealWithCustomAction: "+msg.custom);
                        try{
                            JSONObject object =new JSONObject(msg.custom);
                            String id = object.getString("orderId");
                            String type = object.getString("type");
                            String url="";
                            if (id!=null&&!id.equals("")&&type!=null&&type.equals("2")){
                                url = "orderDetail.html?oid="+ id+"&ifFlower=0";
                            }
                            if (type!=null&&type.equals("1")&&id.equals("0")){
                                url = "mess.html";
                            }
                            if (mainHandler!=null){
                                mainHandler.obtainMessage(BaseActivity.openNewDetali,url).sendToTarget();
                            }

                        }catch (Exception e){
                            Log.e("Exception", "dealWithCustomAction: "+e);
                        }
                    }
                };
                mPushAgent.setNotificationClickHandler(notificationClickHandler);



            }
        }).start();
    }

    {
        //设置Log开关
        UMConfigure.setLogEnabled(true);
        PlatformConfig.setWeixin(WXID, WXSECRET);
        PlatformConfig.setQQZone(QQID, QQSECRET);
        PlatformConfig.setTwitter("2QCb1sVy6qXUJEVrslzVNfKzX","2VAMzLpBzNE34mZ2RVq40laKTOBCl1LQNjWTxhXreUuIQvrE2M");
    }



    static public boolean checkUpdate = false;


    private void initQiyu() {
        //----qiyu---
        Unicorn.init(this, QYSECRET, options(), new UnicornImageLoader(){
            @Nullable
            @Override
            public Bitmap loadImageSync(String uri, int width, int height) {
                return null;
            }
            @Override
            public void loadImage(String uri, int width, int height, final ImageLoaderListener listener) {
                if(!TextUtils.isEmpty(uri)){}
                    Glide.with(getApplicationContext()).asBitmap().load(uri).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            listener.onLoadComplete(resource);
                        }
                    });
            }
        });

        Unicorn.addUnreadCountChangeListener(new UnreadCountChangeListener() {
            @Override
            public void onUnreadCountChange(int count) {
                BaseActivity a = ActivityManager.getInstance().getFirst();
                if(a!=null) {
                    a.getWebView().loadUrl("javascript:qiyuMsgCount&&qiyuMsgCount("+count+")");
                }
            }
        }, true);
    }

    //----qiyu---如果返回值为null，则全部使用默认参数。
    public static YSFOptions options() {
        YSFOptions options = new YSFOptions();
        options.statusBarNotificationConfig = new StatusBarNotificationConfig();
        options.statusBarNotificationConfig.notificationEntrance = MainActivity.class;
        options.savePowerConfig = new SavePowerConfig();
        options.uiCustomization = new UICustomization();
        options.uiCustomization.leftAvatar = "";
        return options;
    }




}
