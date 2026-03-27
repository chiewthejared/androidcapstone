package com.example.test_v2.articlesvideos;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticlesVideosPage extends AppCompatActivity {

    private static final String CHANNEL_ID = "UCcHhzB1sqo56Cc6oUp2FoGQ";
    private static final String PERMANENT_VIDEO_ID = "EVyoQkqcuWM";

    private static final String TAB_ALL = "all";
    private static final String TAB_VIDEOS = "videos";
    private static final String TAB_WEBSITE = "website";

    private RecyclerView latestRecycler;
    private LatestVideoAdapter latestAdapter;

    private RecyclerView articlesRecycler;
    private ArticlesVideosAdapter articlesAdapter;

    private final List<LatestVideoAdapter.VideoInfo> allLatestList = new ArrayList<>();
    private final List<LatestVideoAdapter.VideoInfo> latestVisibleList = new ArrayList<>();

    private final List<ArticleVideoItem> allArticleList = new ArrayList<>();
    private final List<ArticleVideoItem> articleVisibleList = new ArrayList<>();

    private ConstraintLayout featuredVideoCard;
    private TextView featuredVideoTitle;
    private TextView featuredRecommendedLabel;
    private Button btnVideos;
    private Button btnAll;
    private Button btnWebsite;
    private Button backButton;
    private TextView latestLabel;
    private TextView articlesLabel;
    private EditText searchBar;

    private String currentTab = TAB_ALL;
    private String currentQuery = "";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_videos_page);

        bindViews();
        setupRecyclerViews();
        setupStaticContent();
        setupListeners();
        setupChrome();
        refreshUi();

        fetchLatestVideos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void bindViews() {
        backButton = findViewById(R.id.back_button);

        btnAll = findViewById(R.id.button_all);
        btnVideos = findViewById(R.id.button_videos);
        btnWebsite = findViewById(R.id.button_website);

        searchBar = findViewById(R.id.searchBar);

        featuredVideoTitle = findViewById(R.id.featured_video_title);
        featuredRecommendedLabel = findViewById(R.id.featured_video_recommended_label);
        featuredVideoCard = findViewById(R.id.permanent_player_container);

        latestLabel = findViewById(R.id.latest_label);
        latestRecycler = findViewById(R.id.latest_videos_recycler);

        articlesLabel = findViewById(R.id.articles_label);
        articlesRecycler = findViewById(R.id.articles_recycler);
    }

    private void setupRecyclerViews() {
        latestAdapter = new LatestVideoAdapter(this, latestVisibleList);
        latestRecycler.setLayoutManager(new LinearLayoutManager(this));
        latestRecycler.setNestedScrollingEnabled(false);
        latestRecycler.setAdapter(latestAdapter);
        latestRecycler.addItemDecoration(new LineDividerDecoration(dp(1), Color.parseColor("#1F000000")));

        articlesAdapter = new ArticlesVideosAdapter(articleVisibleList, this);
        articlesRecycler.setLayoutManager(new LinearLayoutManager(this));
        articlesRecycler.setNestedScrollingEnabled(false);
        articlesRecycler.setAdapter(articlesAdapter);
        articlesRecycler.addItemDecoration(new LineDividerDecoration(dp(1), Color.parseColor("#1F000000")));
    }

    private void setupStaticContent() {
        allArticleList.clear();

        allArticleList.add(new ArticleVideoItem(
                getString(R.string.swf_website_title),
                getString(R.string.swf_website_description),
                "http://sturge-weber.org/",
                "article"
        ));

        allArticleList.add(new ArticleVideoItem(
                getString(R.string.emergency_room_guide_title),
                getString(R.string.emergency_room_guide_description),
                "https://sturge-weber.org/file_download/inline/d86f6b6f-fa6d-49f2-a405-484b38fdb2d3",
                "article"
        ));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        btnAll.setOnClickListener(v -> {
            currentTab = TAB_ALL;
            refreshUi();
        });

        btnVideos.setOnClickListener(v -> {
            currentTab = TAB_VIDEOS;
            refreshUi();
        });

        btnWebsite.setOnClickListener(v -> {
            currentTab = TAB_WEBSITE;
            refreshUi();
        });

        featuredVideoCard.setOnClickListener(v -> openYoutubeVideo(PERMANENT_VIDEO_ID));

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString();
                refreshUi();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupChrome() {
        backButton.setAllCaps(false);
        backButton.setText(getString(R.string.back_arrow));
        backButton.setTextSize(22f);
        backButton.setTextColor(ContextCompat.getColor(this, R.color.brand_green));
        backButton.setBackground(makeRoundedDrawable(
                Color.parseColor("#CFE3DF"),
                Color.TRANSPARENT,
                dp(22),
                0
        ));

        styleTabButton(btnAll, true, false);
        styleTabButton(btnVideos, false, true);
        styleTabButton(btnWebsite, false, true);

        searchBar.setBackground(makeRoundedDrawable(
                Color.parseColor("#F3F4F6"),
                Color.TRANSPARENT,
                dp(24),
                0
        ));
        searchBar.setPadding(dp(16), dp(12), dp(16), dp(12));

        featuredVideoCard.setBackground(makeFeaturedDrawable());
        featuredVideoCard.setClickable(true);
        featuredVideoCard.setFocusable(true);

        featuredVideoTitle.setText(getString(R.string.featured_video_title));
        featuredVideoTitle.setTextColor(Color.WHITE);

        featuredRecommendedLabel.setText(getString(R.string.recommended_by_swf));
        featuredRecommendedLabel.setTextColor(ContextCompat.getColor(this, R.color.golden_yellow));
        featuredRecommendedLabel.setTextSize(13f);

        latestLabel.setText(getString(R.string.latest_videos));
        latestLabel.setTextColor(ContextCompat.getColor(this, R.color.brand_green));

        articlesLabel.setText(getString(R.string.website));
        articlesLabel.setTextColor(ContextCompat.getColor(this, R.color.golden_yellow));

        latestRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        articlesRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void refreshUi() {
        applyFilters();
        updateTabAppearance();
        updateSectionVisibility();
    }

    private void applyFilters() {
        String query = currentQuery == null ? "" : currentQuery.trim().toLowerCase(Locale.US);

        articleVisibleList.clear();
        for (ArticleVideoItem item : allArticleList) {
            if (matches(item, query)) {
                articleVisibleList.add(item);
            }
        }

        latestVisibleList.clear();
        for (LatestVideoAdapter.VideoInfo info : allLatestList) {
            if (matches(info, query)) {
                latestVisibleList.add(info);
            }
        }

        articlesAdapter.notifyDataSetChanged();
        latestAdapter.notifyDataSetChanged();
    }

    private boolean matches(ArticleVideoItem item, String query) {
        if (query.isEmpty()) return true;

        return contains(item.getTitle(), query)
                || contains(item.getDescription(), query)
                || contains(item.getLink(), query)
                || contains(item.getType(), query);
    }

    private boolean matches(LatestVideoAdapter.VideoInfo info, String query) {
        if (query.isEmpty()) return true;
        return contains(info.title, query);
    }

    private boolean contains(String value, String query) {
        if (value == null) return false;
        return value.toLowerCase(Locale.US).contains(query);
    }

    private void updateSectionVisibility() {
        boolean showVideos = TAB_ALL.equals(currentTab) || TAB_VIDEOS.equals(currentTab);
        boolean showWebsite = TAB_ALL.equals(currentTab) || TAB_WEBSITE.equals(currentTab);

        featuredVideoCard.setVisibility(showVideos ? View.VISIBLE : View.GONE);
        latestLabel.setVisibility(showVideos ? View.VISIBLE : View.GONE);
        latestRecycler.setVisibility(showVideos ? View.VISIBLE : View.GONE);

        articlesLabel.setVisibility(showWebsite ? View.VISIBLE : View.GONE);
        articlesRecycler.setVisibility(showWebsite ? View.VISIBLE : View.GONE);

        if (showWebsite) {
            articlesLabel.setText(getString(R.string.website));
        }
    }

    private void updateTabAppearance() {
        styleTabButton(btnAll, TAB_ALL.equals(currentTab), false);
        styleTabButton(btnVideos, TAB_VIDEOS.equals(currentTab), true);
        styleTabButton(btnWebsite, TAB_WEBSITE.equals(currentTab), true);
    }

    private void styleTabButton(Button button, boolean selected, boolean yellowUnselectedText) {
        int fillColor = selected ? ContextCompat.getColor(this, R.color.port_wine) : Color.WHITE;
        int strokeColor = ContextCompat.getColor(this, R.color.port_wine);
        int textColor;

        if (selected) {
            textColor = Color.WHITE;
        } else if (yellowUnselectedText) {
            textColor = ContextCompat.getColor(this, R.color.golden_yellow);
        } else {
            textColor = Color.WHITE;
        }

        button.setBackground(makeRoundedDrawable(fillColor, strokeColor, dp(24), dp(1)));
        button.setTextColor(textColor);
        button.setAllCaps(false);
    }

    private GradientDrawable makeRoundedDrawable(int fillColor, int strokeColor, int radiusPx, int strokeWidthPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusPx);
        if (strokeWidthPx > 0) {
            drawable.setStroke(strokeWidthPx, strokeColor);
        }
        return drawable;
    }

    private GradientDrawable makeFeaturedDrawable() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        ContextCompat.getColor(this, R.color.port_wine),
                        Color.parseColor("#5F4550"),
                        ContextCompat.getColor(this, R.color.brand_green)
                }
        );
        drawable.setCornerRadius(dp(20));
        return drawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private void openYoutubeVideo(String videoId) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        try {
            startActivity(appIntent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoId)));
        }
    }

    private void fetchLatestVideos() {
        executorService.execute(() -> {
            List<LatestVideoAdapter.VideoInfo> result = new ArrayList<>();
            String feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=" + CHANNEL_ID;

            try (InputStream in = new URL(feedUrl).openStream()) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, null);

                int eventType = parser.getEventType();
                String text = null;
                String currentVideoId = null;
                String currentTitle = null;

                while (eventType != XmlPullParser.END_DOCUMENT && result.size() < 3) {
                    String tagName = parser.getName();

                    if (eventType == XmlPullParser.TEXT) {
                        text = parser.getText();
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if ("videoId".equals(tagName)) {
                            currentVideoId = text != null ? text.trim() : null;
                        } else if ("title".equals(tagName)) {
                            currentTitle = text != null ? text.trim() : null;
                        } else if ("entry".equals(tagName)) {
                            if (currentVideoId != null && currentTitle != null && !currentVideoId.isEmpty()) {
                                result.add(new LatestVideoAdapter.VideoInfo(currentVideoId, currentTitle));
                            }
                            currentVideoId = null;
                            currentTitle = null;
                        }
                    }

                    eventType = parser.next();
                }
            } catch (Exception ignored) {
            }

            runOnUiThread(() -> {
                if (!result.isEmpty()) {
                    allLatestList.clear();
                    allLatestList.addAll(result);

                    if (!result.isEmpty()) {
                        featuredVideoTitle.setText(result.get(0).title);
                    }

                    refreshUi();
                }
            });
        });
    }

    private static class LineDividerDecoration extends RecyclerView.ItemDecoration {
        private final int heightPx;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        LineDividerDecoration(int heightPx, int color) {
            this.heightPx = heightPx;
            paint.setColor(color);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position != RecyclerView.NO_POSITION && position < state.getItemCount() - 1) {
                outRect.bottom = heightPx;
            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            for (int i = 0; i < parent.getChildCount() - 1; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + heightPx;

                c.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}