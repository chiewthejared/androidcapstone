package com.example.test_v2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.timer.HelperTimerEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TimerPage activity — unified, compiled version.
 * Uses the layout ids:
 *  - R.id.timer_display
 *  - R.id.play_pause_button
 *  - R.id.stop_button
 *  - R.id.back_button
 *  - R.id.history_button
 *  - R.id.action_log_scroll
 *  - R.id.action_log_container
 *
 * Keeps the original timer logic, intervals persistence and DB saving (finalizeAndSave()).
 */
public class TimerPage extends AppCompatActivity {

    // UI elements
    private ImageButton backButton;
    private TextView timerDisplay;
    // image buttons for play/pause and stop
    private ImageButton playPauseBtn;
    private ImageButton stopBtn;
    // history button (text)
    private Button historyButton;

    private ScrollView actionLogScroll;
    private LinearLayout actionLogContainer;

    // Timer state
    private boolean isRunning = false;
    private boolean hasEverRun = false;

    // real-time storing
    private long baseTimeMs = 0L; // Real time at last start/resume
    private long totalActiveBeforeResume = 0L;

    // For logging intervals: [startRealMs, endRealMs], using currentTimeMillis
    private List<long[]> intervals = new ArrayList<>();

    // Persistence keys
    private static final String PREFS_TIMER = "timer_prefs_v1";
    private static final String KEY_INTERVALS_JSON = "intervals_json";
    private static final String KEY_TOTAL_ACTIVE = "total_active_before_resume";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_HAS_EVER_RUN = "has_ever_run";
    private static final String KEY_LAST_RESUME_MS = "last_resume_ms";

    private SharedPreferences prefs;
    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // measure run intervals
    private long lastResumeMs = 0L;
    private long lastPauseMs = 0L;

    // store user actions (start/pause/resume) messages
    private ArrayList<String> actionLogMessages = new ArrayList<>();

    // update the display ~30 times/second
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long elapsedActiveMs = getTotalActiveMs();
                timerDisplay.setText(formatElapsed(elapsedActiveMs));
                timerHandler.postDelayed(this, 33L);
            }
        }
    };

    // Database
    private HelperAppDatabase db;
    private String currentUserId = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());

        // get the current user from SharedPreferences (use a local variable — do NOT shadow the class field)
        SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = sessionPrefs.getString("loggedInPin", "-1");

        // Find UI (match ids from timer_page.xml)
        timerDisplay = findViewById(R.id.timer_display);

        // ImageButtons
        playPauseBtn = findViewById(R.id.play_pause_button);
        stopBtn = findViewById(R.id.stop_button);

        // Back and History
        backButton = findViewById(R.id.back_button);
        historyButton = findViewById(R.id.history_button);

        // Hidden action log references (may be gone visually, but kept for code)
        actionLogScroll = findViewById(R.id.action_log_scroll);
        actionLogContainer = findViewById(R.id.action_log_container);

        // Initialize display text
        timerDisplay.setText(getString(R.string.timer_default_text)); // use string resource

        // Wire UI events

        // Back = finish
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Play / Pause toggle
        if (playPauseBtn != null) {
            playPauseBtn.setOnClickListener(v -> {
                if (!hasEverRun) {
                    // start
                    startTimer();
                    hasEverRun = true;
                    // show pause icon if you added it
                    try {
                        playPauseBtn.setImageResource(R.drawable.ic_pause_white);
                    } catch (Exception ignored) {}
                    appendActionLog("Timer started at " + getCurrentTimeString());
                } else if (isRunning) {
                    // pause
                    pauseTimer();
                    try {
                        playPauseBtn.setImageResource(R.drawable.ic_play_white);
                    } catch (Exception ignored) {}
                    appendActionLog("Timer paused at " + getCurrentTimeString());
                } else {
                    // resume
                    resumeTimer();
                    try {
                        playPauseBtn.setImageResource(R.drawable.ic_pause_white);
                    } catch (Exception ignored) {}
                    appendActionLog("Timer resumed at " + getCurrentTimeString());
                }
            });
        }

        // Stop/Save button: call existing finalizeAndSave()
        if (stopBtn != null) {
            stopBtn.setOnClickListener(v -> {
                if (isRunning) {
                    pauseTimer();
                    if (playPauseBtn != null) {
                        try { playPauseBtn.setImageResource(R.drawable.ic_play_white); } catch (Exception ignored) {}
                    }
                }
                finalizeAndSave();
            });
        }

        // History button: open TimerHistoryPage
        if (historyButton != null) {
            historyButton.setOnClickListener(v ->
                    startActivity(new Intent(this, TimerHistoryPage.class))
            );
        }

        // Initialize class prefs field
        prefs = getSharedPreferences(PREFS_TIMER, MODE_PRIVATE);

        // Restore state (do this after prefs is initialized)
        restoreTimerState();
    }

    private void startTimer() {
        if (!isRunning && intervals.isEmpty()) {
            isRunning = true;
            hasEverRun = true;

            long now = System.currentTimeMillis();
            baseTimeMs = now;
            totalActiveBeforeResume = 0L;

            lastResumeMs = now;
            lastPauseMs = 0L;

            // store real start in intervals
            intervals.add(new long[]{ now, -1 });

            timerHandler.post(timerRunnable);
        } else if (!isRunning && hasEverRun) {
            // start/resume when there are previous intervals
            isRunning = true;
            long now = System.currentTimeMillis();
            baseTimeMs = now;
            lastResumeMs = now;
            intervals.add(new long[]{ now, -1 });
            timerHandler.post(timerRunnable);
        }
        saveTimerState();
        refreshIntervalsUI();
    }

    private void pauseTimer() {
        if (isRunning) {
            isRunning = false;
            long now = System.currentTimeMillis();
            // close out last active interval
            if (!intervals.isEmpty()) {
                intervals.get(intervals.size() - 1)[1] = now;
            }
            totalActiveBeforeResume += (now - baseTimeMs);
            lastPauseMs = now;
            timerHandler.removeCallbacks(timerRunnable);
        }
        saveTimerState();
        refreshIntervalsUI();
    }

    private void resumeTimer() {
        if (!isRunning && hasEverRun) {
            isRunning = true;
            long now = System.currentTimeMillis();
            baseTimeMs = now;
            lastResumeMs = now;

            intervals.add(new long[]{ now, -1 });
            timerHandler.post(timerRunnable);
        }
        saveTimerState();
        refreshIntervalsUI();
    }

    private void finalizeAndSave() {
        if (isRunning) {
            pauseTimer();
        }
        removeEmptyInterval();

        long totalActiveMs = totalActiveBeforeResume;
        if (totalActiveMs <= 0) {
            Toast.makeText(this, "No time recorded", Toast.LENGTH_SHORT).show();
            resetTimer();
            return;
        }

        appendActionLog("Event ended at " + getCurrentTimeString()
                + ", total " + formatElapsed(totalActiveMs));

        // Combine action logs
        StringBuilder sb = new StringBuilder();
        for (String line : actionLogMessages) {
            sb.append(line).append("\n");
        }
        String finalActionLog = sb.toString().trim();

        String intervalsJson = buildIntervalsJson(intervals);

        // Insert into DB
        new Thread(() -> {
            try {
                HelperTimerEvent event = new HelperTimerEvent();
                event.userId = currentUserId;
                event.startTimestamp = (intervals.size() > 0) ? intervals.get(0)[0] : System.currentTimeMillis();
                event.totalTimeMs = totalActiveMs;
                event.intervalsJson = intervalsJson;
                event.notes = "";
                event.actionLog = finalActionLog;
                event.eventName = "Timer Event"; // default name if not renamed

                db.timerEventDao().insert(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        Toast.makeText(this, "Timer saved (" + formatElapsed(totalActiveMs) + ")", Toast.LENGTH_SHORT).show();
        resetTimer();
    }

    private void removeEmptyInterval() {
        if (!intervals.isEmpty()) {
            long[] last = intervals.get(intervals.size() - 1);
            if (last[1] < 0) {
                intervals.remove(intervals.size() - 1);
            }
        }
    }

    private String buildIntervalsJson(List<long[]> intervals) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < intervals.size(); i++) {
            long[] iv = intervals.get(i);
            sb.append("{\"start\":").append(iv[0])
                    .append(",\"end\":").append(iv[1]).append("}");
            if (i < intervals.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatElapsed(long ms) {
        long totalMs = ms;
        long sec = totalMs / 1000;
        long milli = totalMs % 1000;
        long min = sec / 60;
        long hrs = min / 60;
        sec = sec % 60;
        min = min % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%03d", hrs, min, sec, milli);
    }

    private void resetTimer() {
        timerHandler.removeCallbacks(timerRunnable);

        isRunning = false;
        hasEverRun = false;
        baseTimeMs = 0L;
        totalActiveBeforeResume = 0L;
        lastResumeMs = 0L;
        lastPauseMs = 0L;

        intervals.clear();
        actionLogMessages.clear();

        timerDisplay.setText(getString(R.string.timer_default_text));
        try {
            if (playPauseBtn != null) playPauseBtn.setImageResource(R.drawable.ic_play_white);
        } catch (Exception ignored) {}
        if (actionLogContainer != null) actionLogContainer.removeAllViews();
        clearSavedTimerState();
    }

    private void appendActionLog(String msg) {
        actionLogMessages.add(msg);
        if (actionLogContainer != null) {
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextSize(14f);
            tv.setTextColor(Color.WHITE);
            actionLogContainer.addView(tv);
        }
        if (actionLogScroll != null) {
            actionLogScroll.post(() -> actionLogScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private String getCurrentTimeString() {
        // This is just for display, no effect on intervals
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If app resumes and was running, ensure handler is active
        if (isRunning) startHandlerIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTimerState();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void saveTimerState() {
        try {
            JSONArray arr = new JSONArray();
            for (long[] iv : intervals) {
                JSONObject o = new JSONObject();
                o.put("start", iv[0]);
                o.put("end", iv[1]);
                arr.put(o);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_INTERVALS_JSON, arr.toString());
            editor.putLong(KEY_TOTAL_ACTIVE, totalActiveBeforeResume);
            editor.putBoolean(KEY_IS_RUNNING, isRunning);
            editor.putBoolean(KEY_HAS_EVER_RUN, hasEverRun);
            editor.putLong(KEY_LAST_RESUME_MS, lastResumeMs);
            editor.apply();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void restoreTimerState() {
        try {
            String json = prefs.getString(KEY_INTERVALS_JSON, null);
            intervals.clear();
            if (json != null) {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    long s = o.optLong("start", -1L);
                    long e = o.optLong("end", -1L);
                    intervals.add(new long[]{s, e});
                }
            }

            totalActiveBeforeResume = prefs.getLong(KEY_TOTAL_ACTIVE, 0L);
            isRunning = prefs.getBoolean(KEY_IS_RUNNING, false);
            hasEverRun = prefs.getBoolean(KEY_HAS_EVER_RUN, false);
            lastResumeMs = prefs.getLong(KEY_LAST_RESUME_MS, 0L);

            // If it was running when persisted, restart the handler using lastResumeMs
            if (isRunning) {
                startHandlerIfNeeded();
                appendActionLog("Restored running timer from saved state (" + formatElapsed(getTotalActiveMs()) + ")");
                try {
                    if (playPauseBtn != null) playPauseBtn.setImageResource(R.drawable.ic_pause_white);
                } catch (Exception ignored) {}
            } else {
                try {
                    if (playPauseBtn != null) playPauseBtn.setImageResource(R.drawable.ic_play_white);
                } catch (Exception ignored) {}
            }
            // update UI list of intervals
            refreshIntervalsUI();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private long getTotalActiveMs() {
        long total = totalActiveBeforeResume;
        if (isRunning) {
            total += (System.currentTimeMillis() - lastResumeMs);
        }
        return total;
    }

    private void startHandlerIfNeeded() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.post(timerRunnable);
        }
    }

    // Show a dialog listing current intervals with options to add / remove
    private void showIntervalsDialog() {
        final ArrayList<String> displayList = new ArrayList<>();
        for (int i = 0; i < intervals.size(); i++) {
            long[] iv = intervals.get(i);
            long s = iv[0];
            long e = iv[1];
            String startStr = (s > 0) ? logDateFormat.format(new Date(s)) : "unknown";
            String durStr = (e > 0) ? formatElapsed(e - s) : (isRunning && i == intervals.size() - 1 ? "running..." : "open");
            displayList.add(String.format(Locale.getDefault(), "%d) %s — %s", i+1, startStr, durStr));
        }
        if (displayList.isEmpty()) displayList.add("No intervals recorded");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        ListView lv = new ListView(this);
        lv.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Intervals")
                .setView(lv)
                .setPositiveButton("Add interval", (dialog, which) -> showAddIntervalDialog())
                .setNegativeButton("Close", null);

        AlertDialog dialog = builder.create();

        // Long-click to remove item
        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            if (intervals.isEmpty()) return true;
            int idx = position;
            // Confirm deletion
            new AlertDialog.Builder(this)
                    .setTitle("Delete interval")
                    .setMessage("Delete interval " + (idx+1) + "?")
                    .setPositiveButton("Delete", (d, w) -> {
                        removeIntervalAt(idx);
                        dialog.dismiss();
                        showIntervalsDialog();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        dialog.show();
    }

    private void showAddIntervalDialog() {
        // Step 1: pick date via DatePickerDialog
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // default start at 9:00 AM (you can change)
                    picked.set(Calendar.HOUR_OF_DAY, 9);
                    picked.set(Calendar.MINUTE, 0);
                    picked.set(Calendar.SECOND, 0);
                    long startMs = picked.getTimeInMillis();

                    // Step 2: ask for duration in minutes (EditText dialog)
                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setHint("Duration minutes (e.g. 30)");
                    new AlertDialog.Builder(this)
                            .setTitle("Duration")
                            .setView(input)
                            .setPositiveButton("Add", (d, w) -> {
                                String sVal = input.getText().toString();
                                if (sVal.isEmpty()) return;
                                try {
                                    long minutes = Long.parseLong(sVal);
                                    long endMs = startMs + minutes * 60L * 1000L;
                                    addIntervalManual(startMs, endMs);
                                    appendActionLog("Manually added interval: " + logDateFormat.format(new Date(startMs)) + " — " + formatElapsed(endMs - startMs));
                                } catch (NumberFormatException ex) {
                                    // ignore or show toast
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void addIntervalManual(long startMs, long endMs) {
        if (startMs <= 0 || endMs <= startMs) return;
        intervals.add(new long[]{startMs, endMs});
        // update totalActiveBeforeResume so it counts in totals if not running
        totalActiveBeforeResume += (endMs - startMs);
        hasEverRun = true;
        saveTimerState();
        refreshIntervalsUI();
    }

    private void removeIntervalAt(int idx) {
        if (idx < 0 || idx >= intervals.size()) return;
        long[] iv = intervals.remove(idx);
        long delta = (iv[1] > 0 && iv[0] > 0) ? Math.max(0, iv[1] - iv[0]) : 0;
        totalActiveBeforeResume = Math.max(0, totalActiveBeforeResume - delta);
        saveTimerState();
        refreshIntervalsUI();
        appendActionLog("Removed interval " + (idx+1));
    }

    private void refreshIntervalsUI() {
        // If you have a UI container for listing intervals, update it; otherwise update actionLog with summary
        // We'll append a summary entry at top of action log
        if (actionLogContainer == null) return;
        // Remove any previous contents and re-add actionLogMessages only
        actionLogContainer.removeAllViews();
        for (String msg : actionLogMessages) {
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextSize(12f);
            tv.setTextColor(Color.WHITE);
            actionLogContainer.addView(tv);
        }

        // NOTE: intentionally **not** adding an "Intervals (...)" header nor interval detail rows
        // This removes the "Intervals (1):" text you saw.

        // scroll to bottom (existing method you have)
        final ScrollView sv = actionLogScroll;
        if (sv != null) {
            sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void clearSavedTimerState() {
        prefs.edit().clear().apply();
    }
}
