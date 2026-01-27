package com.example.test_v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.timer.HelperTimerEvent;
import com.example.test_v2.TimerHistoryAdapter;
import com.example.test_v2.R;

import java.util.ArrayList;
import java.util.List;

public class TimerHistoryPage extends AppCompatActivity implements TimerHistoryAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private TimerHistoryAdapter adapter;
    private HelperAppDatabase db;
    private String currentUserId = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_history_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());


        currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("loggedInPin", "-1");

        Button backButton = findViewById(R.id.back_button_history);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.timer_history_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TimerHistoryAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {

            List<HelperTimerEvent> list = db.timerEventDao().getAllEventsForUser(currentUserId);
            runOnUiThread(() -> adapter.setEvents(list));
        }).start();
    }

    @Override
    public void onEdit(HelperTimerEvent event) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add/Edit Notes");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_notes, null);
        builder.setView(dialogView);

        EditText notesEditText = dialogView.findViewById(R.id.notes_edit_text);
        notesEditText.setText(event.notes);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newNotes = notesEditText.getText().toString().trim();
            event.notes = newNotes;

            new Thread(() -> {
                db.timerEventDao().update(event);
                runOnUiThread(() -> {
                    Toast.makeText(TimerHistoryPage.this, "Notes updated", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onDelete(HelperTimerEvent event) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Yes", (d, w) -> {
                    new Thread(() -> {
                        db.timerEventDao().deleteById(event.id);
                        runOnUiThread(() -> {
                            Toast.makeText(TimerHistoryPage.this, "Timer record deleted", Toast.LENGTH_SHORT).show();
                            loadData();
                        });
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
