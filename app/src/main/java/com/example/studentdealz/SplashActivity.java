package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MILLIS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> destination = UserRepository.isUserSignedIn()
                    ? MainActivity.class
                    : StartActivity.class;
            Intent intent = new Intent(SplashActivity.this, destination);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MILLIS);
    }
}
