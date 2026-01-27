package com.example.test_v2.homeIntroLogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test_v2.AnalyticsPage;
import com.example.test_v2.doctorInfo.DoctorInfoPage;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.NotificationsPage;
import com.example.test_v2.ProfilePage;
import com.example.test_v2.R;
import com.example.test_v2.TimerPage;
import com.example.test_v2.articlesvideos.ArticlesVideosPage;
import com.example.test_v2.calendar.CalendarPage;
import com.example.test_v2.medsEquipment.MedsAndEquipmentTrackerPage;
import com.example.test_v2.notes.NotesPage;

import java.io.File;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {

    private HelperAppDatabase database;
    private TextView userNameTextView;
    private ImageView profilePictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Use the Singleton database instance
        database = HelperAppDatabase.getDatabase(getApplicationContext());

        // Initialize views
        userNameTextView = findViewById(R.id.user_name);
        profilePictureImageView = findViewById(R.id.profile_picture);
        ImageView timerIcon = findViewById(R.id.timer_icon);
        ImageView notificationIcon = findViewById(R.id.notification_icon);

        // Load user data
        loadUserData();

        // Set up tile click listeners
        setupTileClickListeners();

        // Set up logout button listener
        setupLogoutButton();

        // Set up individual button click listeners
        timerIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, TimerPage.class);
            startActivity(intent);
        });

        profilePictureImageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfilePage.class);
            startActivity(intent);
        });

        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsPage.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Fetch the logged-in user's hashed PIN from SharedPreferences
            String hashedPin = getSessionPin();
            if (hashedPin != null) {
                // Fetch the user from the database using the hashed PIN
                HelperUserAccount loggedInUser = database.userDao().getUserByPin(hashedPin);

                runOnUiThread(() -> {
                    if (loggedInUser != null) {
                        // Set user name
                        userNameTextView.setText(loggedInUser.getFullName());

                        // Set profile picture
                        if (loggedInUser.getProfileImagePath() != null && !loggedInUser.getProfileImagePath().equals("ic_person")) {
                            File imageFile = new File(loggedInUser.getProfileImagePath());
                            if (imageFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                profilePictureImageView.setImageBitmap(bitmap);
                            } else {
                                profilePictureImageView.setImageResource(R.drawable.ic_person); // Default profile picture
                            }
                        } else {
                            profilePictureImageView.setImageResource(R.drawable.ic_person); // Default profile picture
                        }
                    }
                });
            }
        });
    }

    public String getSessionPin() {

        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        return preferences.getString("loggedInPin", null); // Retrieve the stored hashed PIN
    }

    private void setupTileClickListeners() {
        findViewById(R.id.view_calendar_tile).setOnClickListener(v -> startActivity(new Intent(this, CalendarPage.class)));
        findViewById(R.id.add_notes_tile).setOnClickListener(v -> startActivity(new Intent(this, NotesPage.class)));
        findViewById(R.id.medication_tracker_tile).setOnClickListener(v -> startActivity(new Intent(this, MedsAndEquipmentTrackerPage.class)));
        findViewById(R.id.doctor_info_tile).setOnClickListener(v -> startActivity(new Intent(this, DoctorInfoPage.class)));
        findViewById(R.id.analytics_tile).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsPage.class)));
        findViewById(R.id.articles_videos_tile).setOnClickListener(v -> startActivity(new Intent(this, ArticlesVideosPage.class)));
        findViewById(R.id.timer_tile).setOnClickListener(v -> startActivity(new Intent(this, TimerPage.class)));
    }

    private void setupLogoutButton() {
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            // Clear the stored PIN in SharedPreferences
            SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            preferences.edit().remove("loggedInPin").apply();

            // Navigate back to the login page
            Intent intent = new Intent(HomePage.this, LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close the current activity
        });
    }
}
