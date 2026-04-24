package com.swf.capstone26.medsEquipment;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swf.capstone26.R;

import java.util.List;

public class MedsEquipmentAdapter extends RecyclerView.Adapter<MedsEquipmentAdapter.ViewHolder> {
    private final List<MedsEquipmentItem> items;
    private final Context context;

    public MedsEquipmentAdapter(List<MedsEquipmentItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meds_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedsEquipmentItem item = items.get(position);

        holder.name.setText(item.getName());
        holder.date.setText(buildDateLabel(item));
        holder.description.setText(buildSummary(item));
        holder.description.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MedsAndEquipmentTrackerPage) {
                if ("Medication".equals(item.getTag())) {
                    ((MedsAndEquipmentTrackerPage) context).showMedicationDetailsDialog(item);
                } else if ("Equipment".equals(item.getTag())) {
                    ((MedsAndEquipmentTrackerPage) context).showEquipmentDetailsDialog(item);
                } else if ("Supplies".equals(item.getTag())) {
                    ((MedsAndEquipmentTrackerPage) context).showSupplyDetailsDialog(item);
                }
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (context instanceof MedsAndEquipmentTrackerPage) {
                            ((MedsAndEquipmentTrackerPage) context).deleteMedsOrEquipment(item);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildDateLabel(MedsEquipmentItem item) {
        if ("Medication".equals(item.getTag())) {
            return "Refill: " + safe(item.getDate());
        } else if ("Equipment".equals(item.getTag())) {
            return "Maintenance: " + safe(item.getDate());
        } else {
            return "Next Order: " + safe(item.getDate());
        }
    }

    private String buildSummary(MedsEquipmentItem item) {
        if ("Medication".equals(item.getTag())) {
            String frequencyText = safe(item.getFrequency()).isEmpty()
                    ? "N/A"
                    : item.getFrequency() + " times/day";

            return "Dosage: " + safe(item.getDosage())
                    + "\nFrequency: " + frequencyText
                    + "\nTime: " + safe(item.getTime())
                    + "\nDoctor: " + safe(item.getPrescribingDoctor());
        } else if ("Equipment".equals(item.getTag())) {
            return "Serial: " + safe(item.getSerialNumber())
                    + "\nDoctor: " + safe(item.getPrescribingDoctor())
                    + "\nProvider: " + safe(item.getEquipmentProvider());
        } else {
            return "Order Qty: " + safe(item.getOrderQuantity())
                    + "\nFrequency: " + safe(item.getOrderFrequency())
                    + "\nDoctor: " + safe(item.getPrescribingDoctor());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, description;
        Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            date = itemView.findViewById(R.id.item_date);
            description = itemView.findViewById(R.id.item_description);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public void updateData(List<MedsEquipmentItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
}