package com.example.test_v2;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.timer.HelperTimerEvent;
import com.example.test_v2.HelperUserAccount;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GuestTimerPage extends AppCompatActivity {

    private Button backButton;
    private TextView timerDisplay;
    private Button toggleTimerButton, addTimeButton;
    private ScrollView actionLogScroll;
    private LinearLayout actionLogContainer;

    private boolean isRunning = false;
    private boolean hasEverRun = false;

    // Real time storing
    private long baseTimeMs = 0L;
    private long totalActiveBeforeResume = 0L;

    // intervals => real times
    private List<long[]> intervals = new ArrayList<>();

    // for logging how long user ran / paused
    private long lastResumeMs = 0L;
    private long lastPauseMs = 0L;

    private ArrayList<String> actionLogMessages = new ArrayList<>();
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long now = System.currentTimeMillis();
                long elapsedActiveMs = totalActiveBeforeResume + (now - baseTimeMs);
                timerDisplay.setText(formatElapsed(elapsedActiveMs));
                timerHandler.postDelayed(this, 33L);
            }
        }
    };

    private HelperAppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_timer_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());

        backButton = findViewById(R.id.back_button_bottom);
        timerDisplay = findViewById(R.id.timer_display);
        toggleTimerButton = findViewById(R.id.toggle_timer_button);
        addTimeButton = findViewById(R.id.add_time_button);
        actionLogScroll = findViewById(R.id.action_log_scroll);
        actionLogContainer = findViewById(R.id.action_log_container);

        timerDisplay.setText("00:00:00.000");
        toggleTimerButton.setText("Start");

        // "Back" => must assign or discard if there's an active event
        backButton.setOnClickListener(v -> {
            if (hasEverRun) {
                showAssignOrDiscardDialog(() -> finish());
            } else {
                finish();
            }
        });

        toggleTimerButton.setOnClickListener(v -> handleToggleButton());
        addTimeButton.setOnClickListener(v -> finalizeEvent());
    }

    private void handleToggleButton() {
        if (!hasEverRun) {
            // START
            startTimer();
            hasEverRun = true;
            toggleTimerButton.setText("Pause");
            appendActionLog("User started timer at " + getCurrentTimeString());

        } else if (isRunning) {
            // PAUSE
            pauseTimer();
            long runInterval = System.currentTimeMillis() - lastResumeMs;
            appendActionLog("User paused at " + getCurrentTimeString()
                    + " after " + formatElapsed(runInterval) + " run");
            toggleTimerButton.setText("Resume");

        } else {
            // RESUME
            resumeTimer();
            long pausedInterval = System.currentTimeMillis() - lastPauseMs;
            appendActionLog("User resumed at " + getCurrentTimeString()
                    + " after " + formatElapsed(pausedInterval) + " paused");
            toggleTimerButton.setText("Pause");
        }
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

            intervals.add(new long[]{ now, -1 });
            timerHandler.post(timerRunnable);
        }
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
    }

    private void finalizeEvent() {
        if (isRunning) pauseTimer();
        removeEmptyInterval();

        long totalActiveMs = totalActiveBeforeResume;
        if (totalActiveMs <= 0) {
            Toast.makeText(this, "No time recorded", Toast.LENGTH_SHORT).show();
            resetTimer();
            finish();
            return;
        }

        appendActionLog("Event ended at " + getCurrentTimeString()
                + ", total " + formatElapsed(totalActiveMs));

        String intervalsJson = buildIntervalsJson(intervals);
        String finalActionLog = buildActionLogString();

        pickUserToAssign((chosenUserId, chosenUserName) -> {
            storeEventInDatabase(chosenUserId, intervalsJson, finalActionLog, totalActiveMs);
            Toast.makeText(this, "Event assigned to user: " + chosenUserName, Toast.LENGTH_SHORT).show();
            resetTimer();
            finish();
        }, () -> {
            Toast.makeText(this, "Event was discarded", Toast.LENGTH_SHORT).show();
            resetTimer();
            finish();
        });
    }

    private void showAssignOrDiscardDialog(Runnable onDone) {
        new AlertDialog.Builder(this)
                .setTitle("Assign Event to User?")
                .setMessage("You must assign this timer event to a user before leaving, or discard.")
                .setPositiveButton("Assign", (dialog, which) -> {
                    finalizeEvent();
                })
                .setNegativeButton("Discard", (dialog, which) -> {
                    resetTimer();
                    onDone.run();
                })
                .show();
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

    private String buildActionLogString() {
        StringBuilder sb = new StringBuilder();
        for (String line : actionLogMessages) {
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    private String formatElapsed(long ms) {
        int totalMs = (int) ms;
        int sec = totalMs / 1000;
        int milli = totalMs % 1000;
        int min = sec / 60;
        int hrs = min / 60;
        sec = sec % 60;
        min = min % 60;
        return String.format("%02d:%02d:%02d.%03d", hrs, min, sec, milli);
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
        timerDisplay.setText("00:00:00.000");
        toggleTimerButton.setText("Start");
        actionLogContainer.removeAllViews();
    }

    private void appendActionLog(String msg) {
        actionLogMessages.add(msg);
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(14f);
        actionLogContainer.addView(tv);
        actionLogScroll.post(() -> actionLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
        return sdf.format(new Date());
    }

    // The user assignment
    interface OnUserChosen {
        void userChosen(String userId, String userName);
    }

    private void pickUserToAssign(OnUserChosen onUserChosen, Runnable onCancel) {
        new Thread(() -> {
            List<HelperUserAccount> users = db.userDao().getAllUsers();
            runOnUiThread(() -> {
                if (users.isEmpty()) {
                    Toast.makeText(this, "No users exist! Discarding event...", Toast.LENGTH_SHORT).show();
                    onCancel.run();
                    return;
                }

                String[] userDisplayNames = new String[users.size()];
                String[] userIds = new String[users.size()];
                for (int i = 0; i < users.size(); i++) {
                    userDisplayNames[i] = users.get(i).getFullName();
                    userIds[i] = users.get(i).getPin();
                }

                new AlertDialog.Builder(this)
                        .setTitle("Choose a user to assign this event")
                        .setItems(userDisplayNames, (dialogInterface, index) -> {
                            String chosenUserId   = userIds[index];
                            String chosenUserName = userDisplayNames[index];
                            onUserChosen.userChosen(chosenUserId, chosenUserName);
                        })
                        .setNegativeButton("Cancel", (dialog, w) -> onCancel.run())
                        .show();
            });
        }).start();
    }

    private void storeEventInDatabase(String userId, String intervalsJson, String actionLog, long totalMs) {
        new Thread(() -> {
            HelperTimerEvent e = new HelperTimerEvent();
            e.userId = userId;
            // intervals.get(0)[0] is the real start
            e.startTimestamp = intervals.isEmpty()
                    ? System.currentTimeMillis()
                    : intervals.get(0)[0];
            e.totalTimeMs = totalMs;
            e.intervalsJson = intervalsJson;
            e.notes = "";
            e.actionLog = actionLog;
            e.eventName = "Timer Event";
            db.timerEventDao().insert(e);
        }).start();
    }
}
