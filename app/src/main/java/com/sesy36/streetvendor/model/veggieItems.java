package com.sesy36.streetvendor.model;

public class veggieItems
{
    private String name;
    private int picId;

    public veggieItems(String name, int picId) {
        this.name = name;
        this.picId = picId;
    }

    public int getPicId() {
        return picId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
