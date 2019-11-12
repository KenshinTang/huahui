package com.yunlinker.manager;
import android.content.Intent;

import com.yunlinker.baseclass.BaseActivity;
import com.yunlinker.map.Location;
import com.yunlinker.upimage.UpImger;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.yunlinker.config.WebConfig.GPS_REQUEST_CODE;
import static com.yunlinker.config.WebConfig.QRCODE_GET_CODE;
import static com.yunlinker.config.WebConfig.SELECTED_IMAGE_CODE;


/**
 * Created by lemon on 2017/8/21.
 */

public class ActivityResult {
    private static ActivityResult instance = null;

    public static ActivityResult getInstance() {
        if (instance == null) {
            synchronized (ActivityResult.class) {
                if (instance == null) {
                    instance = new ActivityResult();
                }
            }
        }
        return instance;
    }

    public void resultBack(BaseActivity activity, int requestCode, int resultCode, Intent data) {
        //处理回调
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case QRCODE_GET_CODE:
                    //扫描二维码成功
                    activity.getWebView().setValue("scanf",data.getExtras().getString("code"));
                    break;
//                case UM_AUTH_CODE:
//                    UMShareAPI.get(BaseActivity.this).onActivityResult(requestCode, resultCode, data);
//                    break;
                case SELECTED_IMAGE_CODE:
                    ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    if (images != null){
                        UpImger.getInstance().initUploadImageItems(images);
                    }
                    break;
            }
        } else {
            //异常处理
        }
    }
}

