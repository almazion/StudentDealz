package com.example.studentdealz;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "category_name";

    private List<Item> categoryDeals;
    private DealAdapter dealAdapter;
    private TextView emptyState;
    private SearchView searchView;
    private ListenerRegistration dealsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.categoryScreen), (v, insets) -> {
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

        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = DealRepository.CATEGORY_FOOD;
        }

        setupHeader(categoryName);
        setupBackButton();
        setupDealsList(categoryName);
        setupSearch();
    }

    private void setupHeader(String categoryName) {
        TextView title = findViewById(R.id.categories4);
        TextView subtitle = findViewById(R.id.categories);
        title.setText(categoryName);
        subtitle.setText(getString(R.string.category_subtitle, categoryName));
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());
    }

    private void setupDealsList(String categoryName) {
        categoryDeals = new ArrayList<>();
        emptyState = findViewById(R.id.emptyState);

        RecyclerView dealsRecyclerView = findViewById(R.id.rv2);
        dealsRecyclerView.setHasFixedSize(false);
        dealsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        dealAdapter = new DealAdapter(categoryDeals, this::openDealRedemption);
        dealsRecyclerView.setAdapter(dealAdapter);
        updateEmptyState(categoryDeals);

        dealsListener = DealRepository.listenToDealsForCategory(this, categoryName, deals -> {
            categoryDeals = deals;
            filterDeals(searchView == null ? "" : searchView.getQuery().toString());
        });
    }

    private void setupSearch() {
        searchView = findViewById(R.id.categorySearch);
        searchView.setQueryHint(getString(R.string.search_in_category));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDeals(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterDeals(newText);
                return true;
            }
        });
    }

    private void filterDeals(String query) {
        List<Item> filteredDeals = DealRepository.filterDeals(categoryDeals, query);
        dealAdapter.setItems(filteredDeals);
        updateEmptyState(filteredDeals);
    }

    private void updateEmptyState(List<Item> visibleDeals) {
        if (visibleDeals.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
        }
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
