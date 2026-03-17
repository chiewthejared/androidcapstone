package com.example.test_v2.homeIntroLogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.test_v2.R;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.tiles.TileItem;
import com.example.test_v2.tiles.TilesAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {

    private RecyclerView rvTiles;
    private TextView tvName, tvSubtitle;
    private ImageButton btnBack, btnCompose;

    // Room database reference
    private HelperAppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // view bindings (IDs must match home_page.xml). See home_page.xml for exact IDs. :contentReference[oaicite:2]{index=2}
        rvTiles = findViewById(R.id.rv_tiles);
        tvName = findViewById(R.id.tv_name);
        tvSubtitle = findViewById(R.id.tv_sub);
        btnBack = findViewById(R.id.btn_back);
        btnCompose = findViewById(R.id.btn_share);

        // Initialize Room database (do not run DB queries on UI thread)
        database = Room.databaseBuilder(getApplicationContext(),
                HelperAppDatabase.class, "user-database").build();

        // Load logged-in user's display name from SharedPreferences & DB
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String loggedHashedPin = prefs.getString("loggedInPin", null);
        if (loggedHashedPin != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    HelperUserAccount account = database.userDao().getUserByPin(loggedHashedPin);
                    runOnUiThread(() -> {
                        if (account != null) {
                            String displayName = extractDisplayName(account);
                            if (displayName != null && !displayName.isEmpty()) {
                                tvName.setText(displayName);
                            } else {
                                tvName.setText(getString(R.string.john_doe));
                            }
                        } else {
                            tvName.setText(getString(R.string.john_doe));
                        }
                    });
                } catch (Exception e) {
                    // DB access failed for some reason
                    runOnUiThread(() -> tvName.setText(getString(R.string.john_doe)));
                }
            });
        } else {
            tvName.setText(getString(R.string.john_doe));
        }

        // Build tiles list (add "My Profile" as the 8th tile)
        List<TileItem> items = new ArrayList<>();
        items.add(new TileItem(R.drawable.ic_calendar, "Calendar"));
        items.add(new TileItem(R.drawable.ic_notes, "Notes"));
        items.add(new TileItem(R.drawable.ic_medication, "Meds & Equipment"));
        items.add(new TileItem(R.drawable.ic_doctor, "Doctor Info"));
        items.add(new TileItem(R.drawable.ic_analytics, "Analytics"));
        items.add(new TileItem(R.drawable.ic_articles, "Articles & Videos"));
        items.add(new TileItem(R.drawable.ic_timer, "Timer"));
        items.add(new TileItem(R.drawable.ic_profile, "My Profile")); // NEW

        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rvTiles.setLayoutManager(glm);
        rvTiles.setHasFixedSize(true);

        // TilesAdapter: ensure your TilesAdapter has a constructor (List<TileItem>, Context, OnItemClickListener)
        TilesAdapter adapter = new TilesAdapter(items, this, position -> {
            TileItem tile = items.get(position);
            openForTile(tile.getLabel());
        });

        // Optional: if you used the runtime icon resize trick in adapter, it will keep working.
        rvTiles.setAdapter(adapter);

        // subtitle
        tvSubtitle.setText(getString(R.string.subtitle));

        // basic top buttons
        btnBack.setOnClickListener(v -> onBackPressed());
        btnCompose.setOnClickListener(v -> Toast.makeText(this, "Compose/Share clicked", Toast.LENGTH_SHORT).show());

        // LOGOUT button (clears session and returns to Login)
        ImageButton logoutBtn = findViewById(R.id.btn_logout);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                // clear session and return to Login page
                prefs.edit().clear().apply();

                Intent intent = new Intent(HomePage.this, com.example.test_v2.homeIntroLogin.LoginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    /**
     * Extract a displayable name from the HelperUserAccount object using reflection.
     * Tries common getter method names first, then common field names.
     * Returns null if nothing is found.
     */
    private String extractDisplayName(HelperUserAccount account) {
        if (account == null) return null;

        // Try common getter method names
        String[] methodCandidates = {
                "getDisplayName", "getFullName", "getFirstName", "getName",
                "getUsername", "getUserName", "getNickName", "getNick", "getEmail"
        };

        for (String methodName : methodCandidates) {
            try {
                Method m = account.getClass().getMethod(methodName);
                Object value = m.invoke(account);
                if (value instanceof String) {
                    String s = (String) value;
                    if (!s.trim().isEmpty()) return s.trim();
                }
            } catch (NoSuchMethodException ignored) {
                // not present — try next
            } catch (Exception e) {
                // invocation problem — ignore and try next
            }
        }

        // Try common field names if getters fail
        String[] fieldCandidates = {
                "displayName", "fullName", "firstName", "name",
                "username", "userName", "nickName", "nick", "email"
        };

        for (String fieldName : fieldCandidates) {
            try {
                Field f = account.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Object value = f.get(account);
                if (value instanceof String) {
                    String s = (String) value;
                    if (!s.trim().isEmpty()) return s.trim();
                }
            } catch (NoSuchFieldException ignored) {
                // not present — try next
            } catch (Exception e) {
                // access issue — ignore and try next
            }
        }

        return null;
    }

    /**
     * Open an Activity for a given tile label. Uses candidate class names and attempts to start
     * the first valid Activity. If none found, shows a helpful Toast with attempted class names.
     */
    private void openForTile(String label) {
        String[] candidates;

        switch (label) {
            case "Calendar":
                candidates = new String[] {
                        "com.example.test_v2.calendar.CalendarPage",
                        "com.example.test_v2.CalendarPage",
                        "com.example.test_v2.calendar.CalendarActivity"
                };
                break;
            case "Notes":
                candidates = new String[] {
                        "com.example.test_v2.notes.NotesPage",
                        "com.example.test_v2.NotesPage",
                        "com.example.test_v2.notes.NotesActivity"
                };
                break;
            case "Meds & Equipment":
                candidates = new String[] {
                        "com.example.test_v2.medsEquipment.MedsAndEquipmentTrackerPage",
                        "com.example.test_v2.medsEquipment.MedsAndEquipmentTrackerActivity",
                        "com.example.test_v2.medsEquipment.MedsEquipmentPage"
                };
                break;
            case "Doctor Info":
                candidates = new String[] {
                        "com.example.test_v2.doctorinfo.DoctorInfoPage",
                        "com.example.test_v2.doctorinfo.DoctorInfoActivity",
                        "com.example.test_v2.doctor.DoctorInfoPage",
                        "com.example.test_v2.doctorinfo.DoctorInfo"
                };
                break;
            case "Analytics":
                candidates = new String[] {
                        "com.example.test_v2.timer.AnalyticsPage",
                        "com.example.test_v2.analytics.AnalyticsPage",
                        "com.example.test_v2.timer.AnalyticsActivity",
                        "com.example.test_v2.AnalyticsPage"
                };
                break;
            case "Articles & Videos":
                candidates = new String[] {
                        "com.example.test_v2.articlesvideos.ArticlesVideosPage",
                        "com.example.test_v2.articlesvideos.ArticlesVideosActivity",
                        "com.example.test_v2.articlesvideos.ArticlesVideos"
                };
                break;
            case "Timer":
                candidates = new String[] {
                        "com.example.test_v2.timer.TimerPage",
                        "com.example.test_v2.timer.GuestTimerPage",
                        "com.example.test_v2.timer.TimerActivity",
                        "com.example.test_v2.TimerPage"
                };
                break;
            case "My Profile":
                candidates = new String[] {
                        "com.example.test_v2.profile.MyProfilePage",
                        "com.example.test_v2.profile.ProfileActivity",
                        "com.example.test_v2.MyProfilePage"
                };
                break;
            default:
                Toast.makeText(this, "No action defined for: " + label, Toast.LENGTH_SHORT).show();
                return;
        }

        boolean started = tryStartCandidates(candidates);
        if (!started) {
            String tried = android.text.TextUtils.join(", ", candidates);
            Toast.makeText(this,
                    "No matching Activity found for '" + label + "'. Tried: " + tried,
                    Toast.LENGTH_LONG).show();
        }
    }

    /** Try each class name; start the first Activity that exists. */
    private boolean tryStartCandidates(String[] candidates) {
        for (String className : candidates) {
            try {
                Class<?> cls = Class.forName(className);
                Intent i = new Intent(this, cls);
                startActivity(i);
                return true; // success
            } catch (ClassNotFoundException cnf) {
                // not found, try next
            } catch (Exception ex) {
                // class exists but failed to start
                Toast.makeText(this, "Found class but couldn't start: " + className + "\n" + ex.getMessage(),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }
}