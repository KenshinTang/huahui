package com.yunlinker.pay;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yunlinker.baseclass.BaseActivity;
import com.yunlinker.manager.HttpManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by lemon on 2017/9/21.
 */

public class OnlinePay {

    public interface PayResult {
        void success();
        void error(String msg);
    }
    public PayResult payResult;


    private ProgressDialog pg;

    public void startPay(JSONObject obj, final Activity a) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pg = ProgressDialog.show(a, "提示", "正在获取支付信息");
            }
        });
        String type = "";
        String url = "";
        StringBuilder sb = new StringBuilder();
        Map map = com.alibaba.fastjson.JSONObject.parseObject(obj.toString(), Map.class);
        Iterator entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if(key.equals("url")){
                url = value;
            }else{
                if(sb.length()>0){//表示前面有参数
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            if(key.equals("type")){
                type = value;
            }
        }

        Log.e("2222222222", "startPay: "+url + "?"+sb.toString());
        String errMsg = "网络异常，支付失败，请稍后重试";
        if(!TextUtils.isEmpty(type)||!TextUtils.isEmpty(url)) {
            String resStr = HttpManager.GET(url + "?"+sb.toString());
            Log.e("支付宝成功没有", "startPay: "+resStr);
            try {
                JSONObject res = new JSONObject(resStr);
                if(res.has("code")) {
                    if(res.getInt("code") == 1) {
                        if(TextUtils.equals(type,"1"))
                            alipay(res.getString("data"), a);
                        else if(TextUtils.equals(type,"3"))
                            wechatpay(res.getString("data"), a);
                        return;
                    } else {
                        errMsg = res.getString("msg");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else
            errMsg = "支付参数错误，支付失败，请稍后重试";
        if(payResult!=null) {
            if(pg!=null) {
                pg.dismiss();
                pg=null;
            }
            payResult.error(errMsg);
        }
    }


    private void alipay(final String order, final Activity a) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(a);
                Map<String, String> result = alipay.payV2(order,true);
                String rs = result.get("resultStatus");
                String errMsg = "支付失败，请稍后重试";
                if(TextUtils.equals(rs, "6001")) {
                    errMsg = "用户取消支付";
                } else if(TextUtils.equals(rs, "9000")) {
                    errMsg = null;
                }
                if(payResult!=null) {
                    if(errMsg!=null)
                        payResult.error(errMsg);
                    else
                        payResult.success();
                }
            }
        }).start();
        PayTask alipay = new PayTask(a);
        if(pg!=null) {
            pg.dismiss();
            pg=null;
        }
    }

    IWXAPI msgApi = null;

    private void wechatpay(final String order, Activity a) {
        if(pg!=null) {
            pg.dismiss();
            pg=null;
        }
        JSONObject o = null;
        try {
            o = new JSONObject(order);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(o!=null &&
                o.has("package") &&
                o.has("appid") &&
                o.has("sign") &&
                o.has("partnerid") &&
                o.has("prepayid") &&
                o.has("timestamp") &&
                o.has("noncestr")) {
            PayReq request = new PayReq();
            try {
                if(msgApi==null) {
                    msgApi = WXAPIFactory.createWXAPI(a, null);
                    msgApi.registerApp(o.getString("appid"));
                }
                request.appId = o.getString("appid");
                request.partnerId = o.getString("partnerid");
                request.prepayId= o.getString("prepayid");
                request.packageValue = o.getString("package");
                request.nonceStr= o.getString("noncestr");
                request.timeStamp= o.getString("timestamp");
                request.sign= o.getString("sign");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msgApi.sendReq(request);
        } else {
            if(payResult!=null)
                payResult.error("微信参数获取失败，支付失败");
        }
    }
}
