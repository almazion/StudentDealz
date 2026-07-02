package com.example.studentdealz;

public class Item {
    private final int imageResId;
    private final String dealId;
    private final String title;
    private final String discount;
    private final String partner;
    private final String category;
    private final String imageName;
    private final String keywords;
    private final String barcodeValue;
    private final String expirationDate;

    public Item(int imageResId, String discount, String partner, String category) {
        this(imageResId, discount, partner, category, "", "");
    }

    public Item(int imageResId, String discount, String partner, String category, String imageName) {
        this(imageResId, discount, partner, category, imageName, "");
    }

    public Item(int imageResId, String discount, String partner, String category, String imageName,
                String keywords) {
        this(
                imageResId,
                createDealId(partner),
                partner + " Student Discount",
                discount,
                partner,
                category,
                imageName,
                keywords,
                "",
                ""
        );
    }

    public Item(int imageResId, String dealId, String title, String discount, String partner,
                String category, String imageName, String keywords, String barcodeValue,
                String expirationDate) {
        this.imageResId = imageResId;
        this.dealId = isBlank(dealId) ? createDealId(partner) : dealId.trim();
        this.title = isBlank(title) ? partner + " Student Discount" : title.trim();
        this.discount = discount;
        this.partner = partner;
        this.category = category;
        this.imageName = imageName;
        this.keywords = keywords;
        this.barcodeValue = isBlank(barcodeValue) ? createBarcodeValue(this.dealId) : barcodeValue.trim();
        this.expirationDate = expirationDate == null ? "" : expirationDate.trim();
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDealId() {
        return dealId;
    }

    public String getTitle() {
        return title;
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

    public String getKeywords() {
        return keywords;
    }

    public String getBarcodeValue() {
        return barcodeValue;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        String searchableText = (partner + " "
                + title + " "
                + discount + " "
                + category + " "
                + dealId + " "
                + barcodeValue + " "
                + imageName + " "
                + keywords).toLowerCase();
        return searchableText.contains(lowerCaseQuery);
    }

    private static String createDealId(String partner) {
        String safePartner = partner == null ? "DEAL" : partner.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (safePartner.isEmpty()) {
            safePartner = "DEAL";
        }
        return safePartner;
    }

    private static String createBarcodeValue(String dealId) {
        return "STUDENTDEALZ-" + dealId;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
