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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.fileAndDatabase.FileStorageHelper;
import com.example.test_v2.fileAndDatabase.FileViewerActivity;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
    private EditText searchBar;
    private List<HelperNote> allNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.notes_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new NotesAdapter(new ArrayList<>(), this::showEditNoteDialog);
        recyclerView.setAdapter(adapter);

        Button addMediaButton = findViewById(R.id.add_media_button);
        Button addNoteButton = findViewById(R.id.add_note_button);
        Button backButton = findViewById(R.id.back_button);

        addMediaButton.setOnClickListener(v -> selectFile("*/*"));
        addNoteButton.setOnClickListener(v -> showAddNoteDialog());
        backButton.setOnClickListener(v -> finish());

        // Add search bar functionality
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        executorService.execute(this::fetchUserAndLoadNotes);
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
            allNotes = db.noteDao().getAllNotesForUser(String.valueOf(currentUserId)); // Store all notes for filtering
            runOnUiThread(() -> adapter.updateNotes(new ArrayList<>(allNotes)));
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
            if (fileUri != null) {
                saveFileToInternalStorage(fileUri);
            }
        }
    }

    private void saveFileToInternalStorage(Uri fileUri) {
        executorService.execute(() -> {
            try {
                String originalFileName = FileStorageHelper.getFileName(getContentResolver(), fileUri);
                File savedFile = FileStorageHelper.saveFile(getContentResolver(), getFilesDir(), fileUri, originalFileName);
                if (savedFile != null) {
                    insertFileIntoDatabase(savedFile.getAbsolutePath(), originalFileName);
                }
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

            //Determine file type by extension
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png") || filePath.endsWith(".heic")) {
                newNote.type = "image";
            } else if (filePath.endsWith(".pdf")) {
                newNote.type = "pdf";
            } else if (filePath.endsWith(".mp4") || filePath.endsWith(".mov") || filePath.endsWith(".avi") || filePath.endsWith(".mkv")) {
                newNote.type = "video";  // Identify as a video
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
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
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
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options");

            String[] options = {"Rename", "Delete"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    showRenameDialog(note); // Rename
                } else if (which == 1) {
                    showDeleteConfirmationDialog(note); // Delete
                }
            });

            builder.setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss());
            builder.show();
        });
    }

    public void showRenameDialog(HelperNote note) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Rename File");

            // Input field for new name
            EditText input = new EditText(this);
            input.setText(note.title); // Set existing name
            builder.setView(input);

            builder.setPositiveButton("Rename", (dialog, which) -> {
                String newTitle = input.getText().toString().trim();
                if (!newTitle.isEmpty()) {
                    renameFile(note, newTitle);
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void renameFile(HelperNote note, String newTitle) {
        executorService.execute(() -> {
            if (note.type.equals("note")) {
                //  Rename text note (update title in database only)
                note.title = newTitle;
                db.noteDao().update(note);
                runOnUiThread(this::loadNotes);
            } else {
                //  Rename file-based notes (PDFs & Images)
                File oldFile = new File(note.filePath);
                if (!oldFile.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Get file extension
                String extension = "";
                int i = note.filePath.lastIndexOf('.');
                if (i > 0) {
                    extension = note.filePath.substring(i);
                }

                // Create new file with updated name
                File newFile = new File(oldFile.getParent(), newTitle + extension);
                boolean renamed = oldFile.renameTo(newFile);

                if (renamed) {
                    note.title = newTitle;  // Update title
                    note.filePath = newFile.getAbsolutePath(); // Update path in database
                    db.noteDao().update(note);
                    runOnUiThread(this::loadNotes);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void showDeleteConfirmationDialog(HelperNote note) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Deletion");
            builder.setMessage("Are you sure you want to delete this item?");

            builder.setPositiveButton("Delete", (dialog, which) -> deleteNote(note));
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void deleteNote(HelperNote note) {
        executorService.execute(() -> {
            if (!note.filePath.isEmpty()) {
                File file = new File(note.filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        runOnUiThread(() -> Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show());
                    }
                }
            }

            db.noteDao().delete(note);
            runOnUiThread(this::loadNotes);
        });
    }

    private void filterNotes(String query) {
        if (query.isEmpty()) {
            runOnUiThread(() -> adapter.updateNotes(new ArrayList<>(allNotes)));
            return;
        }

        List<MatchResult> matchResults = new ArrayList<>();

        for (HelperNote note : allNotes) {
            String titleLower = note.title.toLowerCase();
            String queryLower = query.toLowerCase();

            if (titleLower.contains(queryLower)) {
                int matchScore = 0;

                if (titleLower.equals(queryLower)) {
                    matchScore = 3; // Exact match gets highest priority
                } else if (titleLower.startsWith(queryLower)) {
                    matchScore = 2; // Starts with query gets second priority
                } else {
                    matchScore = 1; // Contains query but not at the start
                }

                matchResults.add(new MatchResult(note, matchScore));
            }
        }

        // Sort by match score (higher first), then by createdAt (newest first)
        Collections.sort(matchResults, (a, b) -> {
            if (b.matchScore != a.matchScore) {
                return Integer.compare(b.matchScore, a.matchScore); // Higher score first
            }
            return Long.compare(Long.parseLong(b.note.createdAt), Long.parseLong(a.note.createdAt)); // Newest first
        });

        // Extract sorted HelperNote objects
        List<HelperNote> sortedNotes = new ArrayList<>();
        for (MatchResult result : matchResults) {
            sortedNotes.add(result.note);
        }

        runOnUiThread(() -> adapter.updateNotes(sortedNotes));
    }

    //  Helper class to store match scores
    private static class MatchResult {
        HelperNote note;
        int matchScore;

        MatchResult(HelperNote note, int matchScore) {
            this.note = note;
            this.matchScore = matchScore;
        }
    }

}