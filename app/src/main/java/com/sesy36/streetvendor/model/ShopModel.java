package com.sesy36.streetvendor.model;

public class ShopModel
{
    private String profileImage, shopName,uid, phoneNo;


    public ShopModel() {
    }

    public ShopModel(String profileImage, String shopName, String uid, String phoneNo) {
        this.profileImage = profileImage;
        this.shopName = shopName;
        this.uid = uid;
        this.phoneNo = phoneNo;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
