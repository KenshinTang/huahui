package com.yunlinker.addressbook;

/**
 * Created by Administrator on 2017/1/12.
 */
public class ContactsBean {

    private String contactName;
    private String contactPhone;

    public ContactsBean(String name,String phone){
        this.contactName = name;
        this.contactPhone = phone;
    }
    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
