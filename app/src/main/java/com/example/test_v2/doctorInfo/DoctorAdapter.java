package com.example.test_v2.doctorInfo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
                        item.specialty.toLowerCase().contains(lowerQuery) ||
                        (item.phone != null && item.phone.toLowerCase().contains(lowerQuery)) ||
                        (item.clinic != null && item.clinic.toLowerCase().contains(lowerQuery))) {
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

        // Phone
        if (!TextUtils.isEmpty(item.phone)) {
            holder.phoneRow.setVisibility(View.VISIBLE);
            holder.phone.setText(item.phone);
        } else {
            holder.phoneRow.setVisibility(View.GONE);
        }

        // Clinic (shown in email row position)
        if (!TextUtils.isEmpty(item.clinic)) {
            holder.emailRow.setVisibility(View.VISIBLE);
            holder.clinic.setText(item.clinic);
        } else {
            holder.emailRow.setVisibility(View.GONE);
        }

        // Address
        if (!TextUtils.isEmpty(item.address)) {
            holder.addressRow.setVisibility(View.VISIBLE);
            holder.address.setText(item.address);
        } else {
            holder.addressRow.setVisibility(View.GONE);
        }

        // Notes
        if (!TextUtils.isEmpty(item.notes)) {
            holder.notes.setVisibility(View.VISIBLE);
            holder.notes.setText(item.notes);
        } else {
            holder.notes.setVisibility(View.GONE);
        }

        // Tap to edit
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof DoctorInfoPage) {
                ((DoctorInfoPage) context).showDoctorDialog(item);
            }
        });

        // Long press to delete
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Doctor")
                    .setMessage("Are you sure you want to delete " + item.name + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (context instanceof DoctorInfoPage) {
                            ((DoctorInfoPage) context).doctorViewModel.delete(item);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public DoctorItem getItemAt(int position) {
        return doctorList.get(position);
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty, phone, clinic, address, notes;
        LinearLayout phoneRow, emailRow, addressRow;

        public DoctorViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.doctor_name);
            specialty = itemView.findViewById(R.id.doctor_specialty);
            phone = itemView.findViewById(R.id.doctor_phone);
            clinic = itemView.findViewById(R.id.doctor_clinic);
            address = itemView.findViewById(R.id.doctor_address);
            notes = itemView.findViewById(R.id.doctor_notes);
            phoneRow = itemView.findViewById(R.id.phone_row);
            emailRow = itemView.findViewById(R.id.email_row);
            addressRow = itemView.findViewById(R.id.address_row);
        }
    }
}