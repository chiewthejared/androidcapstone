package com.example.test_v2.notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.fileAndDatabase.FileStorageHelper;
import com.example.test_v2.fileAndDatabase.FileViewerActivity;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesPage extends Activity {
    private static final int FILE_PICKER_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private HelperAppDatabase db;
    private int currentUserId = -1;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<HelperNote> allNotes = new ArrayList<>();
    private String currentFilter = "all";
    private String currentSearch = "";

    private TextView filterAll, filterMedical, filterPersonal, filterSchool, filterOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.notes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotesAdapter(new ArrayList<>(), this::showEditNoteDialog);
        recyclerView.setAdapter(adapter);

        Button addNoteButton = findViewById(R.id.add_note_button);
        Button addMediaButton = findViewById(R.id.add_media_button);
        Button backButton = findViewById(R.id.back_button);

        addNoteButton.setOnClickListener(v -> showAddNoteDialog());
        addMediaButton.setOnClickListener(v -> selectFile("*/*"));
        backButton.setOnClickListener(v -> finish());

        // Search bar
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                applyFilterAndSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter tags
        filterAll      = findViewById(R.id.filter_all);
        filterMedical  = findViewById(R.id.filter_medical);
        filterPersonal = findViewById(R.id.filter_personal);
        filterSchool   = findViewById(R.id.filter_school);
        filterOther    = findViewById(R.id.filter_other);

        filterAll.setOnClickListener(v -> setFilter("all"));
        filterMedical.setOnClickListener(v -> setFilter("Medical"));
        filterPersonal.setOnClickListener(v -> setFilter("Personal"));
        filterSchool.setOnClickListener(v -> setFilter("School"));
        filterOther.setOnClickListener(v -> setFilter("Other"));

        executorService.execute(this::fetchUserAndLoadNotes);
    }

    private void setFilter(String filter) {
        currentFilter = filter;

        int blue = android.graphics.Color.parseColor("#3A7BD5");
        filterAll.setTextColor(blue);
        filterMedical.setTextColor(blue);
        filterPersonal.setTextColor(blue);
        filterSchool.setTextColor(blue);
        filterOther.setTextColor(blue);

        filterAll.setBackgroundResource(R.drawable.tag_unselected_bg);
        filterMedical.setBackgroundResource(R.drawable.tag_unselected_bg);
        filterPersonal.setBackgroundResource(R.drawable.tag_unselected_bg);
        filterSchool.setBackgroundResource(R.drawable.tag_unselected_bg);
        filterOther.setBackgroundResource(R.drawable.tag_unselected_bg);

        TextView selected;
        switch (filter) {
            case "Medical":  selected = filterMedical;  break;
            case "Personal": selected = filterPersonal; break;
            case "School":   selected = filterSchool;   break;
            case "Other":    selected = filterOther;    break;
            default:         selected = filterAll;      break;
        }
        selected.setBackgroundResource(R.drawable.tag_selected_bg);
        selected.setTextColor(android.graphics.Color.parseColor("#C0626A"));

        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        List<HelperNote> filtered = new ArrayList<>();
        for (HelperNote note : allNotes) {
            // 没有 category 字段，filter 暂时全显示
            boolean searchMatch = currentSearch.isEmpty() ||
                    note.title.toLowerCase().contains(currentSearch.toLowerCase());
            if (searchMatch) {
                filtered.add(note);
            }
        }
        runOnUiThread(() -> adapter.updateNotes(filtered));
    }

    private void fetchUserAndLoadNotes() {
        String hashedPin = getSessionPin();
        if (hashedPin == null) {
            runOnUiThread(this::finish);
            return;
        }
        executorService.execute(() -> {
            HelperUserAccount loggedInUser = db.userDao().getUserByPin(hashedPin);
            if (loggedInUser == null) {
                runOnUiThread(this::finish);
                return;
            }
            currentUserId = loggedInUser.getId();
            runOnUiThread(this::loadNotes);
        });
    }

    private void loadNotes() {
        executorService.execute(() -> {
            allNotes = db.noteDao().getAllNotesForUser(String.valueOf(currentUserId));
            applyFilterAndSearch();
        });
    }

    private void selectFile(String type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) saveFileToInternalStorage(fileUri);
        }
    }

    private void saveFileToInternalStorage(Uri fileUri) {
        executorService.execute(() -> {
            try {
                String originalFileName = FileStorageHelper.getFileName(getContentResolver(), fileUri);
                File savedFile = FileStorageHelper.saveFile(getContentResolver(), getFilesDir(), fileUri, originalFileName);
                if (savedFile != null) insertFileIntoDatabase(savedFile.getAbsolutePath(), originalFileName);
            } catch (Exception e) {
                Log.e("NotesPage", "Error saving file", e);
            }
        });
    }

    private void insertFileIntoDatabase(String filePath, String title) {
        executorService.execute(() -> {
            HelperNote newNote = new HelperNote();
            newNote.userId = String.valueOf(currentUserId);
            newNote.title = title;
            newNote.content = "";
            newNote.filePath = filePath;
            newNote.createdAt = String.valueOf(System.currentTimeMillis());

            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") ||
                    filePath.endsWith(".png") || filePath.endsWith(".heic")) {
                newNote.type = "image";
            } else if (filePath.endsWith(".pdf")) {
                newNote.type = "pdf";
            } else if (filePath.endsWith(".mp4") || filePath.endsWith(".mov") ||
                    filePath.endsWith(".avi") || filePath.endsWith(".mkv")) {
                newNote.type = "video";
            } else {
                newNote.type = "other";
            }

            db.noteDao().insert(newNote);
            runOnUiThread(this::loadNotes);
        });
    }

    private void showAddNoteDialog() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
            builder.setView(dialogView);

            EditText noteTitleInput = dialogView.findViewById(R.id.note_title_input);
            EditText noteContentInput = dialogView.findViewById(R.id.note_content_input);
            Button saveButton = dialogView.findViewById(R.id.save_button);
            Button cancelButton = dialogView.findViewById(R.id.cancel_button);

            AlertDialog dialog = builder.create();

            saveButton.setOnClickListener(v -> {
                String title = noteTitleInput.getText().toString().trim();
                String content = noteContentInput.getText().toString().trim();
                if (!title.isEmpty() || !content.isEmpty()) {
                    insertTextNoteIntoDatabase(title, content);
                }
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        });
    }

    private void insertTextNoteIntoDatabase(String title, String content) {
        executorService.execute(() -> {
            HelperNote newNote = new HelperNote();
            newNote.userId = String.valueOf(currentUserId);
            newNote.title = title;
            newNote.content = content;
            newNote.filePath = "";
            newNote.createdAt = String.valueOf(System.currentTimeMillis());
            newNote.type = "note";
            db.noteDao().insert(newNote);
            runOnUiThread(this::loadNotes);
        });
    }

    private void showEditNoteDialog(HelperNote note) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
            builder.setView(dialogView);

            EditText noteTitleInput = dialogView.findViewById(R.id.note_title_input);
            EditText noteContentInput = dialogView.findViewById(R.id.note_content_input);
            Button saveButton = dialogView.findViewById(R.id.save_button);
            Button cancelButton = dialogView.findViewById(R.id.cancel_button);

            noteTitleInput.setText(note.title);
            noteContentInput.setText(note.content);

            AlertDialog dialog = builder.create();

            saveButton.setOnClickListener(v -> {
                String updatedTitle = noteTitleInput.getText().toString().trim();
                String updatedContent = noteContentInput.getText().toString().trim();
                if (!updatedTitle.isEmpty() || !updatedContent.isEmpty()) {
                    updateNoteInDatabase(note.id, updatedTitle, updatedContent);
                }
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        });
    }

    private void updateNoteInDatabase(int noteId, String title, String content) {
        executorService.execute(() -> {
            HelperNote note = db.noteDao().getNoteById(noteId);
            if (note != null) {
                note.title = title;
                note.content = content;
                db.noteDao().update(note);
                runOnUiThread(this::loadNotes);
            }
        });
    }

    private String getSessionPin() {
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        return preferences.getString("loggedInPin", null);
    }

    public void openFile(Context context, String filePath) {
        Intent intent = new Intent(context, FileViewerActivity.class);
        intent.putExtra("filePath", filePath);
        context.startActivity(intent);
    }

    public void showOptionsDialog(HelperNote note) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Options")
                    .setItems(new String[]{"Rename", "Delete"}, (dialog, which) -> {
                        if (which == 0) showRenameDialog(note);
                        else showDeleteConfirmationDialog(note);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    public void showRenameDialog(HelperNote note) {
        runOnUiThread(() -> {
            EditText input = new EditText(this);
            input.setText(note.title);
            new AlertDialog.Builder(this)
                    .setTitle("Rename")
                    .setView(input)
                    .setPositiveButton("Rename", (dialog, which) -> {
                        String newTitle = input.getText().toString().trim();
                        if (!newTitle.isEmpty()) renameFile(note, newTitle);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void renameFile(HelperNote note, String newTitle) {
        executorService.execute(() -> {
            if (note.type.equals("note")) {
                note.title = newTitle;
                db.noteDao().update(note);
                runOnUiThread(this::loadNotes);
            } else {
                File oldFile = new File(note.filePath);
                if (!oldFile.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show());
                    return;
                }
                String extension = "";
                int i = note.filePath.lastIndexOf('.');
                if (i > 0) extension = note.filePath.substring(i);
                File newFile = new File(oldFile.getParent(), newTitle + extension);
                if (oldFile.renameTo(newFile)) {
                    note.title = newTitle;
                    note.filePath = newFile.getAbsolutePath();
                    db.noteDao().update(note);
                    runOnUiThread(this::loadNotes);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showDeleteConfirmationDialog(HelperNote note) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote(note))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show());
    }

    private void deleteNote(HelperNote note) {
        executorService.execute(() -> {
            if (note.filePath != null && !note.filePath.isEmpty()) {
                File file = new File(note.filePath);
                if (file.exists()) file.delete();
            }
            db.noteDao().delete(note);
            runOnUiThread(this::loadNotes);
        });
    }
}