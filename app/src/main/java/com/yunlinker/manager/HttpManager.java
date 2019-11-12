package com.yunlinker.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lemon on 2017/9/21.
 */

public class HttpManager {

    private static HttpManager instance = null;
    private OkHttpClient client;

    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (HttpManager.class) {
                if (instance == null) {
                    instance = new HttpManager();
                    instance.client = new OkHttpClient();
                }
            }
        }
        return instance;
    }



    private String request(Boolean post, String url, HashMap<String,String> data) throws IOException {
        Request request = null;
        if(post) {
            FormBody.Builder builder = new FormBody.Builder();
            Iterator iter = data.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String)entry.getKey();
                String val = (String)entry.getValue();
                builder.add(key, val);
            }
            RequestBody body = builder.build();
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .build();
        }
        if(request!=null) {
            Response response = client.newCall(request).execute();
            if(response.body()!=null)
                return response.body().string();
        }
        return "{}";
    }

    public static String POST(String url, HashMap<String,String> data) {
        try {
            return HttpManager.getInstance().request(true, url, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public static String GET(String url) {
        try {
            return HttpManager.getInstance().request(false, url, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{}";
    }

}
