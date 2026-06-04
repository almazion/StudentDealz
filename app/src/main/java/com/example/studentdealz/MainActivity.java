package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Item> allDeals;
    private DealAdapter dealAdapter;
    private SearchView searchView;
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
// Obtain the FirebaseAnalytics instance.
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
        allDeals = DealRepository.getAllDeals();

        RecyclerView dealsRecyclerView = findViewById(R.id.rv);
        dealsRecyclerView.setHasFixedSize(false);
        dealsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        dealAdapter = new DealAdapter(allDeals);
        dealsRecyclerView.setAdapter(dealAdapter);

        dealsListener = DealRepository.listenToAllDeals(this, deals -> {
            allDeals = deals;
            filterDeals(searchView == null ? "" : searchView.getQuery().toString());
        });
    }

    private void setupSearch() {
        searchView = findViewById(R.id.search);
        searchView.setQueryHint("Search deals");
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
        dealAdapter.setItems(DealRepository.filterDeals(allDeals, query));
    }

    @Override
    protected void onDestroy() {
        if (dealsListener != null) {
            dealsListener.remove();
        }
        super.onDestroy();
    }
}
