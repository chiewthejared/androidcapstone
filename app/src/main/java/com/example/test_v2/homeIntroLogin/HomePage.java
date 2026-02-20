package com.example.test_v2.homeIntroLogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.example.test_v2.tiles.TileItem;
import com.example.test_v2.tiles.TilesAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    private RecyclerView rvTiles;
    private TextView tvName, tvSubtitle;
    private ImageButton btnBack, btnCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        rvTiles = findViewById(R.id.rv_tiles);
        tvName = findViewById(R.id.tv_name);
        tvSubtitle = findViewById(R.id.tv_sub);
        btnBack = findViewById(R.id.btn_back);
        btnCompose = findViewById(R.id.btn_share);

        // Populate tiles list — drawable names must exist in res/drawable
        List<TileItem> items = new ArrayList<>();
        items.add(new TileItem(R.drawable.ic_calendar, "Calendar"));
        items.add(new TileItem(R.drawable.ic_notes, "Notes"));
        items.add(new TileItem(R.drawable.ic_medication, "Meds & Equipment"));
        items.add(new TileItem(R.drawable.ic_doctor, "Doctor Info"));
        items.add(new TileItem(R.drawable.ic_analytics, "Analytics"));
        items.add(new TileItem(R.drawable.ic_articles, "Articles & Videos"));
        items.add(new TileItem(R.drawable.ic_timer, "Timer"));

        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rvTiles.setLayoutManager(glm);
        rvTiles.setHasFixedSize(true);
        rvTiles.setLayoutManager(glm);

        TilesAdapter adapter = new TilesAdapter(items, this, position -> {
            TileItem tile = items.get(position);
            openForTile(tile.getLabel());
        });
        rvTiles.setAdapter(adapter);

        tvName.setText(getString(R.string.john_doe));
        tvSubtitle.setText(getString(R.string.subtitle));

        btnBack.setOnClickListener(v -> onBackPressed());
        btnCompose.setOnClickListener(v -> {
            // Placeholder: replace with real share/compose action if desired
            Toast.makeText(this, "Compose/Share clicked", Toast.LENGTH_SHORT).show();
        });
    }

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
            default:
                Toast.makeText(this, "No action defined for: " + label, Toast.LENGTH_SHORT).show();
                return;
        }

        // Try each candidate until one works
        boolean started = tryStartCandidates(candidates);
        if (!started) {
            // helpful message listing tried class names (so you can copy/paste and find the real one)
            String tried = android.text.TextUtils.join(", ", candidates);
            Toast.makeText(this,
                    "No matching Activity found for '" + label + "'. Tried: " + tried,
                    Toast.LENGTH_LONG).show();
        }
    }

    /** Helper: try each class name, start the first one that exists. */
    private boolean tryStartCandidates(String[] candidates) {
        for (String className : candidates) {
            try {
                Class<?> cls = Class.forName(className);
                Intent i = new Intent(this, cls);
                startActivity(i);
                return true; // success
            } catch (ClassNotFoundException cnf) {
                // not present — try next
            } catch (Exception ex) {
                // class found but couldn't start (not an Activity or other issue)
                Toast.makeText(this, "Found class but couldn't start: " + className + "\n" + ex.getMessage(),
                        Toast.LENGTH_LONG).show();
                return true; // stop trying further because class exists but failed to start
            }
        }
        return false; // none worked
    }
}