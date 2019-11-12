package com.yunlinker.manager;

import android.content.Intent;
import android.text.TextUtils;

import com.yunlinker.baseclass.BaseActivity;
import com.yunlinker.baseclass.BaseWebView;
import com.yunlinker.hhgys.MainActivity;

import java.util.Stack;

/**
 * Created by lemon on 2017/8/3.
 */

public class ActivityManager {


    private static ActivityManager instance = null;

    public static ActivityManager getInstance() {
        if (instance == null) {
            synchronized (ActivityManager.class) {
                if (instance == null) {
                    instance = new ActivityManager();
                    instance.stacks = new Stack<>();
                    instance.webStacks = new Stack<>();
                }
            }
        }
        return instance;
    }


    public Stack<BaseWebView> webStacks;
    private Stack<MainActivity> stacks;

    //添加控制器
    public void add(MainActivity a) {
        stacks.push(a);
    }

    public void start(String url) {
        MainActivity a = stacks.pop();
        Intent it =new Intent(a ,MainActivity.class);
        it.putExtra("sendUrl", url);
        a.startActivity(it);
        stacks.push(a);
    }

    //根据url返回指定控制器
    public void backTo(String url) {
        MainActivity c = null;
        int times = 0;
        for (MainActivity a : stacks) {
            times ++;
            if (TextUtils.equals(a.loadUrl, url))
                break;
        }
        for(int i=0;i<times;i++) {
            MainActivity b = stacks.pop();
            b.finish();
        }
    }

    //根据次数返回
    public void back(int t) {
        for(int i=0;i<t;i++) {
            MainActivity b = stacks.pop();
            b.finish();
        }
    }

    //关闭除顶层控制器外的所有控制器
    public void finishButTop() {
        MainActivity a = null;
        int times = stacks.size();
        for(int i=0;i<times;i++) {
            MainActivity b = stacks.pop();
            if(i != 0)
                b.finish();
            else
                a = b;
        }
        stacks.push(a);
    }

    //关闭除底层控制器外的所有控制器
    public void finishButFirst() {
        int times = stacks.size();
        for(int i=times;i>1;i--) {
            MainActivity b = stacks.pop();
            if(i != 0)
                b.finish();
        }
    }


    //获取第一个控制器
    public BaseActivity getFirst() {
        if(stacks.size()>0)
            return stacks.get(0);
        else
            return null;
    }

    //获取最顶层控制器
    public BaseActivity getTop() {
        if(stacks.size()>0)
            return stacks.get(stacks.size()-1);
        else
            return null;
    }
}
