package com.yunlinker.upimage.aliupload;

import android.system.Os;
import android.util.Log;

import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.google.gson.Gson;
import com.lzy.imagepicker.bean.ImageFolder;
import com.yunlinker.model.OSStokensingmodel;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
/**
 * Created by Administrator on 2015/12/9 0009.
 * 重载OSSFederationCredentialProvider生成自己的获取STS的功能
 */
public class STSGetter extends OSSFederationCredentialProvider {

    private String stsServer = "http://jke5.com/swzh/api/imgupload/app/getUploadToken";
    private OSStokensingmodel  osstoken;
    public STSGetter(String stsServer) {
        this.stsServer = stsServer;
    }

    public OSSFederationToken getFederationToken() {
        OSSFederationToken ross = null;
        String stsJson;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(stsServer).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                stsJson = response.body().string();
//                JSONObject jsonObjs = new JSONObject(stsJson);
//                String ak = jsonObjs.getString("AccessKeyId");
//                Log.e("ddddddddddd", "getFederationToken: "+ak);
//                String sk = jsonObjs.getString("AccessKeySecret");
//                Log.e("ddddddddddd", "getFederationToken: "+sk);
//                String token = jsonObjs.getString("SecurityToken");
//                Log.e("ddddddddddd", "getFederationToken: "+token);
//                String expiration = jsonObjs.getString("Expiration");
//                Log.e("ddddddddddd", "getFederationToken: "+expiration);
                Gson gson = new Gson();
                osstoken = gson.fromJson(stsJson, OSStokensingmodel.class);
                if (osstoken.getData()!=null){
                    String ak = osstoken.getData().getAccessKeyId();
                    String sk = osstoken.getData().getAccessKeySecret();
                    String token = osstoken.getData().getSecurityToken();
                    String expiration = osstoken.getData().getExpiration();
                    ross = new OSSFederationToken(ak, sk, token, expiration);
                }
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("GetSTSTokenFail", e.toString());
        }
        return ross;
    }

}
