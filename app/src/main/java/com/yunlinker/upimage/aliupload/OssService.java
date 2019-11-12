package com.yunlinker.upimage.aliupload;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.yunlinker.upimage.UpImger;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


public class OssService {

    private OSS oss;
    private String bucket;
    private ArrayList<Map> uploadList;


    public OssService(OSS oss, String bucket) {
        this.oss = oss;
        this.bucket = bucket;
    }

    public void configUploadList(ArrayList<Map> list) {
        successList = new ArrayList<>();
        uploadList = list;
    }

    private ArrayList<String> successList;
    public void uploadImgs() {
        if(uploadList.size()>0) {
            String object = (String)uploadList.get(0).get("name");
            String path = (String)uploadList.get(0).get("path");
            asyncUploadImage(object, path);
            uploadList.remove(0);
        } else {
            //全部上传完成
            UpImger.getInstance().uploadEvent.finished(successList);
        }
    }

    private void asyncUploadImage(final String object, String localFile) {
        if (object.equals("")) {
            Log.w("AsyncPutImage", "ObjectNull");
            return;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            Log.w("AsyncPutImage", "FileNotExist");
            Log.w("LocalFile", localFile);
            return;
        }


        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucket, object, localFile);

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                successList.add(object);
                uploadImgs();
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                uploadImgs();
            }
        });
    }

}
