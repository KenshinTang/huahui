package com.yunlinker.upimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.yunlinker.baseclass.BaseTools;
import com.yunlinker.baseclass.BaseWebView;
import com.yunlinker.upimage.aliupload.OssService;
import com.yunlinker.upimage.aliupload.STSGetter;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

import static com.yunlinker.config.WebConfig.SELECTED_IMAGE_CODE;
import static com.yunlinker.config.WebConfig.bucket;
import static com.yunlinker.config.WebConfig.endpoint;

/**
 * Created by lemon on 2017/8/21.
 */

public class UpImger {




    private static UpImger instance = null;
    public static UpImger getInstance() {
        synchronized (UpImger.class) {
            if (instance == null)
                instance = new UpImger();
        }
        return instance;
    }

    //-----------------------------------上传图片-----------------------------------
    private Activity ma;
    private BaseWebView bw;
    private int remain;
    private OssService ossService;
    private ProgressDialog progressDialog;

    public interface UploadEvent{
        public void finished(ArrayList<String> list);
    }
    public UploadEvent uploadEvent;
    public void upimgs(JSONObject jb, Activity a, BaseWebView w) {
        ma = a;
        bw = w;
        try {
            remain = jb.getInt("remain");
            ossService = initOSS(endpoint, bucket, jb.getString("url"));
            if(remain > 0) {
                ImagePicker.getInstance().setShowCamera(true);
                if(remain == 1) {
                    ImagePicker.getInstance().setMultiMode(false);
                    ImagePicker.getInstance().setCrop(false);        //允许裁剪（单选才有效）
                    ImagePicker.getInstance().setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
                    ImagePicker.getInstance().setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
                    ImagePicker.getInstance().setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
                    ImagePicker.getInstance().setSaveRectangle(true); //是否按矩形区域保存
                } else {
                    ImagePicker.getInstance().setMultiMode(true);
                    ImagePicker.getInstance().setSelectLimit(remain);
                }
                Intent intent = new Intent(ma, ImageGridActivity.class);
                ma.startActivityForResult(intent, SELECTED_IMAGE_CODE);
            } else {
                Toast.makeText(ma, "当前图片已达最大值",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private OssService initOSS( String endpoint, String bucket, String authUrl) {
        OSSCredentialProvider credentialProvider;
        credentialProvider = new STSGetter(authUrl);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(ma, endpoint, credentialProvider, conf);
        return new OssService(oss, bucket);
    }


    private ArrayList compressedList;
    private ArrayList<ImageItem> compressItems;

    public void initUploadImageItems(ArrayList<ImageItem> items) {
        compressItems = items;
        compressedList = new ArrayList();
        if(items.size()>0){
            progressDialog = new ProgressDialog(ma);
            progressDialog.setMessage("图片正在上传中...");
            progressDialog.show();
        }
        uploadEvent = new UploadEvent() {
            @Override
            public void finished(ArrayList<String> list) {
                progressDialog.dismiss();
                if(list.size()>0) {
                    String finalUrls = "";
                    for(String tmp:list){
                        finalUrls += (tmp + ",");
                    }
                    bw.setValue("upimg",finalUrls.substring(0,finalUrls.length()-1));
                    Toast.makeText(ma, "上传图片完成",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ma, "上传图片失败",Toast.LENGTH_SHORT).show();
                }

            }
        };

        compressImg();
    }


    public void compressImg() {
        if(compressItems.size()>0) {
            ImageItem item = compressItems.get(0);
            compressItems.remove(0);

            final File file = new File(item.path);
            Luban.get(ma).load(file).setMaxSize(500).setMaxHeight(1080).setMaxWidth(720)
                    .putGear(Luban.THIRD_GEAR)
                    .launch(new OnCompressListener() {
                        @Override
                        public void onStart() {}
                        @Override
                        public void onSuccess(File file) {
                            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                            String str = new SimpleDateFormat("yyyy_MM").format(curDate);
                            String random = BaseTools.createRandom(false,5);

                            Map<String, String> fileMap = new HashMap<>();
                            fileMap.put("name",str+"/a"+random+file.getName());
                            fileMap.put("path",file.getAbsolutePath());
                            compressedList.add(fileMap);

                            compressImg();
                        }
                        @Override
                        public void onError(Throwable e) {
                            compressImg();
                        }
                    });
        } else {
            startUploadImgs();
        }
    }

    private void startUploadImgs() {
        if(compressedList.size()>0) {
            ossService.configUploadList(compressedList);
            ossService.uploadImgs();
        } else {
            if(progressDialog!=null)
                progressDialog.dismiss();
        }
    }

}
