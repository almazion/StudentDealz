package com.example.studentdealz;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private final List<Item> allDeals = new ArrayList<>();
    private DealAdapter dealAdapter;
    private TextInputEditText searchInput;
    private TextView emptyState;
    private ListenerRegistration dealsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.searchScreen), (v, insets) -> {
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

        setupBackButton();
        setupSearchInput();
        setupSuggestionChips();
        setupResultsList();
        listenForDeals();
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.searchBackButton);
        backButton.setOnClickListener(view -> finish());
    }

    private void setupSearchInput() {
        searchInput = findViewById(R.id.dealSearchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDeals(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSuggestionChips() {
        int[] chipIds = {
                R.id.chipPizza,
                R.id.chipCoffee,
                R.id.chipClothes,
                R.id.chipGym,
                R.id.chipIceCream,
                R.id.chipLaptop,
                R.id.chipPhone,
                R.id.chipRestaurants,
                R.id.chipAttractions
        };

        for (int chipId : chipIds) {
            Chip chip = findViewById(chipId);
            chip.setOnClickListener(view -> {
                String suggestion = chip.getText().toString();
                searchInput.setText(suggestion);
                searchInput.setSelection(suggestion.length());
            });
        }
    }

    private void setupResultsList() {
        emptyState = findViewById(R.id.searchEmptyState);

        RecyclerView resultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        resultsRecyclerView.setHasFixedSize(false);
        resultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        dealAdapter = new DealAdapter(new ArrayList<>(), this::openDealRedemption);
        resultsRecyclerView.setAdapter(dealAdapter);
    }

    private void listenForDeals() {
        dealsListener = DealRepository.listenToAllDeals(this, deals -> {
            allDeals.clear();
            allDeals.addAll(deals);
            filterDeals(searchInput == null || searchInput.getText() == null
                    ? ""
                    : searchInput.getText().toString());
        });
    }

    private void filterDeals(String query) {
        if (query == null || query.trim().isEmpty()) {
            dealAdapter.setItems(new ArrayList<>());
            emptyState.setVisibility(View.GONE);
            return;
        }

        List<Item> filteredDeals = DealRepository.filterDeals(allDeals, query);
        dealAdapter.setItems(filteredDeals);
        emptyState.setVisibility(filteredDeals.isEmpty() ? View.VISIBLE : View.GONE);
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
