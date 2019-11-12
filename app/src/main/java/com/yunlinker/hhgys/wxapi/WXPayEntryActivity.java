package com.yunlinker.hhgys.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yunlinker.baseclass.BaseActivity;
import com.yunlinker.manager.ActivityManager;
import com.yunlinker.pay.OnlinePay;
import com.yunlinker.hhgys.JSInspect;

import static com.yunlinker.config.WebConfig.WXID;

/**
 * Created by Lemon on 2017/12/15.
 */

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WXID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.e("---------->","code:"+baseResp.errCode);
        if(baseResp.getType()== ConstantsAPI.COMMAND_PAY_BY_WX){
            BaseActivity ba = ActivityManager.getInstance().getTop();
            if(ba!=null) {
                JSInspect inp = ba.jsInp();
                if(inp!=null) {
                    OnlinePay p = inp.getPay();
                    if(p!=null) {
                        if(baseResp.errCode==0) {
                            //支付成功
                            p.payResult.success();
                        } else if(baseResp.errCode==-1) {
                            //支付失败
                            p.payResult.error("微信支付失败");
                        } else if(baseResp.errCode==-2) {
                            //用户取消
                            p.payResult.error("用户取消支付");
                        }
                    }
                }
            }
        }
        finish();
    }
}
