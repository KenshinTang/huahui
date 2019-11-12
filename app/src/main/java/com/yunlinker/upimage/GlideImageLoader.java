package com.yunlinker.upimage;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yunlinker.image.GlideApp;
import com.yunlinker.hhgys.R;
import com.lzy.imagepicker.loader.ImageLoader;

import java.io.File;

/**
 * Created by lemon on 2017/7/12.
 */

public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        GlideApp.with(activity)                          //配置上下文
                .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .error(R.mipmap.default_image)           //设置错误图片
                .placeholder(R.mipmap.default_image)     //设置占位图片
                .diskCacheStrategy(DiskCacheStrategy.NONE)//设置缓存
                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {

    }

}
