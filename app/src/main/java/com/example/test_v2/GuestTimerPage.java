package com.example.test_v2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
 * GuestTimerPage — uses res/layout/guest_timer_page.xml
 * Same timer behavior as TimerPage but:
 *  - Uses a guest-only layout (no History button)
 *  - Does NOT insert events into DB (guest)
 */
public class GuestTimerPage extends AppCompatActivity {

    // UI elements
    private ImageButton backButton;
    private TextView timerDisplay;
    private Button playPauseBtn;
    private Button stopBtn;
    // No historyButton here (guest has no history)

    private ScrollView actionLogScroll;
    private LinearLayout actionLogContainer;

    // Timer state
    private boolean isRunning = false;
    private boolean hasEverRun = false;

    // real-time storing
    private long baseTimeMs = 0L;
    private long totalActiveBeforeResume = 0L;

    // For logging intervals
    private List<long[]> intervals = new ArrayList<>();

    // Persistence keys (kept same so restore/save works separately)
    private static final String PREFS_TIMER = "timer_prefs_v1";
    private static final String KEY_INTERVALS_JSON = "intervals_json";
    private static final String KEY_TOTAL_ACTIVE = "total_active_before_resume";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_HAS_EVER_RUN = "has_ever_run";
    private static final String KEY_LAST_RESUME_MS = "last_resume_ms";

    private SharedPreferences prefs;
    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private long lastResumeMs = 0L;
    private long lastPauseMs = 0L;

    private ArrayList<String> actionLogMessages = new ArrayList<>();

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

    // Database reference (kept but guests won't write)
    private HelperAppDatabase db;
    private String currentUserId = "guest"; // forced guest

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_timer_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());

        // Force guest id for this activity
        currentUserId = "guest";

        // Find UI (IDs kept to match TimerPage)
        timerDisplay = findViewById(R.id.timer_display);
        playPauseBtn = findViewById(R.id.play_pause_button);
        stopBtn = findViewById(R.id.stop_button);
        backButton = findViewById(R.id.back_button);

        actionLogScroll = findViewById(R.id.action_log_scroll);
        actionLogContainer = findViewById(R.id.action_log_container);

        // Initialize display
        timerDisplay.setText(getString(R.string.timer_default_text));

        // Make sure the Start/Stop drawables and text colors match the main timer page
        try {
            playPauseBtn.setBackgroundResource(R.drawable.circle_button_bg_timer);
            stopBtn.setBackgroundResource(R.drawable.circle_button_outline);

            // cancel theme tints
            playPauseBtn.setBackgroundTintList(null);
            stopBtn.setBackgroundTintList(null);

            // explicit text colors
            playPauseBtn.setTextColor(android.graphics.Color.WHITE);
            stopBtn.setTextColor(android.graphics.Color.BLACK);
        } catch (Exception ignored) {}

        // Wire UI events

        if (backButton != null) backButton.setOnClickListener(v -> finish());

        if (playPauseBtn != null) {
            playPauseBtn.setOnClickListener(v -> {
                if (!hasEverRun) {
                    startTimer();
                    hasEverRun = true;
                    try { playPauseBtn.setText("Pause"); } catch (Exception ignored) {}
                    appendActionLog("Timer started at " + getCurrentTimeString());
                } else if (isRunning) {
                    pauseTimer();
                    try { playPauseBtn.setText("Start"); } catch (Exception ignored) {}
                    appendActionLog("Timer paused at " + getCurrentTimeString());
                } else {
                    resumeTimer();
                    try { playPauseBtn.setText("Pause"); } catch (Exception ignored) {}
                    appendActionLog("Timer resumed at " + getCurrentTimeString());
                }
            });
        }

        if (stopBtn != null) {
            stopBtn.setOnClickListener(v -> {
                if (isRunning) {
                    pauseTimer();
                    if (playPauseBtn != null) {
                        try { playPauseBtn.setText("Start"); } catch (Exception ignored) {}
                    }
                }
                finalizeAndSave(); // for guest, this will NOT write to DB
            });
        }

        // Initialize prefs
        prefs = getSharedPreferences(PREFS_TIMER, MODE_PRIVATE);

        // default button labels
        if (playPauseBtn != null) playPauseBtn.setText("Start");
        if (stopBtn != null) stopBtn.setText("Stop");

        // restore saved state
        restoreTimerState();
    }

    // Timer control methods (same semantics as TimerPage)
    private void startTimer() {
        if (!isRunning && intervals.isEmpty()) {
            isRunning = true;
            hasEverRun = true;

            long now = System.currentTimeMillis();
            baseTimeMs = now;
            totalActiveBeforeResume = 0L;

            lastResumeMs = now;
            lastPauseMs = 0L;

            intervals.add(new long[]{ now, -1 });
            timerHandler.post(timerRunnable);
        } else if (!isRunning && hasEverRun) {
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
        if (isRunning) pauseTimer();
        removeEmptyInterval();

        long totalActiveMs = totalActiveBeforeResume;
        if (totalActiveMs <= 0) {
            Toast.makeText(this, "No time recorded", Toast.LENGTH_SHORT).show();
            resetTimer();
            return;
        }

        appendActionLog("Event ended at " + getCurrentTimeString()
                + ", total " + formatElapsed(totalActiveMs));

        StringBuilder sb = new StringBuilder();
        for (String line : actionLogMessages) sb.append(line).append("\n");
        String finalActionLog = sb.toString().trim();
        String intervalsJson = buildIntervalsJson(intervals);

        // Guest: do NOT insert into DB. Show toast only (optionally persist elsewhere).
        Toast.makeText(this, "Guest timer finished (" + formatElapsed(totalActiveMs) + "). Not saved to DB.", Toast.LENGTH_LONG).show();

        resetTimer();
    }

    private void removeEmptyInterval() {
        if (!intervals.isEmpty()) {
            long[] last = intervals.get(intervals.size() - 1);
            if (last[1] < 0) intervals.remove(intervals.size() - 1);
        }
    }

    private String buildIntervalsJson(List<long[]> intervals) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < intervals.size(); i++) {
            long[] iv = intervals.get(i);
            sb.append("{\"start\":").append(iv[0]).append(",\"end\":").append(iv[1]).append("}");
            if (i < intervals.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Format elapsed ms as HH:MM:SS.CS (centiseconds with dot) -> 00:00:00.00
     */
    private String formatElapsed(long ms) {
        long totalMs = ms;
        long totalSec = totalMs / 1000L;
        long msRemainder = totalMs % 1000L;
        long centis = msRemainder / 10L;

        long secs = totalSec % 60L;
        long totalMin = totalSec / 60L;
        long mins = totalMin % 60L;
        long hrs = totalMin / 60L;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", hrs, mins, secs, centis);
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
        try { if (playPauseBtn != null) playPauseBtn.setText("Start"); } catch (Exception ignored) {}
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
        if (actionLogScroll != null) actionLogScroll.post(() -> actionLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            if (isRunning) {
                startHandlerIfNeeded();
                appendActionLog("Restored running timer from saved state (" + formatElapsed(getTotalActiveMs()) + ")");
                try { if (playPauseBtn != null) playPauseBtn.setText("Pause"); } catch (Exception ignored) {}
            } else {
                try { if (playPauseBtn != null) playPauseBtn.setText("Start"); } catch (Exception ignored) {}
            }
            refreshIntervalsUI();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private long getTotalActiveMs() {
        long total = totalActiveBeforeResume;
        if (isRunning) total += (System.currentTimeMillis() - lastResumeMs);
        return total;
    }

    private void startHandlerIfNeeded() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.post(timerRunnable);
        }
    }

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

        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            if (intervals.isEmpty()) return true;
            int idx = position;
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
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    picked.set(Calendar.HOUR_OF_DAY, 9);
                    picked.set(Calendar.MINUTE, 0);
                    picked.set(Calendar.SECOND, 0);
                    long startMs = picked.getTimeInMillis();

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
                                } catch (NumberFormatException ex) { }
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
        if (actionLogContainer == null) return;
        actionLogContainer.removeAllViews();
        for (String msg : actionLogMessages) {
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextSize(12f);
            tv.setTextColor(Color.WHITE);
            actionLogContainer.addView(tv);
        }
        final ScrollView sv = actionLogScroll;
        if (sv != null) sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
    }

    private void clearSavedTimerState() {
        prefs.edit().clear().apply();
    }
}
