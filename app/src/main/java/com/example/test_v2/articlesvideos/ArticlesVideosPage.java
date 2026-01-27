package com.example.test_v2.articlesvideos;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArticlesVideosPage extends Activity {
    private RecyclerView recyclerView;
    private ArticlesVideosAdapter adapter;
    private List<ArticleVideoItem> allItems = new ArrayList<>();
    private EditText searchBar;
    private Button buttonArticles, buttonVideos, buttonAll, backButton;
    private String filterType = "all"; // Default to show all

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_videos_page);

        recyclerView = findViewById(R.id.articles_videos_recycler_view);
        searchBar = findViewById(R.id.search_bar);
        buttonArticles = findViewById(R.id.button_articles);
        buttonVideos = findViewById(R.id.button_videos);
        buttonAll = findViewById(R.id.button_all);
        backButton = findViewById(R.id.back_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter
        adapter = new ArticlesVideosAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Load data
        loadArticlesVideos();

        // Set button click listeners
        buttonArticles.setOnClickListener(v -> {
            filterType = "article";
            updateButtonStyles();
            filterContent(searchBar.getText().toString().trim());
        });

        buttonVideos.setOnClickListener(v -> {
            filterType = "video";
            updateButtonStyles();
            filterContent(searchBar.getText().toString().trim());
        });

        buttonAll.setOnClickListener(v -> {
            filterType = "all";
            updateButtonStyles();
            filterContent(searchBar.getText().toString().trim());
        });

        backButton.setOnClickListener(v -> finish());

        // Search Bar functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContent(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        updateButtonStyles(); // Ensure correct styling on launch
        filterContent(""); // Initial load
    }

    //THIS IS HOW YOU ADD NEW THINGS
    private void loadArticlesVideos() {
        allItems.clear();

        // Articles
        allItems.add(new ArticleVideoItem(
                "How to Improve Your Focus",
                "A guide on enhancing concentration and productivity.",
                "https://example.com/focus-guide",
                "article"
        ));

        allItems.add(new ArticleVideoItem(
                "The Future of AI",
                "An in-depth look at how artificial intelligence is shaping our world.",
                "https://example.com/ai-future",
                "article"
        ));

        // Videos
        allItems.add(new ArticleVideoItem(
                "Understanding Neural Networks",
                "A video explaining neural networks and how they work.",
                "https://www.youtube.com/watch?v=aircAruvnKk",
                "video"
        ));

        allItems.add(new ArticleVideoItem(
                "Top 10 Space Discoveries",
                "A documentary about the greatest discoveries in space.",
                "https://www.youtube.com/watch?v=1dGOXY5O9uM",
                "video"
        ));

        filterContent(""); // Apply filter
    }

    private void filterContent(String query) {
        if (adapter == null) return; // Prevent crash

        List<MatchResult> matchResults = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (ArticleVideoItem item : allItems) {
            if (!filterType.equals("all") && !item.getType().equals(filterType)) {
                continue; // Skip if it doesn't match the selected filter type
            }

            String titleLower = item.getTitle().toLowerCase();
            int matchScore = 0;

            if (titleLower.equals(queryLower)) {
                matchScore = 3;
            } else if (titleLower.startsWith(queryLower)) {
                matchScore = 2;
            } else if (titleLower.contains(queryLower)) {
                matchScore = 1;
            } else {
                continue;
            }

            matchResults.add(new MatchResult(item, matchScore));
        }

        Collections.sort(matchResults, (a, b) -> {
            if (b.matchScore != a.matchScore) {
                return Integer.compare(b.matchScore, a.matchScore);
            }
            return a.item.getTitle().compareToIgnoreCase(b.item.getTitle());
        });

        List<ArticleVideoItem> sortedItems = new ArrayList<>();
        for (MatchResult result : matchResults) {
            sortedItems.add(result.item);
        }

        adapter.updateList(sortedItems);
    }

    private void updateButtonStyles() {
        int selectedColor = Color.parseColor("#FFD700"); // Gold color for selected
        int defaultColor = Color.parseColor("#D3D3D3"); // Light gray for default

        buttonArticles.setBackgroundColor(filterType.equals("article") ? selectedColor : defaultColor);
        buttonVideos.setBackgroundColor(filterType.equals("video") ? selectedColor : defaultColor);
        buttonAll.setBackgroundColor(filterType.equals("all") ? selectedColor : defaultColor);
    }

    private static class MatchResult {
        ArticleVideoItem item;
        int matchScore;

        MatchResult(ArticleVideoItem item, int matchScore) {
            this.item = item;
            this.matchScore = matchScore;
        }
    }
}