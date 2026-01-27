package com.example.test_v2.homeIntroLogin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

public class AccountCreationPage extends AppCompatActivity {

    private HelperAppDatabase database;
    private Uri selectedImageUri = null;
    private ImageView profileImageView;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImageView.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_creation_page);

        database = Room.databaseBuilder(getApplicationContext(), HelperAppDatabase.class, "user-database").build();

        EditText fullNameEditText = findViewById(R.id.fullName);
        EditText pinEditText = findViewById(R.id.pin);
        EditText confirmPinEditText = findViewById(R.id.confirmPin);
        profileImageView = findViewById(R.id.profile_picture);
        Button continueButton = findViewById(R.id.continueButton);
        TextView learnMoreLink = findViewById(R.id.learnMoreLink);

        // Set default profile picture
        profileImageView.setImageResource(R.drawable.ic_person);

        // Launch image picker when clicking on the profile picture
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        continueButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String pin = pinEditText.getText().toString();
            String confirmPin = confirmPinEditText.getText().toString();

            if (fullName.isEmpty() || pin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pin.equals(confirmPin)) {
                Toast.makeText(this, "Pins do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pin.length() != 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String hashedPin = hashPin(pin);
                Executors.newSingleThreadExecutor().execute(() -> {
                    HelperUserAccount existingAccount = database.userDao().getUserByPin(hashedPin);

                    runOnUiThread(() -> {
                        if (existingAccount != null) {
                            Toast.makeText(this, "PIN already exists. Please choose a different PIN.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Save the profile picture
                            String profileImagePath = saveProfilePicture();

                            // Create a new account
                            HelperUserAccount account = new HelperUserAccount(fullName, hashedPin, profileImagePath);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                database.userDao().insert(account);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, LoginPage.class); // Navigate to LoginPage
                                    startActivity(intent);
                                    finish(); // Close the current activity
                                });
                            });
                        }
                    });
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
            }
        });

        learnMoreLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sturge-weber.org"));
            startActivity(browserIntent);
        });
    }

    private String saveProfilePicture() {
        if (selectedImageUri == null) {
            // Return the default profile picture resource if no image is selected
            return "ic_person";
        }

        File directory = new File(getFilesDir(), "profile_pictures");
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        File file = new File(directory, "profile_picture_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
             FileOutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return file.getAbsolutePath(); // Return the file path
        } catch (IOException e) {
            e.printStackTrace();
            return "ic_person"; // Fallback to default
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