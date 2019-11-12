package com.yunlinker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by szmkq on 2018/12/12.
 */

public class PrintBean {
    String storename = "";
    String storephone = "";
    String number = "";
    String code = "";
    String action = "";
    String waitnum= "";

    public List<Map<String, String>> getMap() {
        return map;
    }

    public void setMap(List<Map<String, String>> map) {
        this.map = map;
    }

    List<Map<String,String>> map =  new ArrayList<>();
    String username = "";
    String usersex = "";
    String userphone = "";
    String back = "";

    public String getWaitnum() {
        return waitnum;
    }

    public void setWaitnum(String waitnum) {
        this.waitnum = waitnum;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    public String getStorename() {
        return storename;
    }

    public void setStorename(String storename) {
        this.storename = storename;
    }

    public String getStorephone() {
        return storephone;
    }

    public void setStorephone(String storephone) {
        this.storephone = storephone;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsersex() {
        return usersex;
    }

    public void setUsersex(String usersex) {
        this.usersex = usersex;
    }

    public String getUserphone() {
        return userphone;
    }

    public void setUserphone(String userphone) {
        this.userphone = userphone;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }




}
