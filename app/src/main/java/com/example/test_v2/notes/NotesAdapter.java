package com.example.test_v2.notes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<HelperNote> notes;
    private final NoteEditListener noteEditListener;

    public interface NoteEditListener {
        void onNoteEdit(HelperNote note);
    }

    public NotesAdapter(List<HelperNote> notes, NoteEditListener editListener) {
        this.notes = notes;
        this.noteEditListener = editListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_tile, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        HelperNote note = notes.get(position);
        holder.title.setText(note.title);

        if (note.type.equals("image")) {
            File file = new File(note.filePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                holder.previewImage.setImageBitmap(bitmap);
                holder.previewImage.setVisibility(View.VISIBLE);
                holder.previewText.setVisibility(View.GONE);
            }
        } else if (note.type.equals("pdf")) {
            holder.previewImage.setImageResource(R.drawable.ic_file);
            holder.previewImage.setVisibility(View.VISIBLE);
            holder.previewText.setVisibility(View.GONE);
        } else if (note.type.equals("video")) {
            File videoFile = new File(note.filePath);
            if (videoFile.exists()) {
                Bitmap thumbnail = null;
                try {
                    thumbnail = getVideoThumbnail(videoFile.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (thumbnail != null) {
                    holder.previewImage.setImageBitmap(thumbnail);
                } else {
                    holder.previewImage.setImageResource(R.drawable.ic_videos); // Placeholder if no thumbnail
                }
                holder.previewImage.setVisibility(View.VISIBLE);
                holder.previewText.setVisibility(View.GONE);
            }
        } else {
            holder.previewText.setText(note.content.length() > 50 ? note.content.substring(0, 50) + "..." : note.content);
            holder.previewImage.setVisibility(View.GONE);
            holder.previewText.setVisibility(View.VISIBLE);
        }

        // Handle file & note opening
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            if (note.type.equals("note")) {
                noteEditListener.onNoteEdit(note); // Open note editor
            } else if (note.type.equals("image") || note.type.equals("pdf")) {
                if (context instanceof NotesPage) {
                    ((NotesPage) context).openFile(context, note.filePath);
                }
            } else if (note.type.equals("video")) {
                openVideo(context, note.filePath);
            }
        });

        // Long press to show options (Rename/Delete)
        holder.itemView.setOnLongClickListener(v -> {
            if (holder.itemView.getContext() instanceof NotesPage) {
                ((NotesPage) holder.itemView.getContext()).showOptionsDialog(note);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<HelperNote> newNotes) {
        this.notes.clear();
        this.notes.addAll(newNotes);
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, previewText;
        ImageView previewImage;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            previewText = itemView.findViewById(R.id.note_content_preview);
            previewImage = itemView.findViewById(R.id.note_preview);
        }
    }

    /**
     * Extracts a thumbnail from a video file.
     */
    private Bitmap getVideoThumbnail(String videoPath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            retriever.release();
        }
    }

    /**
     * Opens a video file using an external video player.
     */
    private void openVideo(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(filePath), "video/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}