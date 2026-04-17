package com.swf.app.articlesvideos;

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

import com.swf.app.R;

import java.util.List;

public class LatestVideoAdapter extends RecyclerView.Adapter<LatestVideoAdapter.VideoViewHolder> {

    public static class VideoInfo {
        public String videoId;
        public String title;

        public VideoInfo(String videoId, String title) {
            this.videoId = videoId;
            this.title = title;
        }
    }

    private final List<VideoInfo> videoList;
    private final Context context;

    public LatestVideoAdapter(Context context, List<VideoInfo> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_latest_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoInfo video = videoList.get(position);

        holder.title.setText(video.title);

        // 🔥 ADD DESCRIPTION (simple auto description)
        holder.description.setText("Watch this video about " + video.title.toLowerCase());

        holder.button.setOnClickListener(v -> openYoutube(video.videoId));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        Button button;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.latest_video_title);
            description = itemView.findViewById(R.id.latest_video_description);
            button = itemView.findViewById(R.id.latest_video_button);
        }
    }

    private void openYoutube(String videoId) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        try {
            context.startActivity(appIntent);
        } catch (Exception e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoId)));
        }
    }
}