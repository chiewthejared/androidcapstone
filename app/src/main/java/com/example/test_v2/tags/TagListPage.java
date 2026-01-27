package com.example.test_v2.tags;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.ProfilePage;
import com.example.test_v2.R;

import java.util.Arrays;
import java.util.List;

public class TagListPage extends AppCompatActivity {

    private EditText newTagEditText;
    private Button addTagButton, backButton;
    private RecyclerView tagsRecyclerView;
    private TagAdapter tagAdapter;
    private TagDao tagDao;
    private HelperAppDatabase db;

    // Default tags that cannot be removed
    private final List<String> defaultTags = Arrays.asList("Work", "Personal", "Urgent");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list_page);

        // Initialize views
        newTagEditText = findViewById(R.id.new_tag_edittext);
        addTagButton = findViewById(R.id.add_tag_button);
        backButton = findViewById(R.id.back_button);
        tagsRecyclerView = findViewById(R.id.tags_recycler_view);

        // Setup DB and DAO
        db = HelperAppDatabase.getDatabase(getApplicationContext());
        tagDao = db.tagDao();

        // RecyclerView setup
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tagAdapter = new TagAdapter(
                new java.util.ArrayList<>(),
                tag -> {
                    // Attempt to delete the tag
                    if (defaultTags.contains(tag.name)) {
                        Toast.makeText(TagListPage.this,
                                "Cannot remove default tag: " + tag.name,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Delete from DB
                        new Thread(() -> {
                            tagDao.delete(tag);
                            runOnUiThread(() -> {
                                Toast.makeText(TagListPage.this, "Tag deleted", Toast.LENGTH_SHORT).show();
                                loadTags();
                            });
                        }).start();
                    }
                }
        );
        tagsRecyclerView.setAdapter(tagAdapter);

        // Add new tag
        addTagButton.setOnClickListener(v -> {
            String tagText = newTagEditText.getText().toString().trim();
            if (!tagText.isEmpty()) {
                new Thread(() -> {
                    try {
                        tagDao.insert(new Tag(tagText));
                        runOnUiThread(() -> {
                            newTagEditText.setText("");
                            Toast.makeText(TagListPage.this, "Tag added", Toast.LENGTH_SHORT).show();
                            loadTags();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(TagListPage.this,
                                        "Tag might already exist",
                                        Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                Toast.makeText(TagListPage.this, "Enter a tag", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button -> return to ProfilePage
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(TagListPage.this, ProfilePage.class));
            finish();
        });

        // Load tags on start
        loadTags();
    }

    private void loadTags() {
        new Thread(() -> {
            List<Tag> tags = tagDao.getAll();
            runOnUiThread(() -> tagAdapter.updateTags(tags));
        }).start();
    }
}
