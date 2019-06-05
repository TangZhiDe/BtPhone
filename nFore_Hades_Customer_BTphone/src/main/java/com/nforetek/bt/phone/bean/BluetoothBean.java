package com.nforetek.bt.phone.bean;

public class BluetoothBean {
    private String name;
    private int imageId;
    private String address;
    private String number;
    private String sortLetters;
    private int state; // 0-未配对，1-配对过，2-已连接


    public BluetoothBean(String name){
        this.name = name;
    }

    public BluetoothBean(String name,int imageId){
        this.name = name;
        this.imageId = imageId;
    }

    public BluetoothBean(String name,String address){
        this.name = name;
        this.address = address;
    }

    public BluetoothBean(String name,int state,String address){
        this.name = name;
        this.state = state;
        this.address = address;
    }

    public BluetoothBean(int imageId,String name,String number){
        this.imageId = imageId;
        this.name = name;
        this.number = number;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }
}
