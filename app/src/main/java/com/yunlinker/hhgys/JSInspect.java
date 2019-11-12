package com.yunlinker.hhgys;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.qiyukf.unicorn.api.ConsultSource;
import com.qiyukf.unicorn.api.ProductDetail;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFOptions;
import com.qiyukf.unicorn.api.YSFUserInfo;
import com.tools.command.EscCommand;
import com.umeng.message.IUmengCallback;
import com.umeng.message.PushAgent;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.yunlinker.addressbook.AddressBook;
import com.yunlinker.auth.WebPermissionManager;
import com.yunlinker.baseclass.BaseActivity;
import com.yunlinker.baseclass.BaseInspect;
import com.yunlinker.baseclass.BaseWebView;
import com.yunlinker.hhgys.ui.Activity_DeviceList;
import com.yunlinker.image.ImageTool;
import com.yunlinker.manager.HttpManager;
import com.yunlinker.map.Location;
import com.yunlinker.myApp;
import com.yunlinker.pay.OnlinePay;
import com.yunlinker.printting.DeviceConnFactoryManager;
import com.yunlinker.printting.PrinterCommand;
import com.yunlinker.printting.ThreadFactoryBuilder;
import com.yunlinker.printting.ThreadPool;
import com.yunlinker.printting.Utils;
import com.yunlinker.printting.WifiParameterConfigDialog;
import com.yunlinker.printting.checkClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import HPRTAndroidSDKTSPL.HPRTPrinterHelper;

import static com.yunlinker.auth.WebPermissionManager.StoragePermissions;
import static com.yunlinker.baseclass.BaseActivity.handlerprint;
import static com.yunlinker.config.WebConfig.saveKey;
import static com.yunlinker.printting.Constant.CONN_STATE_DISCONN;

/**
 * Created by lemon on 2017/7/27.
 */

public class JSInspect extends BaseInspect {
    public  int counts;
    public  int printcount=0;
    private ThreadPool threadPool;
    public boolean continuityprint=false;
    public int id = 0;
    private String ConnectType="";
    private static HPRTPrinterHelper HPRTPrinter=new HPRTPrinterHelper();
    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;


    public JSInspect(BaseWebView w, BaseActivity a) {
        super(w, a);
    }


    //注册推送
    @JavascriptInterface
    public void registerPush(final String obj) {
        int state = 0;
        String devicetk = "none";
        String tempTk = null;
        String url = null;
        int clientType = 0;
        if(obj != null) {
            try {
                JSONObject jb = new JSONObject(obj);
                mweb.setInsCode("registerPush",jb.getString("code"));
                state = jb.getInt("state");
                tempTk = jb.getString("token");
                url = jb.getString("url");
                clientType = jb.getInt("clientType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences sp = mactivity.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
        if(state==1 || obj == null)
            devicetk = sp.getString("deviceToken", "");
        if(tempTk!=null && url!=null) {
            HashMap<String,String> tempParam = new HashMap<>();
            tempParam.put("devicetype",tempTk.length()>0?"1":"0");
            tempParam.put("devicetoken",devicetk);
            tempParam.put("_token",tempTk);
            tempParam.put("_device","2");
            tempParam.put("clientType",String.valueOf(clientType));
            final String furl = url;
            final HashMap<String,String> fparam = tempParam;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpManager.POST(furl, fparam);
                }
            }).start();
        }
        if(obj!=null)
            mweb.setValue("registerPush", "1");
    }
    //设置推送
    @JavascriptInterface
    public void setPush(final String obj) {
        String act = "";
        String open = "";
        try {
            JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("setPush",jb.getString("code"));
            act = jb.getString("act");
            open = jb.getString("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final SharedPreferences sp = mactivity.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
        if(act.equals("get")) {
            String closeStr = sp.getString("closePush", "");
            mweb.setValue("setPush",(TextUtils.equals(closeStr,"1")?"0":"1"));
        } else if(act.equals("set")) {
            final String openStr = open;
            PushAgent push = PushAgent.getInstance(mactivity);
            IUmengCallback pushCb = new IUmengCallback() {
                @Override
                public void onSuccess() {
                    SharedPreferences.Editor editor = sp.edit();
                    String res = openStr.equals("1")?"0":"1";
                    editor.putString("closePush", res);
                    editor.apply();
                    mweb.setValue("setPush",openStr);
                }
                @Override
                public void onFailure(String s, String s1) {
                    mweb.setValue("setPush","0");
                }
            };
            if(Integer.parseInt(open)==1) {
                push.enable(pushCb);
            } else {
                push.disable(pushCb);
            }
        }
    }
    //支付
    private OnlinePay payObj;
    public OnlinePay getPay() {
        return payObj;
    }
    @JavascriptInterface
    public void pay(String obj) {
        try{
            JSONObject jb = new JSONObject(obj);
            mweb.setInsCode("pay",jb.getString("code"));
            if(payObj==null) {
                payObj = new OnlinePay();
                payObj.payResult = new OnlinePay.PayResult() {
                    @Override
                    public void success() {
                        mweb.setValue("pay","{\"code\":1}");
                    }
                    @Override
                    public void error(String msg) {
                        mweb.setValue("pay","{\"code\":0,\"msg\":\""+msg+"\"}");
                    }
                };
            }
            payObj.startPay(jb, mactivity);
        }catch(Exception e){}
    }

    //获取位置信息
    @JavascriptInterface
    public void position(String code) {
        mweb.setInsCode("position",code);
        WebPermissionManager.getInstance(mactivity).CheckPermission(WebPermissionManager.LocationPermissions, new WebPermissionManager.OnPermissionBack() {
            @Override
            public void success() {
                Location.getInstance().position(mactivity, mweb);
            }

            @Override
            public void error() {
                mweb.setValue("position", "{\"code\":0}");
                Toast.makeText(mactivity,"授权失败，请设置权限后重试",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //分享
    @JavascriptInterface
    public void shareUrl(String obj) {
         JSONObject jb = null;
        try{
            jb = new JSONObject(obj);
            mweb.setInsCode("shareUrl",jb.getString("code"));
        }catch(Exception e){}
        final JSONObject fjb = jb;
        WebPermissionManager.getInstance(mactivity).CheckPermission(StoragePermissions, new WebPermissionManager.OnPermissionBack() {
            @Override
            public void success() {
                startShare(fjb);
            }

            @Override
            public void error() {
                Toast.makeText(mactivity.getApplicationContext(), "获取权限失败，请检查权限后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private UMShareListener umShareListener;
    private void startShare(JSONObject jb) {
        if(umShareListener==null) {
            umShareListener = new UMShareListener() {
                @Override
                public void onStart(SHARE_MEDIA share_media) {

                }

                @Override
                public void onResult(SHARE_MEDIA share_media) {
                    mweb.setValue("shareUrl","{\"code\":1,\"msg\": \"分享成功\"}");
                }

                @Override
                public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                    mweb.setValue("shareUrl","{\"code\":0,\"msg\": \"分享失败\"}");
                }

                @Override
                public void onCancel(SHARE_MEDIA share_media) {
                    mweb.setValue("shareUrl","{\"code\":0,\"msg\": \"分享失败\"}");
                }
            };
        }
        if(jb != null) {
            String title = "";
            String pic = "";
            String url = "";
            String desc = "";
            String base64 = null;
            try {
                if(jb.has("base64"))
                    base64 = jb.getString("base64");
                Log.e("嘀嘀嘀嘀嘀嘀嘀嘀", "startShare: "+base64);
                title = jb.getString("title");
                Log.e("嘀嘀嘀嘀嘀嘀嘀嘀", "startShare: "+title);
                pic = jb.getString("pic");
                Log.e("嘀嘀嘀嘀嘀嘀嘀嘀", "startShare: "+pic);
                url = jb.getString("url");
                Log.e("嘀嘀嘀嘀嘀嘀嘀嘀", "startShare: "+url);
                desc = jb.getString("desc");
                Log.e("嘀嘀嘀嘀嘀嘀嘀嘀", "startShare: "+desc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(TextUtils.isEmpty(base64) && (TextUtils.isEmpty(title) || TextUtils.isEmpty(pic) || TextUtils.isEmpty(url) || TextUtils.isEmpty(desc))) {
                Toast.makeText(mactivity.getApplicationContext(), "分享异常，请稍后重试", Toast.LENGTH_SHORT).show();
                return;
            }
            UMImage umimg = null;
            UMWeb web = null;
            if(base64!=null) {
                byte[] bt = Base64.decode(base64, Base64.DEFAULT);
                umimg = new UMImage(mactivity, bt);
            } else {
                web = new UMWeb(url);
                web.setTitle(title);//标题
                UMImage thumb =  new UMImage(mactivity, pic);
                web.setThumb(thumb);  //缩略图
                web.setDescription(desc);//描述
            }
            int platform = 0;
            try {
                platform = Integer.parseInt(jb.getString("platform"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ShareAction sa = new ShareAction(mactivity);
            if(umimg!=null) sa.withMedia(umimg);
            else sa.withMedia(web);
            sa.setCallback(umShareListener);
            if(platform>0) {
                SHARE_MEDIA m = SHARE_MEDIA.WEIXIN;
                if(platform==2) {
                    m = SHARE_MEDIA.WEIXIN_CIRCLE;
                }
                sa.setPlatform(m);
                sa.share();
            } else {
                sa.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE);
                sa.open();
            }
        }
    }


    //打开外部浏览器
    @JavascriptInterface
    public void openUrlStr(String url){
        final Uri uri = Uri.parse(url);
        final Intent it = new Intent(Intent.ACTION_VIEW, uri);
        mactivity.startActivity(it);
    }

    private UMAuthListener authListener = null;
    @JavascriptInterface
    public void extLogin(final String obj) {
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        UMShareAPI.get(mactivity).setShareConfig(config);
        String type = "1";
        try {
            JSONObject jb = new JSONObject(obj);
            type = jb.getString("type");
            mweb.setInsCode("extLogin", jb.getString("code"));
        } catch (Exception e) {
        }
        final String platform = type;
        if (authListener == null) {
            authListener = new UMAuthListener() {
                @Override
                public void onStart(SHARE_MEDIA share_media) {

                }

                @Override
                public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                    JSONObject successJb = new JSONObject();
                    try {
                        String unionid = map.get("unionid");
                        if (unionid == null || unionid.length() <= 0)
                            unionid = map.get("openid");
                        successJb.put("unionid", unionid);
                        successJb.put("openid", map.get("openid"));
                        successJb.put("nickname", map.get("screen_name"));
                        successJb.put("sex", map.get("gender"));
                        successJb.put("face", map.get("profile_image_url"));
                        successJb.put("city", map.get("city"));
                        successJb.put("province", map.get("province"));
                        successJb.put("type", Integer.parseInt(platform));
                        successJb.put("code", "1");
//                        Log.e("hehehehheheheeheh", "extLogin: "+"unionid"+successJb.put("unionid", unionid)+"openid"+successJb.put("openid", map.get("openid"))
//                        +"nickname"+successJb.put("nickname", map.get("screen_name"))+"sex"+successJb.put("sex", map.get("gender"))+"face"+successJb.put("face", map.get("profile_image_url"))
//                        +"city"+successJb.put("city", map.get("city"))+"province"+successJb.put("province", map.get("province"))
//                                +"type"+successJb.put("type", Integer.parseInt(platform))+"code"+successJb.put("code", "1"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    UMShareAPI.get(mactivity).deleteOauth(mactivity, (Integer.parseInt(platform) == 2 ? SHARE_MEDIA.QQ : SHARE_MEDIA.WEIXIN), null);
                    if (successJb.has("unionid")) {
                        mweb.setValue("extLogin", successJb.toString());
                    } else {
                        mweb.setValue("extLogin", "{\"code\":0,\"msg\": \"获取用户登录信息失败\"}");
                    }
                }

                @Override
                public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                    mweb.setValue("extLogin", "{\"code\":0,\"msg\": \"第三方登录失败\"}");
                }

                @Override
                public void onCancel(SHARE_MEDIA share_media, int i) {
                    mweb.setValue("extLogin", "{\"code\":0,\"msg\": \"用户取消登录\"}");
                }
            };
        }
       UMShareAPI.get(mactivity).getPlatformInfo(mactivity, Integer.parseInt(platform) == 2 ? SHARE_MEDIA.QQ : SHARE_MEDIA.WEIXIN, authListener);

    }


    @JavascriptInterface
    public void qiyu(String objStr){
        try {
            JSONObject jb = new JSONObject(objStr);
            if(!jb.has("login") || jb.getInt("login")==0) {
                //注销用户
                Unicorn.logout();
            } else {
                //打开客服窗口
                //如果用户存在展示用户信息
                if(jb.has("user")) {
                    JSONObject user =  jb.getJSONObject("user");
                    YSFUserInfo userInfo = new YSFUserInfo();
                    userInfo.userId = user.getString("userId");
                    userInfo.authToken = "";
                    userInfo.data="[\n" +
                            "    {\"key\":\"real_name\", \"value\":\""+user.getString("real_name")+"\"},\n" +
                            "    {\"key\":\"mobile_phone\", \"value\":\"" + user.getString("mobile_phone") + "\"},\n" +
                            "    {\"key\":\"avatar\", \"value\": \"" + user.getString("avatar") + "\"},\n" +
                            "]";
                    YSFOptions o = myApp.options();
                    o.uiCustomization.rightAvatar = user.getString("avatar");
                    Unicorn.updateOptions(o);
                    Unicorn.setUserInfo(userInfo);
                }

                ConsultSource source = new ConsultSource("", "", "");

                if(jb.has("goods")) {
                    JSONObject goods =  jb.getJSONObject("goods");
                    ProductDetail.Builder gb =  new ProductDetail.Builder();
                    gb.setTitle(goods.getString("title"));
                    gb.setPicture(goods.getString("picture"));
                    gb.setDesc(goods.getString("desc"));
                    gb.setNote(goods.getString("note"));
                    gb.setAlwaysSend(true);
                    source.productDetail = gb.create();
                }

                Unicorn.openServiceActivity(mactivity, "皮革客服", source);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private ProgressDialog progressDialog;
    private List<String> downImgs = new ArrayList<>();
    @JavascriptInterface
    public void saveImg(final String obj){
        WebPermissionManager.getInstance(mactivity).CheckPermission(StoragePermissions, new WebPermissionManager.OnPermissionBack() {
            @Override
            public void success() {
                JSONObject jb = null;
                String urls = null;
                try{
                    jb = new JSONObject(obj);
                    mweb.setInsCode("saveImg",jb.getString("code"));
                    urls = jb.getString("urls");
                    String[] ds = urls.split(",");
                    for (String downImg : ds) {
                        downImgs.add(downImg);
                    }
                }catch(Exception e){}
                mactivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(mactivity);
                        progressDialog.setMessage("正在下载图片中...");
                        progressDialog.show();
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getImgtoLocal();
                    }
                }).start();
            }

            @Override
            public void error() {
                mweb.setValue("saveImg","{\"code\":0,\"msg\": \"存储权限读取失败\"}");
            }
        });
    }

    public void getImgtoLocal() {
        if(downImgs.size()>0) {
            String url = downImgs.remove(downImgs.size()-1);
            URL imgUrl = null;
            HttpURLConnection connection=null;
            Bitmap bitmap=null;
            try {
                imgUrl = new URL(url);
                connection = (HttpURLConnection)imgUrl.openConnection();
                connection.setConnectTimeout(6000); //超时设置
                connection.setDoInput(true);
                connection.setUseCaches(false); //设置不使用缓存
                InputStream inputStream=connection.getInputStream();
                bitmap= BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(bitmap!=null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                if(bitmap!=null) {
                    ImageTool.saveImageToGallery(mactivity, bitmap);
                }
            }
            getImgtoLocal();
        } else {
            mactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
            mweb.setValue("saveImg","{\"code\":1,\"msg\": \"保存完成\"}");
        }
    }






    @JavascriptInterface
    public void copyText(String text){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) mactivity.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        Toast.makeText(mactivity.getApplicationContext(), "复制成功",
                Toast.LENGTH_SHORT).show();
    }


    @JavascriptInterface
    public void getAddressBook(String code) {
        mweb.setInsCode("getAddressBook",code);
        AddressBook.startGetList(mactivity, mweb);
    }

    @SuppressLint("NewApi")
    @JavascriptInterface
    public void bluetoothPrint (String code){
        try {
            JSONObject jb = new JSONObject(code);
            int type = jb.getInt("type");
            mweb.setInsCode("bluetoothPrint",jb.getString("code"));
            if (type==1){
                WifiParameterConfigDialog wifiParameterConfigDialog = new WifiParameterConfigDialog(mactivity, handlerprint);
                wifiParameterConfigDialog.show();
            }
            if (type==2){
                if(HPRTPrinter!=null) {
                    HPRTPrinterHelper.PortClose();
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    //校验是否已具有模糊定位权限
                    if (ContextCompat.checkSelfPermission(mactivity,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(mactivity,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.BLUETOOTH,
                                android.Manifest.permission.BLUETOOTH_ADMIN},
                                100);
                    } else {
                        //具有权限
                        ConnectType="Bluetooth";
                        Intent serverIntent = new Intent(mactivity,Activity_DeviceList.class);
                        mactivity.startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
                        return;
                    }
                } else {
                    //系统不高于6.0直接执行
                    ConnectType="Bluetooth";
                    Intent serverIntent = new Intent(mactivity,Activity_DeviceList.class);
                    mactivity.startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
//             connectBluetooth();
                }
                return;
            }
            if (type==3){
                final String data = jb.getString("data");
//                JSONObject obj = new JSONObject(data);
//                final String big = obj.getString("big");
//                final String small = obj.getString("small");
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                    Utils.toast(mactivity, mactivity.getString(R.string.str_cann_printer));
                    mweb.setValue("bluetoothPrint","0");
                    return;
                }

                threadPool = ThreadPool.getInstantiation();
                threadPool.addTask(new Runnable() {
                    @Override
                    public void run() {
                        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
//                            counts = 2 ;
//                            printcount=0;
//                            continuityprint=true;
                            sendContinuityPrint(data);
//                            Log.e("1111111111", "run: "+data);
//                            SharedPreferences sp = mactivity.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sp.edit();
//                            editor.putString("big",data);
//                            editor.commit();
//                            Log.e("111111111", "run: "+data);
                            mweb.setValue("bluetoothPrint","1");
                        } else {
                            handlerprint.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                            mweb.setValue("bluetoothPrint","0");
                        }
                    }
                });
            }

            if (type==4){
                final String data = jb.getString("data");
                if(!HPRTPrinterHelper.IsOpened())
                {
                    Toast.makeText(mactivity, "请连接面单打印机", Toast.LENGTH_SHORT).show();
                    mweb.setValue("bluetoothPrint","0");
                    return;
                }
                printmodel(data);
            }

            if (type==5){
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                    Utils.toast(mactivity, mactivity.getString(R.string.str_cann_printer));
                    return;
                }
                mactivity.handlerprint.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
            }


        }catch (Exception e){
            Log.e("eeeee", "bluetoothPrint: "+e);
        }
    }


    /**
     * 发送票据
     */

    public void sendReceiptWithResponse(String bean) {
        try{
            EscCommand esc = new EscCommand();
            esc.addInitializePrinter();

            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);

            byte[] srtbyte = Base64.decode(bean,Base64.NO_WRAP);
            Bitmap bitmap = BitmapFactory.decodeByteArray(srtbyte,0,srtbyte.length);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            esc.addRastBitImage(bitmap,width*4/5, 0);
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addCutPaper();

            // 加入查询打印机状态，用于连续打印
            byte[] bytes={29,114,1};
            esc.addUserCommand(bytes);
            Vector<Byte> datas = esc.getCommand();
            // 发送数据
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
            mweb.setValue("bluetoothPrint","1");
            Timer timer =new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                            !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                        Utils.toast(mactivity, mactivity.getString(R.string.str_cann_printer));
                        return;
                    }
                    mactivity.handlerprint.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                }
            },120000);

        }catch (Exception e){
            mweb.setValue("bluetoothPrint","0");
            Log.e("1111111111111", "sendReceiptWithResponse: "+e);
        }
    }

    public List<String> getStringstwo(String a){
        List<String> strings = new ArrayList<>();
        int xlenth = 15;
        if (a.length()>xlenth){
            int size = a.length()/xlenth+1;
            for (int i=0;i<size;i++){
                if (i==size-1){
                    strings.add(a.substring(i*xlenth,(a.length()-(i*xlenth))+(i*xlenth)));
                }else{
                    strings.add(a.substring(i*xlenth,xlenth+(i*xlenth)));
                }

            }
        }else{
            strings.add(a);
        }
        return strings;
    }
    public void sendContinuityPrint(final String data) {
        ThreadPool.getInstantiation().addTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null
                        && DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                    ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder("MainActivity_sendContinuity_Timer");
                    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, threadFactoryBuilder);
                    scheduledExecutorService.schedule(threadFactoryBuilder.newThread(new Runnable() {
                        @Override
                        public void run() {
                            counts--;
                            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
                                sendReceiptWithResponse(data);
                            }

                        }
                    }), 1000, TimeUnit.MILLISECONDS);
                }
            }
        });


    }

    public void printmodel(String small) {
        // TODO Auto-generated method stub
        try {

            Log.e("111111", "printmodel: "+small);
            byte[] srtbyte = Base64.decode(small,Base64.NO_WRAP);
            HPRTPrinterHelper.printAreaSize("80","130");
            Bitmap bmp=BitmapFactory.decodeByteArray(srtbyte,0,srtbyte.length);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            // 设置想要的大小
            int newWidth = 600;
            int newHeight = 900;
            // 计算缩放比例
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
               //取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            bmp = Bitmap.createBitmap(bmp,0,0,width,height,matrix,true);
            HPRTPrinterHelper.Density("15");
            HPRTPrinterHelper.printImage("10","10",bmp,true);

            HPRTPrinterHelper.Print("1", "1");
            HPRTPrinterHelper.CLS();
            HPRTPrinterHelper.Cut();
            mweb.setValue("bluetoothPrint","1");
            Timer timer =new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!checkClick.isClickEvent()) {
                        return;
                    }
                    if(HPRTPrinter!=null)
                    {
                        try {
                            HPRTPrinterHelper.PortClose();
                            Toast.makeText(mactivity, "请连接打印机", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            },120000);
        }
        catch(Exception e)
        {
            mweb.setValue("bluetoothPrint","0");
            Toast.makeText(mactivity,  (new StringBuilder("Activity_Main --> printmodel ")).append(e.getMessage()).toString(), Toast.LENGTH_LONG).show();
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> printmodel ")).append(e.getMessage()).toString());
        }
    }



}