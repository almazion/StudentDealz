package com.example.studentdealz;

public class Item {
    private int Item;
    private  String Discount;
    private  String Partner;

    public Item(int item, String discount, String partner) {
        Item = item;
        Discount = discount;
        Partner = partner;
    }

    public int getItem() {
        return Item;
    }

    public String getDiscount() {
        return Discount;
    }

    public String getPartner() {
        return Partner;
    }
}
