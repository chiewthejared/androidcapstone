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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_tile, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        HelperNote note = notes.get(position);

        // Title
        holder.title.setText(note.title);

        // Date
        try {
            long ts = Long.parseLong(note.createdAt);
            String formatted = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                    .format(new Date(ts));
            holder.date.setText(formatted);
        } catch (Exception e) {
            holder.date.setText("");
        }

        // Type tag + attachment indicator
        switch (note.type) {
            case "note":
                holder.typeTag.setText("Note");
                holder.typeTag.setVisibility(View.VISIBLE);
                holder.attachmentIndicator.setVisibility(View.GONE);
                holder.previewImage.setVisibility(View.GONE);
                holder.previewText.setVisibility(View.GONE);
                break;
            case "image":
                holder.typeTag.setText("Image");
                holder.typeTag.setVisibility(View.VISIBLE);
                holder.attachmentIndicator.setVisibility(View.VISIBLE);
                File imgFile = new File(note.filePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.previewImage.setImageBitmap(bitmap);
                    holder.previewImage.setVisibility(View.VISIBLE);
                }
                holder.previewText.setVisibility(View.GONE);
                break;
            case "pdf":
                holder.typeTag.setText("PDF");
                holder.typeTag.setVisibility(View.VISIBLE);
                holder.attachmentIndicator.setVisibility(View.VISIBLE);
                holder.previewImage.setImageResource(R.drawable.ic_file);
                holder.previewImage.setVisibility(View.VISIBLE);
                holder.previewText.setVisibility(View.GONE);
                break;
            case "video":
                holder.typeTag.setText("Video");
                holder.typeTag.setVisibility(View.VISIBLE);
                holder.attachmentIndicator.setVisibility(View.VISIBLE);
                File videoFile = new File(note.filePath);
                if (videoFile.exists()) {
                    try {
                        Bitmap thumbnail = getVideoThumbnail(videoFile.getAbsolutePath());
                        if (thumbnail != null) {
                            holder.previewImage.setImageBitmap(thumbnail);
                        } else {
                            holder.previewImage.setImageResource(R.drawable.ic_videos);
                        }
                    } catch (IOException e) {
                        holder.previewImage.setImageResource(R.drawable.ic_videos);
                    }
                    holder.previewImage.setVisibility(View.VISIBLE);
                }
                holder.previewText.setVisibility(View.GONE);
                break;
            default:
                holder.typeTag.setText("Other");
                holder.typeTag.setVisibility(View.VISIBLE);
                holder.attachmentIndicator.setVisibility(View.VISIBLE);
                holder.previewImage.setVisibility(View.GONE);
                holder.previewText.setVisibility(View.GONE);
                break;
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            if (note.type.equals("note")) {
                noteEditListener.onNoteEdit(note);
            } else if (note.type.equals("image") || note.type.equals("pdf")) {
                if (context instanceof NotesPage) {
                    ((NotesPage) context).openFile(context, note.filePath);
                }
            } else if (note.type.equals("video")) {
                openVideo(context, note.filePath);
            }
        });

        // Long press
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
        TextView title, previewText, date, typeTag, attachmentIndicator;
        ImageView previewImage;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            previewText = itemView.findViewById(R.id.note_content_preview);
            previewImage = itemView.findViewById(R.id.note_preview);
            date = itemView.findViewById(R.id.note_date);
            typeTag = itemView.findViewById(R.id.note_type_tag);
            attachmentIndicator = itemView.findViewById(R.id.attachment_indicator);
        }
    }

    private Bitmap getVideoThumbnail(String videoPath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            return retriever.getFrameAtTime(1000000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            return null;
        } finally {
            retriever.release();
        }
    }

    private void openVideo(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(filePath), "video/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}