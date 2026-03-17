package com.example.test_v2.articlesvideos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.util.List;

public class LatestVideoAdapter extends RecyclerView.Adapter<LatestVideoAdapter.Holder> {

    public static class VideoInfo {
        public final String videoId;
        public final String title;
        public VideoInfo(String id, String title) { this.videoId = id; this.title = title; }
    }

    private final List<VideoInfo> items;
    private final Context ctx;

    public LatestVideoAdapter(Context ctx, List<VideoInfo> items) {
        this.ctx = ctx;
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_latest_video, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (items == null || position < 0 || position >= items.size()) return;
        VideoInfo info = items.get(position);
        holder.title.setText(info.title == null ? "" : info.title);
        holder.openBtn.setOnClickListener(v -> {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + info.videoId));
            try {
                ctx.startActivity(appIntent);
            } catch (Exception e) {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=" + info.videoId)));
            }
        });
    }

    @Override
    public int getItemCount() { return (items == null) ? 0 : items.size(); }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView title;
        Button openBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.latest_video_title);
            openBtn = itemView.findViewById(R.id.latest_video_button);
        }
    }
}
