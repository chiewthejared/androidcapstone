package com.example.test_v2.tags;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private List<Tag> tags;
    private final OnTagDeleteListener onTagDeleteListener;

    public interface OnTagDeleteListener {
        void onTagDelete(Tag tag);
    }

    public TagAdapter(List<Tag> tags, OnTagDeleteListener listener) {
        this.tags = tags;
        this.onTagDeleteListener = listener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        holder.tagTextView.setText(tag.name);
        holder.deleteButton.setOnClickListener(v -> onTagDeleteListener.onTagDelete(tag));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void updateTags(List<Tag> newTags) {
        this.tags = newTags;
        notifyDataSetChanged();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagTextView;
        Button deleteButton;

        public TagViewHolder(View itemView) {
            super(itemView);
            tagTextView = itemView.findViewById(R.id.tag_text);
            deleteButton = itemView.findViewById(R.id.delete_tag_button);
        }
    }
}
