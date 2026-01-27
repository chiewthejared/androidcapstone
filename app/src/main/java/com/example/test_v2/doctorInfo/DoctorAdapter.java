package com.example.test_v2.doctorInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private final Context context;
    private List<DoctorItem> doctorList = new ArrayList<>();
    private List<DoctorItem> fullList = new ArrayList<>();

    public DoctorAdapter(Context context) {
        this.context = context;
    }

    public void setDoctorList(List<DoctorItem> doctors) {
        this.doctorList = new ArrayList<>(doctors);
        this.fullList = new ArrayList<>(doctors);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            doctorList = new ArrayList<>(fullList);
        } else {
            String lowerQuery = query.toLowerCase();
            doctorList = new ArrayList<>();
            for (DoctorItem item : fullList) {
                if (item.name.toLowerCase().contains(lowerQuery) ||
                        item.specialty.toLowerCase().contains(lowerQuery)) {
                    doctorList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public DoctorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_item_layout, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoctorViewHolder holder, int position) {
        DoctorItem item = doctorList.get(position);
        holder.name.setText(item.name);
        holder.specialty.setText(item.specialty);

        // Tap card to edit
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof DoctorInfoPage) {
                ((DoctorInfoPage) context).showDoctorDialog(item);
            }
        });

        // Delete button logic
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Doctor")
                    .setMessage("Are you sure you want to delete this doctor?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (context instanceof DoctorInfoPage) {
                            ((DoctorInfoPage) context).doctorViewModel.delete(item);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty;
        Button deleteButton;

        public DoctorViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.doctor_name);
            specialty = itemView.findViewById(R.id.doctor_specialty);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}