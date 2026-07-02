package com.example.studentdealz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DealRepository {

    private static final String TAG = "DealRepository";

    public interface DealsCallback {
        void onDealsLoaded(List<Item> deals);
    }

    private static final String DEALS_COLLECTION = "Deals";
    private static List<Item> cachedDeals;

    public static final String CATEGORY_FOOD = "Food";
    public static final String CATEGORY_CLOTHING = "Clothing";
    public static final String CATEGORY_ATTRACTIONS = "Attractions";
    public static final String CATEGORY_OFFICE = "Office";
    public static final String CATEGORY_ELECTRONICS = "Electronics";

    public static List<String> getCategories() {
        return Arrays.asList(
                CATEGORY_FOOD,
                CATEGORY_CLOTHING,
                CATEGORY_ATTRACTIONS,
                CATEGORY_OFFICE,
                CATEGORY_ELECTRONICS
        );
    }

    public static List<Item> getAllDeals() {
        List<Item> items = new ArrayList<>();

        items.add(new Item(R.drawable.item1, "golda-15", "GOLDA Student Discount", "15%", "GOLDA",
                CATEGORY_FOOD, "item1", "ice cream coffee dessert restaurant restaurants", "", ""));
        items.add(new Item(R.drawable.item5, "alo-12", "Alo Student Discount", "12%", "Alo",
                CATEGORY_CLOTHING, "item5", "clothes clothing fashion activewear", "", ""));
        items.add(new Item(R.drawable.item4, "idigital-18", "iDigital Student Discount", "18%",
                "iDigital", CATEGORY_ELECTRONICS, "item4", "phone laptop computer apple electronics",
                "", ""));
        items.add(new Item(R.drawable.item3, "kingkong-10", "KING KONG Student Discount", "10%",
                "KING KONG", CATEGORY_FOOD, "item3", "burger burgers restaurant restaurants", "", ""));
        items.add(new Item(R.drawable.item13, "zara-22", "Zara Student Discount", "22%", "Zara",
                CATEGORY_CLOTHING, "item13", "clothes clothing fashion", "", ""));
        items.add(new Item(R.drawable.item6, "chatgpt-50", "ChatGPT Student Discount", "50%",
                "ChatGPT", CATEGORY_OFFICE, "item6", "study software ai office", "", ""));
        items.add(new Item(R.drawable.item7, "pizzahut-23", "Pizza Hut Student Discount", "23%",
                "Pizza Hut", CATEGORY_FOOD, "item7", "pizza restaurant restaurants", "", ""));
        items.add(new Item(R.drawable.item8, "japanika-18", "Japanika Student Discount", "18%",
                "Japanika", CATEGORY_FOOD, "item8", "sushi restaurant restaurants", "", ""));
        items.add(new Item(R.drawable.item9, "bbb-16", "BBB Student Discount", "16%", "BBB",
                CATEGORY_FOOD, "item9", "burger burgers restaurant restaurants", "", ""));
        items.add(new Item(R.drawable.item2, "yesplanet-25", "YES PLANET Student Discount", "25%",
                "YES PLANET", CATEGORY_ATTRACTIONS, "item2", "cinema movie movies attractions", "", ""));
        items.add(new Item(R.drawable.item10, "escaperoom-17", "Student Escape Room Discount", "17%",
                "Student Escape Room", CATEGORY_ATTRACTIONS, "item10", "escape room attractions", "", ""));
        items.add(new Item(R.drawable.item11, "studysupplies-30", "Study Supplies Student Discount",
                "30%", "Study Supplies", CATEGORY_OFFICE, "item11", "study school office stationery",
                "", ""));
        items.add(new Item(R.drawable.item12, "gemini-55", "Gemini Student Discount", "55%",
                "Gemini", CATEGORY_OFFICE, "item12", "study software ai office", "", ""));



        return items;
    }

    public static List<Item> getCurrentDeals() {
        if (cachedDeals == null || cachedDeals.isEmpty()) {
            cachedDeals = new ArrayList<>(getAllDeals());
        }
        return new ArrayList<>(cachedDeals);
    }

    public static ListenerRegistration listenToAllDeals(Context context, DealsCallback callback) {
        return FirebaseFirestore.getInstance()
                .collection(DEALS_COLLECTION)
                .addSnapshotListener(createDealsListener(context, callback, null));
    }

    public static ListenerRegistration listenToDealsForCategory(Context context, String category,
                                                                DealsCallback callback) {
        return FirebaseFirestore.getInstance()
                .collection(DEALS_COLLECTION)
                .whereEqualTo("category", category)
                .addSnapshotListener(createDealsListener(context, callback, category));
    }

    public static List<Item> getDealsForCategory(String category) {
        List<Item> categoryItems = new ArrayList<>();
        for (Item item : getAllDeals()) {
            if (item.getCategory().equals(category)) {
                categoryItems.add(item);
            }
        }
        return categoryItems;
    }

    public static List<Item> filterDeals(List<Item> sourceItems, String query) {
        List<Item> filteredItems = new ArrayList<>();
        for (Item item : sourceItems) {
            if (item.matches(query)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    private static EventListener<QuerySnapshot> createDealsListener(Context context, DealsCallback callback,
                                                                   String fallbackCategory) {
        return (value, error) -> {
            if (error != null) {
                Log.e(TAG, "Could not listen to Deals collection.", error);
                List<Item> fallbackDeals = getFallbackDeals(fallbackCategory);
                updateCache(fallbackDeals, fallbackCategory);
                callback.onDealsLoaded(fallbackDeals);
                return;
            }

            if (value == null || value.isEmpty()) {
                List<Item> fallbackDeals = getFallbackDeals(fallbackCategory);
                updateCache(fallbackDeals, fallbackCategory);
                callback.onDealsLoaded(fallbackDeals);
                return;
            }

            List<Item> deals = new ArrayList<>();
            for (DocumentSnapshot document : value.getDocuments()) {
                Item item = createItemFromDocument(context, document);
                if (item != null) {
                    deals.add(item);
                }
            }

            if (deals.isEmpty()) {
                List<Item> fallbackDeals = getFallbackDeals(fallbackCategory);
                updateCache(fallbackDeals, fallbackCategory);
                callback.onDealsLoaded(fallbackDeals);
            } else {
                updateCache(deals, fallbackCategory);
                callback.onDealsLoaded(deals);
            }
        };
    }

    private static Item createItemFromDocument(Context context, DocumentSnapshot document) {
        String dealId = firstNonBlank(document.getString("dealId"), document.getId());
        String title = document.getString("title");
        String discount = firstNonBlank(document.getString("discountText"), document.getString("discount"));
        String partner = firstNonBlank(document.getString("storeName"), document.getString("partner"));
        String category = document.getString("category");
        String imageName = document.getString("imageName");
        String keywords = document.getString("keywords");
        String barcodeValue = firstNonBlank(
                document.getString("barcodeValue"),
                document.getString("discountCode"),
                document.getString("code")
        );
        String expirationDate = document.getString("expirationDate");

        if (discount == null || partner == null || category == null) {
            return null;
        }

        int imageResId = resolveDrawable(context, imageName);
        return new Item(
                imageResId,
                dealId,
                title,
                discount,
                partner,
                category,
                imageName == null ? "" : imageName,
                keywords == null ? "" : keywords,
                barcodeValue,
                expirationDate == null ? "" : expirationDate
        );
    }

    private static int resolveDrawable(Context context, String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return R.drawable.item1;
        }

        int resourceId = context.getResources().getIdentifier(
                imageName.trim(),
                "drawable",
                context.getPackageName()
        );
        return resourceId == 0 ? R.drawable.item1 : resourceId;
    }

    private static List<Item> getFallbackDeals(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllDeals();
        }
        return getDealsForCategory(category);
    }

    private static void updateCache(List<Item> deals, String category) {
        if (category == null || category.trim().isEmpty()) {
            cachedDeals = new ArrayList<>(deals);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
