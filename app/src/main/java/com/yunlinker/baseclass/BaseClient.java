package com.yunlinker.baseclass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yunlinker.hhgys.R;


/**
 * Created by lemon on 2017/7/30.
 */

public class BaseClient extends WebViewClient {

    private BaseWebView web;
    private BaseActivity activity;
    public BaseClient(BaseWebView mweb, BaseActivity a) {
        web = mweb;
        activity = a;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        web.setHasLoaded(false);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        web.setHasLoaded(true);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                web.setAlpha(1);
                if(TextUtils.equals("HomeActivity", activity.getLocalClassName()))
                    activity.setTheme(R.style.AppTheme);
            }
        }, 1000);
    }



    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse res = getResponse(view, url, null);
        return res;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        WebResourceResponse res = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            res = getResponse(view, request.getUrl().toString(), request);
        }
        return res;
    }


    private WebResourceResponse getResponse(WebView view, String url, WebResourceRequest request) {
        if(!TextUtils.isEmpty(url)) {
            if(url.contains(".png") || url.contains(".jpeg")) {
                if (request!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return super.shouldInterceptRequest(view, request);
                } else
                    return super.shouldInterceptRequest(view, url);
            }
        }
        return null;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return overrideUrlLoading(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return overrideUrlLoading(view, request.getUrl().toString());
        }
        return true;
    }

    private boolean overrideUrlLoading(WebView view, final String url) {
        if (url.startsWith("tel:")){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("电话："+url.replaceAll("tel:","")+"，是否拨打电话？");
                    builder.setCancelable(false);
                    builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(url));
                            activity.startActivity(intent);
                        }
                    });
                    builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    activity.dialog = builder.create();
                    activity.dialog.show();
                }
            });
        }else{
            if(url.contains("_ifr"))
                return  false;
            else if(!TextUtils.isEmpty(url) && url.contains("asset"))
                view.loadUrl(url);
        }
        return true;
    }
}
