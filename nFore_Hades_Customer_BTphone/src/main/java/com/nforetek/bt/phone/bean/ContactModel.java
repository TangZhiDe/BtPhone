package com.nforetek.bt.phone.bean;


public class ContactModel implements Comparable<ContactModel>{
    private Long id;
    private String name;
    private int type;
    private int fav;
    private String letters;
    private String location;
    private String num1;
    private String num2;
    private String num3;
    private String num4;
    private String num5;
    public ContactModel(Long id, String name, int type, int fav, String letters,
                        String location, String num1, String num2, String num3, String num4,
                        String num5) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.fav = fav;
        this.letters = letters;
        this.location = location;
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
        this.num4 = num4;
        this.num5 = num5;
    }
    public ContactModel() {
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
    public String getFirstName()
    {
        if (this.getName().isEmpty()) {
            return "";
        }
        String str;
        str = this.getName();
        return str.substring(0,1);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
    public String getNum1() {
        return this.num1;
    }
    public void setNum1(String num1) {
        this.num1 = num1;
    }
    public String getNum2() {
        return this.num2;
    }
    public void setNum2(String num2) {
        this.num2 = num2;
    }
    public String getNum3() {
        return this.num3;
    }
    public void setNum3(String num3) {
        this.num3 = num3;
    }
    public String getNum4() {
        return this.num4;
    }
    public void setNum4(String num4) {
        this.num4 = num4;
    }
    public String getNum5() {
        return this.num5;
    }
    public void setNum5(String num5) {
        this.num5 = num5;
    }
    public int getFav() {
        return this.fav;
    }
    public void setFav(int fav) {
        this.fav = fav;
    }
    public String getLetters() {
        return this.letters;
    }
    public void setLetters(String letters) {
        this.letters = letters;
    }

    @Override
    public int compareTo(ContactModel o) {

        return this.name.compareTo(o.getName());
    }
}
