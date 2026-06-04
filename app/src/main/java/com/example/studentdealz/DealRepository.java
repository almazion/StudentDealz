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

        items.add(new Item(R.drawable.item1, "15%", "GOLDA", CATEGORY_FOOD));
        items.add(new Item(R.drawable.item5, "12%", "Alo", CATEGORY_CLOTHING));
        items.add(new Item(R.drawable.item4, "18%", "iDigital", CATEGORY_ELECTRONICS));
        items.add(new Item(R.drawable.item3, "10%", "KING KONG", CATEGORY_FOOD));
        items.add(new Item(R.drawable.item13, "22%", "Zara", CATEGORY_CLOTHING));
        items.add(new Item(R.drawable.item6, "50%", "ChatGPT", CATEGORY_OFFICE));
        items.add(new Item(R.drawable.item7, "23%", "Pizza Hut", CATEGORY_FOOD));
        items.add(new Item(R.drawable.item8, "18%", "Japanika", CATEGORY_FOOD));
        items.add(new Item(R.drawable.item9, "16%", "BBB", CATEGORY_FOOD));
        items.add(new Item(R.drawable.item2, "25%", "YES PLANET", CATEGORY_ATTRACTIONS));
        items.add(new Item(R.drawable.item10, "17%", "Student Escape Room", CATEGORY_ATTRACTIONS));
        items.add(new Item(R.drawable.item11, "30%", "Study Supplies", CATEGORY_OFFICE));
        items.add(new Item(R.drawable.item12, "55%", "Gemini", CATEGORY_OFFICE));



        return items;
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
                callback.onDealsLoaded(getFallbackDeals(fallbackCategory));
                return;
            }

            if (value == null || value.isEmpty()) {
                callback.onDealsLoaded(getFallbackDeals(fallbackCategory));
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
                callback.onDealsLoaded(getFallbackDeals(fallbackCategory));
            } else {
                callback.onDealsLoaded(deals);
            }
        };
    }

    private static Item createItemFromDocument(Context context, DocumentSnapshot document) {
        String discount = document.getString("discount");
        String partner = document.getString("partner");
        String category = document.getString("category");
        String imageName = document.getString("imageName");

        if (discount == null || partner == null || category == null) {
            return null;
        }

        int imageResId = resolveDrawable(context, imageName);
        return new Item(imageResId, discount, partner, category, imageName == null ? "" : imageName);
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
}
