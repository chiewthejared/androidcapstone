package com.example.test_v2.calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.test_v2.R;

public class MonthYearPickerDialog extends DialogFragment {

    public interface OnDateSetListener {
        void onDateSet(int year, int month);
    }

    private final OnDateSetListener listener;

    public MonthYearPickerDialog(OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_month_year_picker, null);

        NumberPicker monthPicker = view.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);
        Button okButton = view.findViewById(R.id.okButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        });
        monthPicker.setValue(currentMonth);

        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(2100);
        yearPicker.setValue(currentYear);

        AlertDialog dialog = builder.setView(view).create();

        okButton.setOnClickListener(v -> {
            int selectedMonth = monthPicker.getValue();
            int selectedYear = yearPicker.getValue();
            listener.onDateSet(selectedYear, selectedMonth);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
