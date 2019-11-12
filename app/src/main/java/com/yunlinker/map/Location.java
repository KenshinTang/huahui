package com.yunlinker.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yunlinker.baseclass.BaseWebView;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static com.yunlinker.config.WebConfig.GPS_REQUEST_CODE;

/**
 * Created by lemon on 2017/9/11.
 */

public class Location {

    private static Location instance = null;
    public static Location getInstance() {
        synchronized (Location.class) {
            if (instance == null) {
                instance = new Location();
            }
        }
        return instance;
    }

    public interface LocBack{
        public void getSuccess(JSONObject jb);
    }
    public LocBack locationEvent;

    //获取位置信息
    private LocationClient mLocationClient = null;
    private BDAbstractLocationListener myListener = null;
    private LocationClientOption option = null;
    private Timer locTimer = null;
    private BaseWebView mbsw = null;


    public void position(final Activity ac, final BaseWebView bsw) {
        mbsw = bsw;
        Location.getInstance().locationEvent = new Location.LocBack() {
            @Override
            public void getSuccess(JSONObject jb) {
                if(jb.has("lat") && jb.has("lng")) {
                    locTimer.cancel();
                    mLocationClient.stop();
                    bsw.setValue("position", jb.toString());
                }
            }
        };
        locTimer = new Timer();
        locTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mbsw.setValue("position", "{code:0,msg:\"获取定位失败\"}");
                mLocationClient.stop();
                showGPSSetting(ac);
            }
        },5000);
        //声明LocationClient类
        if(mLocationClient == null)
            mLocationClient = new LocationClient(ac.getApplicationContext());
        initLocation();

        //注册监听函数
        if(myListener == null)
            myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
    }


    private void showGPSSetting(final Activity ac) {
        if(!checkGPSIsOpen(ac)) {
            ac.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(ac)
                            .setTitle("提示")
                            .setMessage("当前应用需要打开GPS功能")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //跳转GPS设置界面
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            ac.startActivityForResult(intent, GPS_REQUEST_CODE);
                                        }
                                    })
                            .setCancelable(false)
                            .show();
                }
            });
        }
    }


    private boolean checkGPSIsOpen(Activity a) {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) a
                .getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    private void initLocation(){
        if(option==null)
            option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setWifiCacheTimeOut(5*60*1000);
        //可选，7.2版本新增能力，如果您设置了这个接口，首次启动定位时，会先判断当前WiFi是否超出有效期，超出有效期的话，会先重新扫描WiFi，然后再定位
        mLocationClient.setLocOption(option);
    }

}
