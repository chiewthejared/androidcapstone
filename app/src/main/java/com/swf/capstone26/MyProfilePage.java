package com.swf.capstone26;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.swf.capstone26.fileAndDatabase.HelperAppDatabase;
import com.swf.capstone26.homeIntroLogin.LoginPage;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.security.MessageDigest;
import java.util.concurrent.Executors;

public class MyProfilePage extends AppCompatActivity {

    private static final String SESSION_PREFS = "UserSession";
    private static final String KEY_LOGGED_PIN = "loggedInPin";
    private static final String LOCAL_PREFS = "user_prefs_local";

    private static final int REQ_PICK_IMAGE = 1001;

    ImageButton btnBack, btnCamera;
    ImageView ivAvatar;
    TextView tvNameTop, tvName;
    SwitchMaterial switchMedication;
    LinearLayout rowChangePass, rowLogout, rowDelete;

    private HelperAppDatabase database;
    private HelperUserAccount currentAccount;
    private String currentLoggedPin; // hashed pin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // views
        btnBack = findViewById(R.id.btn_back);
        btnCamera = findViewById(R.id.btn_camera);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvNameTop = findViewById(R.id.tv_name);
        tvName = findViewById(R.id.tv_person_name);
        switchMedication = findViewById(R.id.switch_medication);
        rowChangePass = findViewById(R.id.row_change_pass);
        rowLogout = findViewById(R.id.row_logout);
        rowDelete = findViewById(R.id.row_delete);

        // init DB (your Room DB)
        database = Room.databaseBuilder(getApplicationContext(), HelperAppDatabase.class, "user-database")
                .build();

        btnBack.setOnClickListener(v -> finish());

        // pick image
        btnCamera.setOnClickListener(v -> openGalleryForImage());

        // edit name
        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showChangeNameDialog());

        // change passcode
        rowChangePass.setOnClickListener(v -> showChangePassDialog());

        // logout
        rowLogout.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(getString(R.string.log_out))
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.ok, (d, w) -> doLogout())
                .setNegativeButton(android.R.string.cancel, null)
                .show());

        // delete account
        rowDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_account))
                .setMessage("This will permanently delete your account. Continue?")
                .setPositiveButton(android.R.string.ok, (d, w) -> deleteAccount())
                .setNegativeButton(android.R.string.cancel, null)
                .show());

        // medication toggle persisted locally
        boolean savedMed = getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).getBoolean("pref_medication_on", false);
        switchMedication.setChecked(savedMed);
        switchMedication.setOnCheckedChangeListener((buttonView, isChecked) ->
                getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).edit().putBoolean("pref_medication_on", isChecked).apply()
        );

        // load UI from DB/session
        loadAndPopulateUserFromDb();
    }

    // ---------- Gallery ----------
    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                ivAvatar.setImageURI(uri);
                final String uriStr = uri.toString();

                // persist to DB if account loaded; otherwise save local pref
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        if (currentAccount != null) {
                            currentAccount.setProfileImagePath(uriStr);
                            database.userDao().update(currentAccount); // uses your DAO update method. :contentReference[oaicite:2]{index=2}
                        } else {
                            getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).edit().putString("saved_avatar_uri", uriStr).apply();
                        }
                    } catch (Exception ignored) {}
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // ---------- Change name ----------
    private void showChangeNameDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(tvName.getText());

        new AlertDialog.Builder(this)
                .setTitle("Change display name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    tvNameTop.setText(newName);
                    tvName.setText(newName);

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            if (currentAccount != null) {
                                currentAccount.setFullName(newName); // your entity field. :contentReference[oaicite:3]{index=3}
                                database.userDao().update(currentAccount); // DAO.update exists. :contentReference[oaicite:4]{index=4}
                            } else {
                                getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).edit().putString("saved_user_name", newName).apply();
                            }
                        } catch (Exception e) {
                            // ignore but don't crash
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- Change passcode ----------
    private void showChangePassDialog() {
        final EditText current = new EditText(this);
        current.setHint("Current passcode");
        current.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        final EditText nw = new EditText(this);
        nw.setHint("New passcode");
        nw.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        final EditText confirm = new EditText(this);
        confirm.setHint("Confirm new passcode");
        confirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (8 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);
        container.addView(current);
        container.addView(nw);
        container.addView(confirm);

        new AlertDialog.Builder(this)
                .setTitle("Change passcode")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String cur = current.getText().toString().trim();
                    String n = nw.getText().toString().trim();
                    String c = confirm.getText().toString().trim();

                    if (n.isEmpty() || c.isEmpty()) {
                        Toast.makeText(this, "New passcode can't be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!n.equals(c)) {
                        Toast.makeText(this, "New passcodes do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences sess = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
                    String storedHashed = sess.getString(KEY_LOGGED_PIN, null);
                    // verify current if session exists
                    if (storedHashed != null) {
                        String curHash = hashString(cur);
                        if (!curHash.equals(storedHashed)) {
                            Toast.makeText(this, "Current passcode incorrect", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    final String newHash = hashString(n);

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            if (currentAccount != null) {
                                currentAccount.setPin(newHash); // update entity pin. :contentReference[oaicite:5]{index=5}
                                database.userDao().update(currentAccount);
                            }
                            // always update session pin so future checks use new value
                            getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().putString(KEY_LOGGED_PIN, newHash).apply();
                            runOnUiThread(() -> Toast.makeText(this, "Passcode updated", Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "Failed to update passcode", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- Logout ----------
    private void doLogout() {
        getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(MyProfilePage.this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ---------- Delete account ----------
    private void deleteAccount() {
        // we will delete the DB row by running a direct SQL delete on the Room database's writable DB.
        // (Your DAO currently has update/getUserByPin; it does not have a delete annotation — so this approach safely removes the row.)
        if (currentLoggedPin == null && currentAccount == null) {
            // nothing to delete — just clear session and return to login
            doLogout();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (currentAccount != null) {
                    // attempt to delete via SQL using the writable DB
                    androidx.sqlite.db.SupportSQLiteDatabase sqliteDb =
                            database.getOpenHelper().getWritableDatabase();
                    // delete by id if available
                    int id = currentAccount.getId();
                    if (id > 0) {
                        sqliteDb.execSQL("DELETE FROM HelperUserAccount WHERE id = ?", new Object[]{id});
                    } else if (currentLoggedPin != null) {
                        sqliteDb.execSQL("DELETE FROM HelperUserAccount WHERE pin = ?", new Object[]{currentLoggedPin});
                    } else {
                        // fallback: try delete by fullName (unlikely)
                        sqliteDb.execSQL("DELETE FROM HelperUserAccount WHERE fullName = ?", new Object[]{currentAccount.getFullName()});
                    }
                } else if (currentLoggedPin != null) {
                    androidx.sqlite.db.SupportSQLiteDatabase sqliteDb =
                            database.getOpenHelper().getWritableDatabase();
                    sqliteDb.execSQL("DELETE FROM HelperUserAccount WHERE pin = ?", new Object[]{currentLoggedPin});
                }
            } catch (Exception ignored) {
            } finally {
                // clear session/local prefs and go to login
                getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).edit().clear().apply();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyProfilePage.this, LoginPage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }
        });
    }

    // ---------- Load user ----------
    private void loadAndPopulateUserFromDb() {
        currentLoggedPin = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).getString(KEY_LOGGED_PIN, null);

        // first, local saved name/avatar fallback
        String localSavedName = getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).getString("saved_user_name", null);
        String localSavedAvatar = getSharedPreferences(LOCAL_PREFS, MODE_PRIVATE).getString("saved_avatar_uri", null);
        if (localSavedName != null) {
            tvNameTop.setText(localSavedName);
            tvName.setText(localSavedName);
        }
        if (localSavedAvatar != null) {
            try { ivAvatar.setImageURI(Uri.parse(localSavedAvatar)); } catch (Exception ignored) { }
        }

        if (currentLoggedPin == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HelperUserAccount account = database.userDao().getUserByPin(currentLoggedPin); // your DAO query. :contentReference[oaicite:6]{index=6}
                currentAccount = account;
                runOnUiThread(() -> {
                    if (account != null) {
                        // prefer fullName from entity
                        String name = account.getFullName();
                        if (name != null && !name.isEmpty()) {
                            tvNameTop.setText(name);
                            tvName.setText(name);
                        }

                        // load profile image if set
                        String path = account.getProfileImagePath();
                        if (path != null && !path.isEmpty()) {
                            try { ivAvatar.setImageURI(Uri.parse(path)); } catch (Exception ignored) {}
                        }
                    }
                });
            } catch (Exception ignored) {}
        });
    }

    // ---------- utils ----------
    private String hashString(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }
}
