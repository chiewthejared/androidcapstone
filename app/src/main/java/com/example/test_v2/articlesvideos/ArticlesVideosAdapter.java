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

public class ArticlesVideosAdapter extends RecyclerView.Adapter<ArticlesVideosAdapter.ViewHolder> {
    private List<ArticleVideoItem> items;
    private Context context;

    public ArticlesVideosAdapter(List<ArticleVideoItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ArticlesVideosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticlesVideosAdapter.ViewHolder holder, int position) {
        if (items == null || position < 0 || position >= items.size()) return;
        ArticleVideoItem item = items.get(position);

        final String title = item.getTitle() == null ? "" : item.getTitle();
        final String desc = item.getDescription() == null ? "" : item.getDescription();
        final String link = item.getLink();

        holder.title.setText(title);
        holder.description.setText(desc);

        // open when button pressed
        holder.openLinkButton.setOnClickListener(v -> openLink(link));

        // also open when title tapped
        holder.title.setOnClickListener(v -> openLink(link));
    }

    private void openLink(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
        } catch (Exception ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void updateList(List<ArticleVideoItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        Button openLinkButton;

        public ViewHolder(@NonNull View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            openLinkButton = view.findViewById(R.id.open_link_button);
        }
    }
}
