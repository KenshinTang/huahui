package com.yunlinker.baseclass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.liulishuo.filedownloader.FileDownloader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMShareAPI;
import com.yunlinker.auth.WebPermissionManager;
import com.yunlinker.hhgys.MainActivity;
import com.yunlinker.hhgys.R;
import com.yunlinker.manager.ActivityResult;

import com.yunlinker.hhgys.JSInspect;
import com.yunlinker.myApp;
import com.yunlinker.printting.DeviceConnFactoryManager;
import com.yunlinker.printting.ThreadPool;
import com.yunlinker.printting.Utils;
import com.yunlinker.util.NotificationsUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import HPRTAndroidSDKTSPL.HPRTPrinterHelper;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.yunlinker.config.WebConfig.AssestRoot;
import static com.yunlinker.config.WebConfig.UMSHARE_CALLBACK_CODE;
import static com.yunlinker.config.WebConfig.saveKey;
import static com.yunlinker.myApp.getContext;
import static com.yunlinker.printting.Constant.CONN_STATE_DISCONN;
import static com.yunlinker.printting.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.yunlinker.printting.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.yunlinker.printting.DeviceConnFactoryManager.CONN_STATE_FAILED;


/**
 * Created by lemon on 2017/7/26.
 */

public class BaseActivity extends Activity implements Handler.Callback {
    private static final int REQUEST_CODE_SCAN = 0x0000;
    public static  String tempcode;

    public String loadUrl;

    protected BaseWebView mwebView;
    private ImageView splash;
    public BaseWebView getWebView() {
        return mwebView;
    }

  //  public RelativeLayout rootlay;
    public LinearLayout rootlay;

    private Handler handler,handler1;
    public static final int openNewDetali=100;
    public static Handler handlerprint;
    private static final String ACTION_USB_PERMISSION = "com.HPRTSDKSample";
    Context mContext;
    private ThreadPool threadPool;
    private int id = 0;
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private Context thisCon=null;
    private ProgressDialog pdialog;
    private static final int CONN_MOST_DEVICES = 0x11;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        //FullScreen.fullScreen(this);
        rootlay = new LinearLayout(this);
        setContentView(rootlay);
        addWebView();
        FileDownloader.setup(this);
        //推送消息点击跳转的handler
        handler1 = new Handler(this);
        myApp.setMainhandler(handler1);

        try {
            thisCon=this.getApplicationContext();
            //EnableBluetooth();
            handlerprint = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    // TODO Auto-generated method stub
                    super.handleMessage(msg);
//                    if (msg.what==1) {
//                        Toast.makeText(thisCon, "succeed", Toast.LENGTH_SHORT).show();
//                        dialog.cancel();
//                    }else {
//                        Toast.makeText(thisCon, "failure",Toast.LENGTH_SHORT).show();
//                        dialog.cancel();
//                    }
                    switch (msg.what){
                        case CONN_STATE_DISCONN:
                            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[inp.id] != null||!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[inp.id].getConnState()) {
                                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[inp.id].closePort(inp.id);
                                Utils.toast(BaseActivity.this,getString(R.string.str_disconnect_success));
                            }
                            break;
                        case MESSAGE_UPDATE_PARAMETER:
                            String strIp = msg.getData().getString("Ip");
                            String strPort = msg.getData().getString("Port");
                            //初始化端口信息
                            new DeviceConnFactoryManager.Build()
                                    //设置端口连接方式
                                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                                    //设置端口IP地址
                                    .setIp(strIp)
                                    //设置端口ID（主要用于连接多设备）
                                    .setId(id)
                                    //设置连接的热点端口号
                                    .setPort(Integer.parseInt(strPort))
                                    .build();
                            threadPool = ThreadPool.getInstantiation();
                            threadPool.addTask(new Runnable() {
                                @Override
                                public void run() {
                                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                                }
                            });
                            break;
                        default:
                            new DeviceConnFactoryManager.Build()
                                    //设置端口连接方式
                                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                                    //设置端口IP地址
                                    .setIp("192.168.0.142")
                                    //设置端口ID（主要用于连接多设备）
                                    .setId(id)
                                    //设置连接的热点端口号
                                    .setPort(9100)
                                    .build();
                            threadPool.addTask(new Runnable() {
                                @Override
                                public void run() {
                                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                                }
                            });
                            break;
                        case PRINTER_COMMAND_ERROR:
                            Utils.toast(mContext, getString(R.string.str_choice_printer_command));
                            break;
                    }

                }
            };
        }catch (Exception e){

        }



        initdialog();

    }


    private void initdialog() {
        if (!NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
            new MaterialDialog.Builder(BaseActivity.this)
                    .title("请手动将通知打开")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.NEUTRAL) {
                                Log.e("onClick", "更多信息: ");
                            } else if (which == DialogAction.POSITIVE) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                    intent.putExtra("app_package", BaseActivity.this.getPackageName());
                                    intent.putExtra("app_uid", BaseActivity.this.getApplicationInfo().uid);
                                    startActivity(intent);
                                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + BaseActivity.this.getPackageName()));
                                    startActivity(intent);
                                } else {
                                    Intent localIntent = new Intent();
                                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    localIntent.setData(Uri.fromParts("package", BaseActivity.this.getPackageName(), null));
                                    startActivity(localIntent);
                                }
                                Log.e("onClick", "同意: ");
                            } else if (which == DialogAction.NEGATIVE) {
                                Log.e("onClick", "不同意: ");
                            }
                        }
                    }).show();
        }
    }




    private boolean EnableBluetooth()
    {
        boolean bRet = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null)
        {
            if(mBluetoothAdapter.isEnabled())
                return true;
            mBluetoothAdapter.enable();
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if(!mBluetoothAdapter.isEnabled())
            {
                bRet = true;
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK");
            }
        }
        else
        {
            Log.d("HPRTSDKSample", (new StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if(mwebView!=null)
            mwebView.resumeWeb();

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_QUERY_PRINTER_STATE);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(receiver, filter);
    }

    public AlertDialog dialog;
    @Override
    protected void onPause() {
        super.onPause();
        if(dialog!=null) dialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mwebView != null) {
            mwebView.destroy();
        }
        MobclickAgent.onPause(this);
//        DeviceConnFactoryManager.closeAllPort();
//        if (threadPool != null) {
//            threadPool.stopThreadPool();
//        }

    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==UMSHARE_CALLBACK_CODE) {
            UMShareAPI.get(this).onActivityResult(requestCode,resultCode,data);
        } else {
            ActivityResult.getInstance().resultBack(BaseActivity.this, requestCode, resultCode, data);
        }
        if(requestCode==REQUEST_CODE_SCAN&&resultCode==RESULT_OK) {
            if (data != null) {
                String result = data.getStringExtra("codedContent");
                mwebView.setInsCode("scanf", tempcode);
                mwebView.setValue("scanf", result);
            }
        }

        try
        {
            String strIsConnected;
            switch(resultCode)
            {
                case HPRTPrinterHelper.ACTIVITY_CONNECT_BT:
                    String strBTAddress="";
                    strIsConnected=data.getExtras().getString("is_connected");
                    if (strIsConnected.equals("NO"))
                    {
                        Toast.makeText(thisCon, "扫描失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else
                    {
                        Toast.makeText(thisCon, "扫描成功！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                case HPRTPrinterHelper.ACTIVITY_CONNECT_WIFI:
                    String strIPAddress="";
                    String strPort="";
                    strIsConnected=data.getExtras().getString("is_connected");
                    if (strIsConnected.equals("NO"))
                    {
                        Toast.makeText(thisCon, "扫描失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else
                    {
                        strIPAddress=data.getExtras().getString("IPAddress");
                        strPort=data.getExtras().getString("Port");
                        if(strIPAddress==null || !strIPAddress.contains("."))
                            return;
                        //HPRTPrinter=new HPRTPrinterHelper(thisCon,spnPrinterList.getSelectedItem().toString().trim());
                        if(HPRTPrinterHelper.PortOpen("WiFi,"+strIPAddress+","+strPort)!=0)
                            Toast.makeText(thisCon, "连接失败！", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(thisCon, "连接成功！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                case HPRTPrinterHelper.ACTIVITY_IMAGE_FILE:
//	  		    	PAct.LanguageEncode();
                    pdialog = new ProgressDialog(BaseActivity.this);
                    pdialog.setMessage("Printing.....");
                    pdialog.setProgress(100);
                    pdialog.show();
                    new Thread(){
                        public void run() {
                            try {
                                String strImageFile=data.getExtras().getString("FilePath");
                                Bitmap bmp= BitmapFactory.decodeFile(strImageFile);
                                int height = bmp.getHeight()/8;
                                HPRTPrinterHelper.printAreaSize("100", ""+height);
                                HPRTPrinterHelper.CLS();
                                int a= HPRTPrinterHelper.printImage("0","0",strImageFile,true);
                                HPRTPrinterHelper.Print("1", "1");
                                if (a>0) {
                                    handler.sendEmptyMessage(1);
                                }else {
                                    handler.sendEmptyMessage(0);
                                }
                            }catch (Exception e) {
                                handler.sendEmptyMessage(0);
                            }
                        }
                    }.start();
                    return;
                case HPRTPrinterHelper.ACTIVITY_PRNFILE:
                    String strPRNFile=data.getExtras().getString("FilePath");
                    HPRTPrinterHelper.PrintBinaryFile(strPRNFile);
                    return;

            }
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case CONN_MOST_DEVICES:
                        id = data.getIntExtra("id", -1);
                        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null &&
                                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                            Toast.makeText(thisCon, "连接状态：已连接"+"\n"+ getConnDeviceInfo(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(thisCon, "连接状态：未连接", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onActivityResult ")).append(e.getMessage()).toString());
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WebPermissionManager.getInstance(BaseActivity.this).authBack(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        mwebView.setValue("0", "back");
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean backV = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;
            case KeyEvent.KEYCODE_MENU:
                break;
            default:
                backV = super.onKeyDown(keyCode, event);
                break;
        }
        return  backV;
    }


    //添加webview
    private JSInspect inp = null;
    //获取inspect对象
    public JSInspect jsInp() {
        return inp;
    }
    //添加视图
    protected void addWebView() {

        mwebView = new BaseWebView(this);
        LinearLayout.LayoutParams webLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mwebView.setLayoutParams(webLayoutParams);
        rootlay.addView(mwebView);

        mwebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mwebView.setHorizontalScrollBarEnabled(false);//水平不显示
        mwebView.setVerticalScrollBarEnabled(false); //垂直不显示
        inp = new JSInspect(mwebView, BaseActivity.this);
        mwebView.addJavascriptInterface(inp, "WeiLai");
        mwebView.setWebViewClient(new BaseClient(mwebView, BaseActivity.this));

//        if(BaseTools.isApkInDebug(this))
            mwebView.setWebContentsDebuggingEnabled(true);

        WebSettings setting = mwebView.getSettings();
        setting.setJavaScriptEnabled(true);//支持js
        setting.setDefaultTextEncodingName("utf-8");
        setting.setDomStorageEnabled(true);
        setting.setAllowFileAccess(true);
        setting.setAllowContentAccess(true);
        setting.setAllowFileAccessFromFileURLs(true);
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);


        loadUrl = getIntent().getStringExtra("sendUrl");
        if (!TextUtils.isEmpty(loadUrl)) {
            if(!loadUrl.startsWith("http") || !loadUrl.startsWith("file"))
             loadUrl = AssestRoot+loadUrl;
            mwebView.loadUrl(loadUrl);
        } else {
            //首页
            mwebView.loadUrl(AssestRoot+"index.html");
        }

        setKeyBoard();
    }

    private int screenHeight=0;
    private void setKeyBoard() {
        //---------------安卓键盘处理-----------------
        mwebView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mwebView.getWindowVisibleDisplayFrame(r);
                final int visibleHeight = r.height();
                if(screenHeight==0) {
                    screenHeight = visibleHeight;
                } else {
                    if(visibleHeight>100 && screenHeight!=visibleHeight) {
                        //有可能为打开键盘
                        mwebView.post(new Runnable() {
                            @Override
                            public void run() {
                                float ratio = (screenHeight-visibleHeight)/(float)screenHeight;
                                mwebView.loadUrl("javascript:window.keyBoardEvent&&keyBoardEvent(1,"+ratio+");");
                            }
                        });
                    } else {
                        //有可能为关闭键盘
                        mwebView.post(new Runnable() {
                            @Override
                            public void run() {
                                mwebView.loadUrl("javascript:window.keyBoardEvent&&keyBoardEvent(0);");
                            }
                        });
                    }
                }
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //Usb连接断开、蓝牙连接断开广播
                case ACTION_USB_DEVICE_DETACHED:
                    handler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            if (id == deviceId) {
                                Toast.makeText(thisCon, "连接状态：未连接", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            Toast.makeText(thisCon, "连接状态：连接中", Toast.LENGTH_SHORT).show();
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            Toast.makeText(thisCon, "连接状态：已连接"+getConnDeviceInfo(), Toast.LENGTH_SHORT).show();
                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(thisCon, getString(R.string.str_conn_fail));
                            Toast.makeText(thisCon, "连接状态：未连接", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    break;
                case ACTION_QUERY_PRINTER_STATE:
                    if (inp.counts >=0) {
                        if(inp.continuityprint) {
                            inp.printcount++;
                            //Utils.toast(thisCon, getString(R.string.str_continuityprinter) + " " + printcount);
                        }
                        if(inp.counts!=0) {
                            SharedPreferences sp = getSharedPreferences(saveKey, Context.MODE_PRIVATE);
                            String big = sp.getString("big","");
                            inp.sendContinuityPrint(big);
                        }else {
                            inp.continuityprint=false;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };





    private String getConnDeviceInfo() {
        String str = "";
        DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
        if (deviceConnFactoryManager != null
                && deviceConnFactoryManager.getConnState()) {
            if ("USB".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "USB\n";
                str += "USB Name: " + deviceConnFactoryManager.usbDevice().getDeviceName();
            } else if ("WIFI".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "WIFI\n";
                str += "IP: " + deviceConnFactoryManager.getIp() + "\t";
                str += "Port: " + deviceConnFactoryManager.getPort();
            } else if ("BLUETOOTH".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "BLUETOOTH\n";
                str += "MacAddress: " + deviceConnFactoryManager.getMacAddress();
            } else if ("SERIAL_PORT".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "SERIAL_PORT\n";
                str += "Path: " + deviceConnFactoryManager.getSerialPortPath() + "\t";
                str += "Baudrate: " + deviceConnFactoryManager.getBaudrate();
            }
        }
        return str;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case openNewDetali:
                inp.go(msg.obj.toString());
                break;
        }
        return false;
    }



}
