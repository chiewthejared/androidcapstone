package com.example.test_v2.medsEquipment;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.util.List;

public class MedsEquipmentAdapter extends RecyclerView.Adapter<MedsEquipmentAdapter.ViewHolder> {
    private List<MedsEquipmentItem> items;
    private Context context;

    public MedsEquipmentAdapter(List<MedsEquipmentItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meds_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedsEquipmentItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.date.setText(item.getDate());
        holder.description.setText(item.getDescription());

        // Handle item click for details
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MedsAndEquipmentTrackerPage) {
                if ("Medication".equals(item.getTag())) {
                    ((MedsAndEquipmentTrackerPage) context).showMedicationDetailsDialog(item);
                } else if ("Equipment".equals(item.getTag())) {
                    ((MedsAndEquipmentTrackerPage) context).showEquipmentDetailsDialog(item); // You'll create this!
                }
                else if ("Supplies".equals(item.getTag())) {
                    ((MedsAndEquipmentTrackerPage) context).showSupplyDetailsDialog(item); // NEW
                }
            }
        });

        // Handle delete button click
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, description;
        Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            date = itemView.findViewById(R.id.item_date);
            description = itemView.findViewById(R.id.item_description);
            deleteButton = itemView.findViewById(R.id.delete_button); // Make sure this ID exists in XML
        }
    }

    // Method to update the adapter dataset
    public void updateData(List<MedsEquipmentItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }
}