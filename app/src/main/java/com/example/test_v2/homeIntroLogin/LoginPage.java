package com.example.test_v2.homeIntroLogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.test_v2.GuestTimerPage;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

public class LoginPage extends AppCompatActivity {

    private StringBuilder enteredPin = new StringBuilder();
    private HelperAppDatabase database; // Reuse the shared HelperAppDatabase
    private ImageView[] pinBubbles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize Room database
        database = Room.databaseBuilder(getApplicationContext(), HelperAppDatabase.class, "user-database").build();

        // Initialize PIN bubbles
        pinBubbles = new ImageView[]{
                findViewById(R.id.bubble_1),
                findViewById(R.id.bubble_2),
                findViewById(R.id.bubble_3),
                findViewById(R.id.bubble_4)
        };

        // Setup number pad listeners
        setNumberPadListeners();

        // Backspace (Delete) Button
        findViewById(R.id.button_delete).setOnClickListener(v -> handleBackspaceInput());

        // Account Creation Button
        TextView accountCreationButton = findViewById(R.id.account_creation_button);
        accountCreationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountCreationPage.class);
            startActivity(intent);
        });

        ImageView guestIcon = findViewById(R.id.guest_timer_icon);
        guestIcon.setOnClickListener(v -> {
            // Launch a new "GuestTimerPage"
            startActivity(new Intent(this, GuestTimerPage.class));
        });
    }

    private void setNumberPadListeners() {
        int[] buttonIds = {
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
                R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7,
                R.id.button_8, R.id.button_9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            int finalI = i;
            findViewById(buttonIds[i]).setOnClickListener(v -> handleNumberInput(String.valueOf(finalI)));
        }
    }

    private void handleNumberInput(String number) {
        if (enteredPin.length() < 4) {
            enteredPin.append(number);
            pinBubbles[enteredPin.length() - 1].setBackgroundResource(R.drawable.filled_bubble);

            if (enteredPin.length() == 4) {
                validatePin();
            }
        }
    }

    private void handleBackspaceInput() {
        if (enteredPin.length() > 0) {
            pinBubbles[enteredPin.length() - 1].setBackgroundResource(R.drawable.empty_bubble);
            enteredPin.deleteCharAt(enteredPin.length() - 1);
        }
    }

    private void validatePin() {
        String enteredPinString = enteredPin.toString();

        try {
            String hashedPin = hashPin(enteredPinString);

            Executors.newSingleThreadExecutor().execute(() -> {
                HelperUserAccount account = database.userDao().getUserByPin(hashedPin);

                runOnUiThread(() -> {
                    if (account == null) {
                        Toast.makeText(this, "Incorrect PIN or Account does not exist", Toast.LENGTH_SHORT).show();
                        clearPin();
                    } else {
                        // Save the hashed PIN to SharedPreferences for the session
                        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("loggedInPin", hashedPin);
                        editor.apply(); // Commit the changes

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to the HomePage
                        Intent intent = new Intent(this, HomePage.class);
                        startActivity(intent);
                        finish(); // Close the LoginPage
                    }
                });
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error validating PIN", Toast.LENGTH_SHORT).show();
            clearPin();
        }
    }

    private void clearPin() {
        enteredPin.setLength(0);
        for (ImageView bubble : pinBubbles) {
            bubble.setBackgroundResource(R.drawable.empty_bubble);
        }
    }

    private String hashPin(String pin) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(pin.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}