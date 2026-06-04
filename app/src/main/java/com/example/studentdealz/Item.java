package com.example.studentdealz;

public class Item {
    private final int imageResId;
    private final String discount;
    private final String partner;
    private final String category;
    private final String imageName;

    public Item(int imageResId, String discount, String partner, String category) {
        this.imageResId = imageResId;
        this.discount = discount;
        this.partner = partner;
        this.category = category;
        this.imageName = "";
    }

    public Item(int imageResId, String discount, String partner, String category, String imageName) {
        this.imageResId = imageResId;
        this.discount = discount;
        this.partner = partner;
        this.category = category;
        this.imageName = imageName;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDiscount() {
        return discount;
    }

    public String getPartner() {
        return partner;
    }

    public String getCategory() {
        return category;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        return partner.toLowerCase().contains(lowerCaseQuery)
                || discount.toLowerCase().contains(lowerCaseQuery)
                || category.toLowerCase().contains(lowerCaseQuery);
    }
}
