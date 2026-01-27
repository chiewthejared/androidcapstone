package com.example.test_v2.doctorInfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.test_v2.R;

public class DoctorDialogFragment extends DialogFragment {

    private DoctorItem existingItem;
    private DoctorViewModel viewModel;

    public static DoctorDialogFragment newInstance(DoctorItem doctorItem) {
        DoctorDialogFragment fragment = new DoctorDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("doctor", doctorItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_doctor, null);
        viewModel = new ViewModelProvider(requireActivity()).get(DoctorViewModel.class);

        // Check if editing
        if (getArguments() != null && getArguments().containsKey("doctor")) {
            existingItem = (DoctorItem) getArguments().getSerializable("doctor");
        }

        // Get all fields
        EditText nameInput = view.findViewById(R.id.doctor_name);
        EditText clinicInput = view.findViewById(R.id.clinic);
        EditText specialtyInput = view.findViewById(R.id.specialty);
        EditText conditionsInput = view.findViewById(R.id.conditions);
        EditText officeInput = view.findViewById(R.id.office);
        EditText phoneInput = view.findViewById(R.id.phone);
        EditText faxInput = view.findViewById(R.id.fax);
        EditText addressInput = view.findViewById(R.id.address);
        EditText insuranceInput = view.findViewById(R.id.insurance);
        EditText notesInput = view.findViewById(R.id.notes);
        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        // If editing, fill existing values
        if (existingItem != null) {
            nameInput.setText(existingItem.name);
            clinicInput.setText(existingItem.clinic);
            specialtyInput.setText(existingItem.specialty);
            conditionsInput.setText(existingItem.conditionsTreated);
            officeInput.setText(existingItem.office);
            phoneInput.setText(existingItem.phone);
            faxInput.setText(existingItem.fax);
            addressInput.setText(existingItem.address);
            insuranceInput.setText(existingItem.insurance);
            notesInput.setText(existingItem.notes);
        }

        // Save button logic
        saveButton.setOnClickListener(v -> {
            String userId = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    .getString("loggedInPin", null);
            if (userId == null || TextUtils.isEmpty(nameInput.getText().toString().trim()) || TextUtils.isEmpty(specialtyInput.getText().toString().trim())) {
                Toast.makeText(getContext(), "Doctor name and Specialty are required", Toast.LENGTH_SHORT).show();
                return;
            }

            DoctorItem doctor = new DoctorItem(
                    userId,
                    nameInput.getText().toString().trim(),
                    clinicInput.getText().toString().trim(),
                    specialtyInput.getText().toString().trim(),
                    conditionsInput.getText().toString().trim(),
                    officeInput.getText().toString().trim(),
                    phoneInput.getText().toString().trim(),
                    faxInput.getText().toString().trim(),
                    addressInput.getText().toString().trim(),
                    insuranceInput.getText().toString().trim(),
                    notesInput.getText().toString().trim()
            );

            if (existingItem != null) {
                doctor.id = existingItem.id;
                viewModel.update(doctor);
                Toast.makeText(getContext(), "Doctor updated", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.insert(doctor);
                Toast.makeText(getContext(), "Doctor added", Toast.LENGTH_SHORT).show();
            }

            dismiss();
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dismiss());

        // Return the dialog with your custom layout only
        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}