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

    public static ListenerRegistration listenToAllDeals(Context context, DealsCallback callback) {
        return FirebaseFirestore.getInstance()
                .collection(DEALS_COLLECTION)
                .addSnapshotListener(createDealsListener(context, callback));
    }

    public static ListenerRegistration listenToDealsForCategory(Context context, String category,
                                                                DealsCallback callback) {
        return FirebaseFirestore.getInstance()
                .collection(DEALS_COLLECTION)
                .whereEqualTo("category", category)
                .addSnapshotListener(createDealsListener(context, callback));
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

    private static EventListener<QuerySnapshot> createDealsListener(Context context, DealsCallback callback) {
        return (value, error) -> {
            if (error != null) {
                Log.e(TAG, "Could not listen to Deals collection.", error);
                callback.onDealsLoaded(new ArrayList<>());
                return;
            }

            if (value == null || value.isEmpty()) {
                callback.onDealsLoaded(new ArrayList<>());
                return;
            }

            List<Item> deals = new ArrayList<>();
            for (DocumentSnapshot document : value.getDocuments()) {
                Item item = createItemFromDocument(context, document);
                if (item != null) {
                    deals.add(item);
                }
            }

            callback.onDealsLoaded(deals);
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

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
