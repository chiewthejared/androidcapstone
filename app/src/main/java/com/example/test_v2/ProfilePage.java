package com.example.test_v2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.homeIntroLogin.HomePage;
import com.example.test_v2.tags.TagListPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfilePage extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView;
    private LinearLayout editTagsTile; // Replaces manageTagsButton
    private Button backButton;

    private HelperAppDatabase db;
    private HelperUserAccount loggedInUser;

    // Image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        String savedPath = saveProfilePicture(selectedImageUri);
                        if (savedPath != null) {
                            updateUserProfileImage(savedPath);
                        } else {
                            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Initialize UI components
        profileImageView = findViewById(R.id.profile_picture);
        userNameTextView = findViewById(R.id.user_name);
        editTagsTile = findViewById(R.id.edit_tags_tile);
        backButton = findViewById(R.id.back_button);

        // Initialize database
        db = HelperAppDatabase.getDatabase(getApplicationContext());

        // Load user info
        loadUserData();

        // Change profile picture
        profileImageView.setOnClickListener(v -> showChangeProfilePictureDialog());

        // Change name
        userNameTextView.setOnClickListener(v -> showChangeNameDialog());

        // "Edit Tags" tile click -> go to TagListPage
        editTagsTile.setOnClickListener(v -> {
            startActivity(new Intent(ProfilePage.this, TagListPage.class));
        });

        // Back button -> HomePage
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfilePage.this, HomePage.class));
            finish();
        });
    }

    /**
     * Loads the logged-in user's data from the database and updates UI.
     */
    private void loadUserData() {
        new Thread(() -> {
            String hashedPin = getSessionPin();
            if (hashedPin == null) return;

            // Fetch the user from DB
            loggedInUser = db.userDao().getUserByPin(hashedPin);

            runOnUiThread(() -> {
                if (loggedInUser != null) {
                    userNameTextView.setText(loggedInUser.getFullName());

                    // Load profile image if exists
                    String imagePath = loggedInUser.getProfileImagePath();
                    if (imagePath != null && !imagePath.equals("ic_person")) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                            profileImageView.setImageBitmap(bitmap);
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_person);
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_person);
                    }
                }
            });
        }).start();
    }

    /**
     * Prompts "Change profile picture?" Yes/No. If yes, opens the image picker.
     */
    private void showChangeProfilePictureDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture?")
                .setMessage("Would you like to change your profile picture?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Launch image picker
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    imagePickerLauncher.launch(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Saves the selected image URI to internal storage and returns the absolute path.
     */
    private String saveProfilePicture(Uri selectedImageUri) {
        try {
            File directory = new File(getFilesDir(), "profile_pictures");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, "profile_picture_" + System.currentTimeMillis() + ".jpg");
            try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates the user object with the new image path, saves to DB, and refreshes the UI.
     */
    private void updateUserProfileImage(String newPath) {
        new Thread(() -> {
            if (loggedInUser == null) return;

            loggedInUser.setProfileImagePath(newPath);
            db.userDao().update(loggedInUser);

            runOnUiThread(() -> {
                File imageFile = new File(newPath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    profileImageView.setImageBitmap(bitmap);
                    Toast.makeText(ProfilePage.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Prompts "Change name?" Yes/No. If yes, opens a dialog to enter a new name.
     */
    private void showChangeNameDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Name?")
                .setMessage("Would you like to change your user name?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    showNameInputDialog();
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Shows a small dialog with an EditText to enter a new user name.
     */
    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new name");

        final EditText input = new EditText(this);
        input.setHint("New name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
            } else {
                Toast.makeText(ProfilePage.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Updates the user's name in the DB and refreshes UI.
     */
    private void updateUserName(String newName) {
        new Thread(() -> {
            if (loggedInUser == null) return;

            loggedInUser.setFullName(newName);
            db.userDao().update(loggedInUser);

            runOnUiThread(() -> {
                userNameTextView.setText(newName);
                Toast.makeText(ProfilePage.this, "Name updated!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * Gets the logged-in user's hashed PIN from SharedPreferences.
     */
    private String getSessionPin() {
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        return preferences.getString("loggedInPin", null);
    }
}
