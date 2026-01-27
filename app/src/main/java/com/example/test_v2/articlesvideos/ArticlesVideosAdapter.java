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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArticleVideoItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.typeLabel.setText(item.getType().equals("video") ? "🎬 Video" : "📰 Article");

        holder.openLinkButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<ArticleVideoItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, typeLabel;
        Button openLinkButton;

        public ViewHolder(View view) {
            super(view);
            typeLabel = view.findViewById(R.id.type_label);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            openLinkButton = view.findViewById(R.id.open_link_button);
        }
    }
}
