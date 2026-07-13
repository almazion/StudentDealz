package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Item> allDeals;
    private DealAdapter dealAdapter;
    private ListenerRegistration dealsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.screen_horizontal_padding);
            v.setPadding(
                    systemBars.left + horizontalPadding,
                    systemBars.top,
                    systemBars.right + horizontalPadding,
                    systemBars.bottom
            );
            return insets;
        });
        // Analytics is required for the final project and starts collecting after initialization.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setupLogoutButton();
        setupCategoryButtons();
        setupDealsList();
        setupSearch();
    }

    private void setupLogoutButton() {
        findViewById(R.id.logoutButton).setOnClickListener(view -> {
            UserRepository.signOut();
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupCategoryButtons() {
        findViewById(R.id.food).setOnClickListener(view -> openCategory(DealRepository.CATEGORY_FOOD));
        findViewById(R.id.clothing).setOnClickListener(view -> openCategory(DealRepository.CATEGORY_CLOTHING));
        findViewById(R.id.attractions).setOnClickListener(view -> openCategory(DealRepository.CATEGORY_ATTRACTIONS));
        findViewById(R.id.office).setOnClickListener(view -> openCategory(DealRepository.CATEGORY_OFFICE));
        findViewById(R.id.electronics).setOnClickListener(view -> openCategory(DealRepository.CATEGORY_ELECTRONICS));
    }

    private void openCategory(String categoryName) {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_NAME, categoryName);
        startActivity(intent);
    }

    private void setupDealsList() {
        allDeals = new ArrayList<>();

        RecyclerView dealsRecyclerView = findViewById(R.id.rv);
        dealsRecyclerView.setHasFixedSize(false);
        dealsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        dealAdapter = new DealAdapter(allDeals, this::openDealRedemption);
        dealsRecyclerView.setAdapter(dealAdapter);

        dealsListener = DealRepository.listenToAllDeals(this, deals -> {
            allDeals = deals;
            dealAdapter.setItems(allDeals);
        });
    }

    private void setupSearch() {
        findViewById(R.id.search).setOnClickListener(view -> openSearchScreen());
    }

    private void openSearchScreen() {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }

    private void openDealRedemption(Item item) {
        startActivity(DealRedemptionActivity.createIntent(this, item));
    }

    @Override
    protected void onDestroy() {
        if (dealsListener != null) {
            dealsListener.remove();
        }
        super.onDestroy();
    }
}
