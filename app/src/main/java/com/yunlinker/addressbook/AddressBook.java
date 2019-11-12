package com.yunlinker.addressbook;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.yunlinker.auth.WebPermissionManager;
import com.yunlinker.baseclass.BaseWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lemon on 2017/12/1.
 */

public class AddressBook {
    //开始获取联系人
    public static void startGetList(final Activity a,final BaseWebView w) {
        //权限检查
        WebPermissionManager.getInstance(a).CheckPermission(WebPermissionManager.GetContactsPermissions, new WebPermissionManager.OnPermissionBack() {
            @Override
            public void success() {
                getList(a, w);
            }

            @Override
            public void error() {
                Toast.makeText(a,"授权失败，请设置权限后重试",Toast.LENGTH_SHORT).show();
            }
        });
    }


    //获取联系人
    static void getList(final Activity a, final BaseWebView w) {
        final List<ContactsBean> contactsBeanList = new ArrayList<ContactsBean>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = a.getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                JSONObject exsitObj = new JSONObject();
                while(cursor.moveToNext())
                {
                    int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    String contact = cursor.getString(nameFieldColumnIndex);
                    String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
                    if(phone!=null){
                        while(phone.moveToNext())
                        {
                            String PhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            PhoneNumber = PhoneNumber.replace("+86","");
                            String newphone = "";
                            for(int i=0;i<PhoneNumber.length();i++) {
                                if (PhoneNumber.charAt(i) >= 48 && PhoneNumber.charAt(i) <= 57) {
                                    newphone += PhoneNumber.charAt(i);
                                }
                            }
                            if(newphone.length()==11){
                                if(!exsitObj.has(newphone)){
                                    try {
                                        exsitObj.put(newphone, "exist");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    ContactsBean bean = new ContactsBean(contact,newphone);
                                    contactsBeanList.add(bean);
                                }
                            }
                        }
                    }
                    phone.close();
                }
                cursor.close();
                try{
                    JSONArray jsonArray = new JSONArray();
                    for(int i = 0;i<contactsBeanList.size();i++){
                        JSONObject jo = new JSONObject();
                        if (contactsBeanList.get(i)!=null) {
                            jo.put("name", contactsBeanList.get(i).getContactName());
                            jo.put("phone", contactsBeanList.get(i).getContactPhone());
                            jsonArray.put(jo);
                        }
                    }
                    JSONObject robj = new JSONObject();
                    robj.put("code", "1");
                    robj.put("list", jsonArray);
                    w.setValue("getAddressBook", robj.toString());
                    return;
                }catch(Exception e){

                }
                w.setValue("getAddressBook", "{\"code\":0, \"msg\":\"联系人列表获取失败\"}");
            }
        }).start();
    }
}
