package com.yunlinker.baseclass;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by lemon on 2017/7/26.
 */

public class BaseWebView extends WebView {

    protected Map<String,Stack<String>> insCodeMap;

    private Boolean hasLoaded = false;
    public void setHasLoaded(Boolean hasLoaded) {
        this.hasLoaded = hasLoaded;
    }

    public BaseWebView(Context context) {
        super(context);
        insCodeMap = new HashMap<String, Stack<String>>();
    }


    //设置code
    public void setInsCode(String method, String code) {
        if(!insCodeMap.containsKey(method)) {
            insCodeMap.put(method, new Stack<String>());
        }
        insCodeMap.get(method).push(code);
    }
    //获取code
    public String getInsCode(String method) {
        if (insCodeMap.get(method)==null || insCodeMap.get(method).empty()) {
            return "";
        } else {
            return insCodeMap.get(method).pop();
        }
    }

    public void resumeWeb() {
        if(hasLoaded)
            setValue("","resume");
    }

    //写入数据
    public void setValue(final String method, String tempData) {
        if(!TextUtils.isEmpty(tempData)) {
            if(tempData.contains("'"))
                tempData = tempData.replaceAll("\'","\\\\'");
            if(tempData.contains("\\\""))
                tempData = tempData.replaceAll("\\\\\"","\\\\'");
        }
        final String resultString = tempData;
        post(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(method) || TextUtils.equals(method,"0")) {
                    loadUrl("javascript:_w9_wcallback('" + resultString + "','0')");
                } else {
                    loadUrl("javascript:_w9_wcallback('" + resultString + "','"+getInsCode(method)+"')");
                }
            }
        });
    }


}
