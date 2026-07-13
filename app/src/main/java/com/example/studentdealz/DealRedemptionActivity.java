package com.example.studentdealz;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DealRedemptionActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_RES_ID = "extra_image_res_id";
    private static final String EXTRA_DEAL_TITLE = "extra_deal_title";
    private static final String EXTRA_DISCOUNT = "extra_discount";
    private static final String EXTRA_PARTNER = "extra_partner";
    private static final String EXTRA_CATEGORY = "extra_category";
    private static final String EXTRA_BARCODE_VALUE = "extra_barcode_value";
    private static final String EXTRA_EXPIRATION_DATE = "extra_expiration_date";

    private String barcodeValue;

    public static Intent createIntent(Context context, Item item) {
        Intent intent = new Intent(context, DealRedemptionActivity.class);
        intent.putExtra(EXTRA_IMAGE_RES_ID, item.getImageResId());
        intent.putExtra(EXTRA_DEAL_TITLE, item.getTitle());
        intent.putExtra(EXTRA_DISCOUNT, item.getDiscount());
        intent.putExtra(EXTRA_PARTNER, item.getPartner());
        intent.putExtra(EXTRA_CATEGORY, item.getCategory());
        intent.putExtra(EXTRA_BARCODE_VALUE, item.getBarcodeValue());
        intent.putExtra(EXTRA_EXPIRATION_DATE, item.getExpirationDate());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deal_redemption);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dealRedemptionScreen), (v, insets) -> {
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

        setupButtons();
        bindDeal();
    }

    private void setupButtons() {
        ImageButton backButton = findViewById(R.id.redemptionBackButton);
        backButton.setOnClickListener(view -> finish());
        findViewById(R.id.copyCodeButton).setOnClickListener(view -> copyCode());
    }

    private void bindDeal() {
        Intent intent = getIntent();
        int imageResId = intent.getIntExtra(EXTRA_IMAGE_RES_ID, R.drawable.item1);
        String title = intent.getStringExtra(EXTRA_DEAL_TITLE);
        String discount = intent.getStringExtra(EXTRA_DISCOUNT);
        String partner = intent.getStringExtra(EXTRA_PARTNER);
        String category = intent.getStringExtra(EXTRA_CATEGORY);
        String expirationDate = intent.getStringExtra(EXTRA_EXPIRATION_DATE);
        barcodeValue = intent.getStringExtra(EXTRA_BARCODE_VALUE);

        ImageView logo = findViewById(R.id.redemptionLogo);
        ImageView dealImage = findViewById(R.id.redemptionDealImage);
        TextView titleText = findViewById(R.id.redemptionDealTitle);
        TextView discountText = findViewById(R.id.redemptionDiscount);
        TextView partnerText = findViewById(R.id.redemptionPartner);
        TextView categoryText = findViewById(R.id.redemptionCategory);
        TextView expirationText = findViewById(R.id.redemptionExpiration);
        Code128BarcodeView barcodeView = findViewById(R.id.redemptionBarcodeView);
        TextView barcodeText = findViewById(R.id.redemptionBarcodeText);

        logo.setImageResource(R.drawable.goodlogo);
        dealImage.setImageResource(imageResId);
        titleText.setText(isBlank(title) ? getString(R.string.student_discount) : title);
        discountText.setText(discount == null ? "" : getString(R.string.discount_off, discount));
        partnerText.setText(partner == null ? "" : partner);
        categoryText.setText(category == null ? "" : category);
        barcodeValue = isBlank(barcodeValue) ? createBarcodeText(partner, discount) : barcodeValue;
        barcodeView.setBarcodeValue(barcodeValue);
        barcodeText.setText(barcodeValue);
        expirationText.setText(isBlank(expirationDate) ? "" : getString(R.string.expires_on, expirationDate));
    }

    private String createBarcodeText(String partner, String discount) {
        String safePartner = partner == null ? "DEAL" : partner.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String safeDiscount = discount == null ? "00" : discount.replaceAll("[^0-9]", "");
        return "STUDENTDEALZ-" + safePartner + "-" + safeDiscount;
    }

    private void copyCode() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText(getString(R.string.discount_code), barcodeValue);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, R.string.code_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
