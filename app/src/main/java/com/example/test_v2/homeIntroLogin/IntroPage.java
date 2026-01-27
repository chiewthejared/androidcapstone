package com.example.test_v2.homeIntroLogin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test_v2.R;

public class IntroPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_page);

        // Continue Button
        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginPage.class); // Navigate to AccountCreationPage
            startActivity(intent);
        });

        // Learn More Text
        TextView learnMoreText = findViewById(R.id.learn_more);
        learnMoreText.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sturge-weber.org"));
            startActivity(browserIntent);
        });
    }
}
