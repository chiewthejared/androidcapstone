package com.example.test_v2.articlesvideos;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
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
    // private EditText searchBar; <-- removed
    private Button buttonArticles, buttonVideos, buttonAll, backButton;
    private String filterType = "all"; // Default to show all

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_videos_page);

        // NOTE: layout updated to use new ids: articles_recycler etc.
        recyclerView = findViewById(R.id.articles_recycler);
        // searchBar = findViewById(R.id.search_bar); // removed
        buttonArticles = findViewById(R.id.button_articles);
        buttonVideos = findViewById(R.id.button_videos);
        buttonAll = findViewById(R.id.button_all);
        backButton = findViewById(R.id.back_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with an empty list (we'll update it via adapter.updateList)
        adapter = new ArticlesVideosAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Load data into allItems
        loadArticlesVideos();

        // Set button click listeners — now call filterContent("") because there is no search bar
        buttonArticles.setOnClickListener(v -> {
            filterType = "article";
            updateButtonStyles();
            filterContent("");
        });

        buttonVideos.setOnClickListener(v -> {
            filterType = "video";
            updateButtonStyles();
            filterContent("");
        });

        buttonAll.setOnClickListener(v -> {
            filterType = "all";
            updateButtonStyles();
            filterContent("");
        });

        backButton.setOnClickListener(v -> finish());

        // Removed Search Bar TextWatcher code (search bar removed)

        updateButtonStyles(); // Ensure correct styling on launch
        filterContent(""); // Initial load
    }

    // THIS IS HOW YOU ADD NEW THINGS
    // inside ArticlesVideosPage.java — replace the entire loadArticlesVideos() method with this:

    private void loadArticlesVideos() {
        allItems.clear();
        // Intentionally left empty — no demo articles or videos.
        // If you want to load items later, add allItems.add(...) calls here.
        // Ensure the adapter shows the current (empty) list:
        filterContent("");
    }


    private void filterContent(String query) {
        if (adapter == null) return; // Prevent crash

        List<MatchResult> matchResults = new ArrayList<>();
        String queryLower = (query == null) ? "" : query.toLowerCase();

        for (ArticleVideoItem item : allItems) {
            if (!filterType.equals("all") && !item.getType().equals(filterType)) {
                continue; // Skip if it doesn't match the selected filter type
            }

            // If query is empty, include all items that passed the type filter.
            if (queryLower.isEmpty()) {
                matchResults.add(new MatchResult(item, 0));
                continue;
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
                // If query provided and no match in title, try description
                String descLower = (item.getDescription() == null) ? "" : item.getDescription().toLowerCase();
                if (descLower.contains(queryLower)) {
                    matchScore = 1;
                } else {
                    continue;
                }
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
        // Selected vs unselected alpha for visual feedback
        float selAlpha = 1.0f;
        float unselAlpha = 0.85f;

        buttonArticles.setAlpha("article".equals(filterType) ? selAlpha : unselAlpha);
        buttonVideos.setAlpha("video".equals(filterType) ? selAlpha : unselAlpha);
        buttonAll.setAlpha("all".equals(filterType) ? selAlpha : unselAlpha);

        // Ensure the filter buttons keep the yellow text color defined in layout (#FFD600)
        int yellow = Color.parseColor("#eeb533");
        buttonArticles.setTextColor(yellow);
        buttonVideos.setTextColor(yellow);
        buttonAll.setTextColor(yellow);

        // Back button remains unchanged (green pill with white text defined in XML)
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
