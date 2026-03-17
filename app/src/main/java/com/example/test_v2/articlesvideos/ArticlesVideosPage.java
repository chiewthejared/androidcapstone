package com.example.test_v2.articlesvideos;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ArticlesVideosPage extends AppCompatActivity {

    private static final String CHANNEL_ID = "UCcHhzB1sqo56Cc6oUp2FoGQ";
    private static final String PERMANENT_VIDEO_ID = "EVyoQkqcuWM";

    private RecyclerView latestRecycler;
    private LatestVideoAdapter latestAdapter;
    private List<LatestVideoAdapter.VideoInfo> latestList = new ArrayList<>();

    private RecyclerView articlesRecycler;
    private ArticlesVideosAdapter articlesAdapter;
    private List<ArticleVideoItem> articleList = new ArrayList<>();

    private FrameLayout playerContainer;
    private View youtubeLabel;
    private Button playInYoutube;
    private Button btnArticles, btnVideos, btnAll;
    private Button backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_videos_page);

        // --- YouTube player setup ---
        playerContainer = findViewById(R.id.permanent_player_container);
        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(this);
        playerContainer.addView(youTubePlayerView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                // Attempt to load the permanent video (some videos block embedding)
                youTubePlayer.loadVideo(PERMANENT_VIDEO_ID, 0f);
            }
        });

        youtubeLabel = findViewById(R.id.youtube_label);
        playInYoutube = findViewById(R.id.play_in_youtube_button);
        playInYoutube.setOnClickListener(v -> openYoutubeVideo(PERMANENT_VIDEO_ID));

        // --- Latest videos RecyclerView ---
        latestRecycler = findViewById(R.id.latest_videos_recycler);
        latestRecycler.setLayoutManager(new LinearLayoutManager(this));
        latestAdapter = new LatestVideoAdapter(this, latestList);
        latestRecycler.setAdapter(latestAdapter);

        // fetch latest videos
        new FetchLatestVideosTask().execute(CHANNEL_ID);

        // --- Articles RecyclerView + adapter ---
        articlesRecycler = findViewById(R.id.articles_recycler);
        articlesRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Add your two article links
        articleList.clear();
        articleList.add(new ArticleVideoItem(
                "Sturge-Weber Foundation Website",
                "Visit the official Sturge-Weber Foundation website.",
                "http://sturge-weber.org/",
                "article"
        ));
        articleList.add(new ArticleVideoItem(
                "Emergency Room Guide",
                "Download the Sturge-Weber Emergency Room Guide.",
                "https://sturge-weber.org/file_download/inline/d86f6b6f-fa6d-49f2-a405-484b38fdb2d3",
                "article"
        ));

        articlesAdapter = new ArticlesVideosAdapter(articleList, this);
        articlesRecycler.setAdapter(articlesAdapter);

        // --- Back button ---
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // --- Top filter buttons wiring ---
        btnArticles = findViewById(R.id.button_articles);
        btnVideos = findViewById(R.id.button_videos);
        btnAll = findViewById(R.id.button_all);

        btnArticles.setOnClickListener(v -> {
            showArticles();
            updateTopButtonState("articles");
        });

        btnVideos.setOnClickListener(v -> {
            showVideos();
            updateTopButtonState("videos");
        });

        btnAll.setOnClickListener(v -> {
            showAll();
            updateTopButtonState("all");
        });

        // default: show all
        showAll();
        updateTopButtonState("all");
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

    // Show/hide helpers for filter buttons
    private void showVideos() {
        // show YouTube + latest
        if (youtubeLabel != null) youtubeLabel.setVisibility(View.VISIBLE);
        if (playerContainer != null) playerContainer.setVisibility(View.VISIBLE);
        if (playInYoutube != null) playInYoutube.setVisibility(View.VISIBLE);
        if (latestRecycler != null) latestRecycler.setVisibility(View.VISIBLE);

        // hide articles
        if (articlesRecycler != null) articlesRecycler.setVisibility(View.GONE);
        View artLabel = findViewById(R.id.articles_label);
        if (artLabel != null) artLabel.setVisibility(View.GONE);
    }

    private void showArticles() {
        // hide YouTube + latest
        if (youtubeLabel != null) youtubeLabel.setVisibility(View.GONE);
        if (playerContainer != null) playerContainer.setVisibility(View.GONE);
        if (playInYoutube != null) playInYoutube.setVisibility(View.GONE);
        if (latestRecycler != null) latestRecycler.setVisibility(View.GONE);

        // show articles
        if (articlesRecycler != null) articlesRecycler.setVisibility(View.VISIBLE);
        View artLabel = findViewById(R.id.articles_label);
        if (artLabel != null) artLabel.setVisibility(View.VISIBLE);
    }

    private void showAll() {
        if (youtubeLabel != null) youtubeLabel.setVisibility(View.VISIBLE);
        if (playerContainer != null) playerContainer.setVisibility(View.VISIBLE);
        if (playInYoutube != null) playInYoutube.setVisibility(View.VISIBLE);
        if (latestRecycler != null) latestRecycler.setVisibility(View.VISIBLE);

        if (articlesRecycler != null) articlesRecycler.setVisibility(View.VISIBLE);
        View artLabel = findViewById(R.id.articles_label);
        if (artLabel != null) artLabel.setVisibility(View.VISIBLE);
    }

    private void updateTopButtonState(String selected) {
        float on = 1.0f;
        float off = 0.85f;
        if (btnArticles != null) btnArticles.setAlpha("articles".equals(selected) ? on : off);
        if (btnVideos != null) btnVideos.setAlpha("videos".equals(selected) ? on : off);
        if (btnAll != null) btnAll.setAlpha("all".equals(selected) ? on : off);
    }

    // AsyncTask to fetch channel feed and parse latest 3 entries
    private class FetchLatestVideosTask extends AsyncTask<String, Void, List<LatestVideoAdapter.VideoInfo>> {
        @Override
        protected List<LatestVideoAdapter.VideoInfo> doInBackground(String... strings) {
            String channelId = strings[0];
            String feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId;
            List<LatestVideoAdapter.VideoInfo> out = new ArrayList<>();
            try (InputStream in = new URL(feedUrl).openStream()) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, null);

                int eventType = parser.getEventType();
                String text = null;
                String currentVideoId = null;
                String currentTitle = null;

                while (eventType != XmlPullParser.END_DOCUMENT && out.size() < 3) {
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
                                out.add(new LatestVideoAdapter.VideoInfo(currentVideoId, currentTitle));
                            }
                            currentVideoId = null;
                            currentTitle = null;
                        }
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                // optional: log
            }
            return out;
        }

        @Override
        protected void onPostExecute(List<LatestVideoAdapter.VideoInfo> videoInfos) {
            if (videoInfos != null && !videoInfos.isEmpty()) {
                latestList.clear();
                latestList.addAll(videoInfos);
                if (latestAdapter != null) latestAdapter.notifyDataSetChanged();
            }
        }
    }
}
