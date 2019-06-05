package com.nforetek.bt.phone.bean;


public class CallLog {
    private Long id;
    private String name;
    private int type;
    private String num;
    private String time;
    private String location;
    private int img_id;



    public CallLog(Long id, String name, int type, String num, String time,
                   String location, int img_id) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.num = num;
        this.time = time;
        this.location = location;
        this.img_id = img_id;
    }
    public CallLog() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getNum() {
        return this.num;
    }
    public void setNum(String num) {
        this.num = num;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public int getImg_id() {
        return this.img_id;
    }
    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public String getFirstName()
    {
        if (this.getName().isEmpty()) {
            return "";
        }
        String str;
        str = this.getName();
        return str.substring(0,1);
    }
}
