package com.example.test_v2.medsEquipment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.homeIntroLogin.HomePage;
import com.example.test_v2.R;
import com.example.test_v2.ReminderReceiver;
import com.example.test_v2.calendar.EventViewModel;
import com.example.test_v2.calendar.HelperEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class MedsAndEquipmentTrackerPage extends AppCompatActivity {
    private EventViewModel eventViewModel;
    private RecyclerView medsRecyclerView, equipRecyclerView, suppliesRecyclerView;
    private MedsEquipmentAdapter medsAdapter, equipAdapter, suppliesAdapter;
    private List<MedsEquipmentItem> medsList = new ArrayList<>();
    private List<MedsEquipmentItem> equipList = new ArrayList<>();
    private List<MedsEquipmentItem> suppliesList = new ArrayList<>();

    String currentUserSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medication_tracker_page);

        currentUserSession = (this.getSharedPreferences("UserSession", MODE_PRIVATE).getString("loggedInPin", null)).toString();
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        Button addMedicationButton = findViewById(R.id.add_medication_button);
        Button addEquipmentButton = findViewById(R.id.add_equipment_button);
        Button addSupplyButton = findViewById(R.id.add_supplies_button);
        Button backButton = findViewById(R.id.back_button);
        medsRecyclerView = findViewById(R.id.meds_recycler_view);
        equipRecyclerView = findViewById(R.id.equip_recycler_view);
        suppliesRecyclerView = findViewById(R.id.supplies_recycler_view);
        medsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        equipRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suppliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        medsAdapter = new MedsEquipmentAdapter(medsList, this);
        equipAdapter = new MedsEquipmentAdapter(equipList, this);
        suppliesAdapter = new MedsEquipmentAdapter(suppliesList, this);
        medsRecyclerView.setAdapter(medsAdapter);
        equipRecyclerView.setAdapter(equipAdapter);
        suppliesRecyclerView.setAdapter(suppliesAdapter);

        loadMedicationsAndEquipment();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
            finish();
        });

        addMedicationButton.setOnClickListener(v -> showMedicationDialog());
        addEquipmentButton.setOnClickListener(v -> showEquipmentDialog());
        addSupplyButton.setOnClickListener(v -> showSupplyDialog());
    }

    private void loadMedicationsAndEquipment() {
        eventViewModel.getAllEvents().observe(this, events -> {
            medsList.clear();
            equipList.clear();
            for (HelperEvent event : events) {
                if ("Medication".equals(event.getTag())) {
                    // Medications stay the same
                    Map<String, String> extractedData = extractFieldsFromDescription(event.getDescription());
                    medsList.add(new MedsEquipmentItem(
                            event.getID(),
                            event.getTitle(),
                            extractedData.get("Dosage"),
                            event.getStartTime(),
                            event.getDate(),
                            event.getDescription(),
                            extractedData.get("Other Names"),
                            extractedData.get("Special Instructions"),
                            extractedData.get("Bottle Description"),
                            extractedData.get("Side Effects"),
                            extractedData.get("Prescribing Doctor"),
                            extractedData.get("Reason Prescribed"),
                            extractedData.get("Notes"),
                            extractedData.get("Reminder2Weeks"),
                            extractedData.get("Reminder1Week"),
                            extractedData.get("Reminder5Days"),
                            extractedData.get("Reminder3Days"),
                            extractedData.get("Reminder1Day"),
                            extractedData.get("ReminderDayOf"),
                            "Medication"
                    ));
                }
                else if ("Equipment".equals(event.getTag())) {
                    Map<String, String> extractedData = extractFieldsFromDescription(event.getDescription());
                    equipList.add(new MedsEquipmentItem(
                            event.getID(),
                            event.getTitle(),
                            event.getDate(), // Maintenance Date (stored as `date`)
                            event.getDescription(),
                            extractedData.getOrDefault("Serial Number", ""),
                            extractedData.getOrDefault("Weight", ""),
                            extractedData.getOrDefault("Size", ""),
                            extractedData.getOrDefault("Prescribing Doctor", ""),
                            extractedData.getOrDefault("Date Prescribed", ""),
                            extractedData.getOrDefault("Insurance Used", ""),
                            extractedData.getOrDefault("Date Purchased", ""),
                            extractedData.getOrDefault("Good Through", ""),
                            extractedData.getOrDefault("Replacement Available On", ""),
                            extractedData.getOrDefault("Equipment Provider", ""),
                            extractedData.getOrDefault("Loaned or Purchased", ""),
                            extractedData.getOrDefault("Maintenance Provider", ""),
                            extractedData.getOrDefault("Last Maintenance", ""),
                            extractedData.getOrDefault("Spare Parts/Tools", ""),
                            extractedData.getOrDefault("Associated Components", ""),
                            extractedData.getOrDefault("Notes", ""),
                            extractedData.getOrDefault("Reminder2Weeks", "false"),
                            extractedData.getOrDefault("Reminder1Week", "false"),
                            extractedData.getOrDefault("Reminder5Days", "false"),
                            extractedData.getOrDefault("Reminder3Days", "false"),
                            extractedData.getOrDefault("Reminder1Day", "false"),
                            extractedData.getOrDefault("ReminderDayOf", "false"),
                            "Equipment"
                    ));
                }
                else if ("Supplies".equals(event.getTag())) {
                    Map<String, String> extractedData = extractFieldsFromDescription(event.getDescription());
                    suppliesList.add(new MedsEquipmentItem(
                            event.getID(),
                            event.getTitle(),
                            event.getDate(), // Next Order Date
                            event.getDescription(),
                            extractedData.getOrDefault("Preferred Brand", ""),
                            extractedData.getOrDefault("Alternative Brands", ""),
                            extractedData.getOrDefault("SKU", ""),
                            extractedData.getOrDefault("Size", ""),
                            extractedData.getOrDefault("Order Quantity", ""),
                            extractedData.getOrDefault("Order Frequency", ""),
                            extractedData.getOrDefault("Refills Remaining", ""),
                            extractedData.getOrDefault("Medical Supply Company", ""),
                            extractedData.getOrDefault("Supply Company Phone", ""),
                            extractedData.getOrDefault("Supply Company Address", ""),
                            extractedData.getOrDefault("Prescribing Doctor", ""),
                            extractedData.getOrDefault("Date Prescribed", ""),
                            extractedData.getOrDefault("Insurance Used", ""),
                            extractedData.getOrDefault("Last Order Date", ""),
                            extractedData.getOrDefault("Expected Delivery Date", ""),
                            extractedData.getOrDefault("Expiry Date", ""),
                            extractedData.getOrDefault("Alternate Medical Supply Sources", ""),
                            extractedData.getOrDefault("Notes", ""),
                            extractedData.getOrDefault("Reminder2Weeks", "false"),
                            extractedData.getOrDefault("Reminder1Week", "false"),
                            extractedData.getOrDefault("Reminder5Days", "false"),
                            extractedData.getOrDefault("Reminder3Days", "false"),
                            extractedData.getOrDefault("Reminder1Day", "false"),
                            extractedData.getOrDefault("ReminderDayOf", "false"),
                            "Supplies"
                    ));
                }
            }
            medsAdapter.notifyDataSetChanged();
            equipAdapter.notifyDataSetChanged();
            suppliesAdapter.notifyDataSetChanged();
        });
    }

    // Helper function to extract dosage if stored in description
    private Map<String, String> extractFieldsFromDescription(String description) {
        Map<String, String> data = new HashMap<>();

        String[] parts = description.split("\\|"); // Split by "|"
        for (String part : parts) {
            String[] keyValue = part.split(": ", 2); // Split key and value
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // Convert "true" or "false" into actual Boolean values
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    data.put(key, Boolean.parseBoolean(value) ? "true" : "false");
                } else {
                    data.put(key, value);
                }
            }
        }

        return data;
    }

    private void addMedEventToCalendar(String title, String dosage, String date, String tag,
                                    String otherNames, String specialInstructions, String bottleDescription,
                                    String sideEffects, String prescribingDoctor, String reasonPrescribed, String notes, String takeTime,
                                    String reminder2Weeks, String reminder1Week, String reminder5Days,
                                    String reminder3Days, String reminder1Day, String reminderDayOf) {

        // Store additional fields inside description
        String formattedDescription = String.format(
                "Dosage: %s | Other Names: %s | Special Instructions: %s | Bottle Description: %s | Side Effects: %s | " +
                        "Prescribing Doctor: %s | Reason Prescribed: %s | Notes: %s | Reminder2Weeks: %s | Reminder1Week: %s " +
                        "| Reminder5Days: %s | Reminder3Days: %s | Reminder1Day: %s | ReminderDayOf: %s",
                dosage, otherNames, specialInstructions, bottleDescription, sideEffects,
                prescribingDoctor, reasonPrescribed, notes, reminder2Weeks, reminder1Week,
                reminder5Days, reminder3Days, reminder1Day, reminderDayOf
        );

        HelperEvent event = new HelperEvent(
                UUID.randomUUID().toString(), title, formattedDescription,
                date, takeTime, takeTime, tag,
                "No Repeat", 0, "DONT SHOW EDIT/DELETE", currentUserSession
        );

        eventViewModel.insert(event);
    }

    private void addEquipEventToCalendar(String name, String maintenanceDate,
                                         String serialNumber, String weight, String size, String prescribingDoctor,
                                         String datePrescribed, String insuranceUsed, String datePurchased, String goodThrough,
                                         String replacementAvailableOn, String equipmentProvider, String loanedOrPurchased,
                                         String maintenanceProvider, String lastMaintenance, String sparePartsTools,
                                         String associatedComponents, String equipmentNotes,
                                         String reminder2Weeks, String reminder1Week, String reminder5Days,
                                         String reminder3Days, String reminder1Day, String reminderDayOf) {

        // Create formatted description string with all equipment fields
        String formattedDescription = String.format(
                "Serial Number: %s | Weight: %s | Size: %s | Prescribing Doctor: %s | Date Prescribed: %s | " +
                        "Insurance Used: %s | Date Purchased: %s | Good Through: %s | Replacement Available On: %s | " +
                        "Equipment Provider: %s | Loaned or Purchased: %s | Maintenance Provider: %s | Last Maintenance: %s | " +
                        "Spare Parts/Tools: %s | Associated Components: %s | Notes: %s | Reminder2Weeks: %s | Reminder1Week: %s " +
                        "| Reminder5Days: %s | Reminder3Days: %s | Reminder1Day: %s | ReminderDayOf: %s",
                emptyIfNull(serialNumber), emptyIfNull(weight), emptyIfNull(size), emptyIfNull(prescribingDoctor), emptyIfNull(datePrescribed),
                emptyIfNull(insuranceUsed), emptyIfNull(datePurchased), emptyIfNull(goodThrough), emptyIfNull(replacementAvailableOn),
                emptyIfNull(equipmentProvider), emptyIfNull(loanedOrPurchased), emptyIfNull(maintenanceProvider), emptyIfNull(lastMaintenance),
                emptyIfNull(sparePartsTools), emptyIfNull(associatedComponents), emptyIfNull(equipmentNotes),
                reminder2Weeks, reminder1Week, reminder5Days, reminder3Days, reminder1Day, reminderDayOf
        );

        HelperEvent event = new HelperEvent(
                UUID.randomUUID().toString(),
                name,
                formattedDescription,
                maintenanceDate, // Stored as event date (maintenance date)
                "08:00", "08:30",
                "Equipment",
                "No Repeat", 0, "DONT SHOW EDIT/DELETE", currentUserSession
        );

        eventViewModel.insert(event);
    }

    private void addSupplyEventToCalendar(String name, String nextOrderDate,
                                          String preferredBrand, String alternativeBrands, String sku, String size,
                                          String orderQuantity, String orderFrequency, String refillsRemaining,
                                          String supplyCompany, String supplyCompanyPhone, String supplyCompanyAddress,
                                          String prescribingDoctor, String datePrescribed, String insuranceUsed,
                                          String lastOrderDate, String expectedDeliveryDate, String expiryDate,
                                          String alternateSources, String supplyNotes,
                                          String reminder2Weeks, String reminder1Week, String reminder5Days,
                                          String reminder3Days, String reminder1Day, String reminderDayOf) {

        String formattedDescription = String.format(
                "Preferred Brand: %s | Alternative Brands: %s | SKU: %s | Size: %s | Order Quantity: %s | Order Frequency: %s | " +
                        "Refills Remaining: %s | Medical Supply Company: %s | Supply Company Phone: %s | Supply Company Address: %s | " +
                        "Prescribing Doctor: %s | Date Prescribed: %s | Insurance Used: %s | Last Order Date: %s | Expected Delivery Date: %s | " +
                        "Expiry Date: %s | Alternate Medical Supply Sources: %s | Notes: %s | Reminder2Weeks: %s | Reminder1Week: %s | " +
                        "Reminder5Days: %s | Reminder3Days: %s | Reminder1Day: %s | ReminderDayOf: %s",
                preferredBrand, alternativeBrands, sku, size, orderQuantity, orderFrequency,
                refillsRemaining, supplyCompany, supplyCompanyPhone, supplyCompanyAddress,
                prescribingDoctor, datePrescribed, insuranceUsed, lastOrderDate, expectedDeliveryDate,
                expiryDate, alternateSources, supplyNotes,
                reminder2Weeks, reminder1Week, reminder5Days, reminder3Days, reminder1Day, reminderDayOf
        );

        HelperEvent event = new HelperEvent(
                UUID.randomUUID().toString(),
                name,
                formattedDescription,
                nextOrderDate,
                "08:00", "08:30",
                "Supplies",
                "No Repeat", 0, "DONT SHOW EDIT/DELETE", currentUserSession
        );

        eventViewModel.insert(event);
    }

    private void scheduleDailyReminders(String eventId, String refillDate, String reminderTimes) {
        if (reminderTimes.isEmpty()) return;

        String[] reminderArray = reminderTimes.split(",");
        Calendar currentDate = Calendar.getInstance();
        Calendar endRefillDate = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedRefillDate = sdf.parse(refillDate);

            if (parsedRefillDate != null) {
                endRefillDate.setTime(parsedRefillDate);
            }

            while (currentDate.before(endRefillDate)) {
                for (String reminderTime : reminderArray) {
                    int timeOffset = Integer.parseInt(reminderTime);

                    Calendar reminderCalendar = (Calendar) currentDate.clone();
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, timeOffset);
                    reminderCalendar.set(Calendar.MINUTE, 0);
                    reminderCalendar.set(Calendar.SECOND, 0);

                    scheduleNotification(eventId, reminderCalendar.getTimeInMillis());
                }
                currentDate.add(Calendar.DAY_OF_YEAR, 1);  // Move to the next day
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void askToContinueReminders(String eventId) {
        new AlertDialog.Builder(this)
                .setTitle("Continue Medication Reminders?")
                .setMessage("Your refill date has passed. Do you want to continue receiving daily reminders?")
                .setPositiveButton("Yes, Continue", (dialog, which) -> {
                    // Reschedule reminders indefinitely
                    scheduleDailyReminders(eventId, "9999-12-31", "8,14,20");  // Example: Morning, Afternoon, Night
                    Toast.makeText(this, "Reminders will continue!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No, Stop Reminders", (dialog, which) -> {
                    cancelReminders(eventId);
                    Toast.makeText(this, "Reminders stopped.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void cancelReminders(String eventId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, eventId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


    private void scheduleNotification(String eventId, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("eventId", eventId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, eventId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,  // Repeat every 24 hours
                pendingIntent
        );
    }

    private void showMedicationDialog() {
        final String[] selectedTime = {""};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medication, null);
        builder.setView(view);

        // Initialize the dialog
        AlertDialog dialog = builder.create();

        // Required Fields
        EditText nameInput = view.findViewById(R.id.med_name_input);
        EditText dosageInput = view.findViewById(R.id.med_dosage_input);
        Button selectRefillDateButton = view.findViewById(R.id.med_refill_date_button);
        Button saveButton = view.findViewById(R.id.save_med_button);
        Button cancelButton = view.findViewById(R.id.cancel_med_button);

        // Other Fields
        EditText otherNamesInput = view.findViewById(R.id.med_other_names_input);
        EditText instructionsInput = view.findViewById(R.id.med_special_instructions);
        EditText bottleInput = view.findViewById(R.id.med_bottle_description);
        EditText sideEffectsInput = view.findViewById(R.id.med_side_effects);
        EditText doctorInput = view.findViewById(R.id.med_prescribing_doctor);
        EditText reasonPrescribedInput = view.findViewById(R.id.med_reason_prescribed);
        EditText notesInput = view.findViewById(R.id.med_notes);

        Button addTimeButton = view.findViewById(R.id.add_time_button);
        LinearLayout timeSelectionContainer = view.findViewById(R.id.time_selection_container);


        // Reminder Checkboxes
        CheckBox reminder2Weeks = view.findViewById(R.id.reminder_2_weeks);
        CheckBox reminder1Week = view.findViewById(R.id.reminder_1_week);
        CheckBox reminder5Days = view.findViewById(R.id.reminder_5_days);
        CheckBox reminder3Days = view.findViewById(R.id.reminder_3_days);
        CheckBox reminder1Day = view.findViewById(R.id.reminder_1_day);
        CheckBox reminderDayOf = view.findViewById(R.id.reminder_day_of);

        final String[] selectedDate = {""};
        selectRefillDateButton.setOnClickListener(v -> showDatePickerDialog(selectedDate, selectRefillDateButton));


        //This is kinda broken
        addTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, hourOfDay, minute1) -> {
                        selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        addTimeButton.setText(selectedTime[0]); // Update button text
                    }, hour, minute, true);

            timePickerDialog.show();
        });


        // **Prevent Saving If Required Fields Are Empty**
        saveButton.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isNameFilled = !nameInput.getText().toString().trim().isEmpty();
                boolean isDosageFilled = !dosageInput.getText().toString().trim().isEmpty();
                saveButton.setEnabled(isNameFilled && isDosageFilled);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        nameInput.addTextChangedListener(textWatcher);
        dosageInput.addTextChangedListener(textWatcher);

        // Handle Save Button Click
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String dosage = dosageInput.getText().toString().trim();
            String otherNames = otherNamesInput.getText().toString().trim();
            String specialInstructions = instructionsInput.getText().toString().trim();
            String bottleDescription = bottleInput.getText().toString().trim();
            String sideEffects = sideEffectsInput.getText().toString().trim();
            String prescribingDoctor = doctorInput.getText().toString().trim();
            String reasonPrescribed = reasonPrescribedInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();
            String reminder2WeeksStr = reminder2Weeks.isChecked() ? "true" : "false";
            String reminder1WeekStr = reminder1Week.isChecked() ? "true" : "false";
            String reminder5DaysStr = reminder5Days.isChecked() ? "true" : "false";
            String reminder3DaysStr = reminder3Days.isChecked() ? "true" : "false";
            String reminder1DayStr = reminder1Day.isChecked() ? "true" : "false";
            String reminderDayOfStr = reminderDayOf.isChecked() ? "true" : "false";

            if (name.isEmpty() || dosage.isEmpty() || selectedTime[0].isEmpty()) {
                if (name.isEmpty()) nameInput.setBackgroundColor(Color.RED);
                if (dosage.isEmpty()) dosageInput.setBackgroundColor(Color.RED);
                if (selectedTime[0].isEmpty()) addTimeButton.setBackgroundColor(Color.RED);
                Toast.makeText(this, "Please fill in required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            String takeTime = selectedTime[0];

            // Collect selected reminders
            List<String> reminders = new ArrayList<>();
            if (reminder2Weeks.isChecked()) reminders.add("14");
            if (reminder1Week.isChecked()) reminders.add("7");
            if (reminder5Days.isChecked()) reminders.add("5");
            if (reminder3Days.isChecked()) reminders.add("3");
            if (reminder1Day.isChecked()) reminders.add("1");
            if (reminderDayOf.isChecked()) reminders.add("0");

            // Pass the new times to addMedEventToCalendar
            addMedEventToCalendar(name, dosage, selectedDate[0], "Medication",
                    otherNames, specialInstructions, bottleDescription,
                    sideEffects, prescribingDoctor, reasonPrescribed, notes,
                    takeTime, reminder2WeeksStr, reminder1WeekStr, reminder5DaysStr,
                    reminder3DaysStr, reminder1DayStr, reminderDayOfStr); // Start and End time updated

            Toast.makeText(this, "Medication added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });


        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showMedicationDetailsDialog(MedsEquipmentItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medication, null);
        builder.setView(view);

        final String[] selectedTime = {item != null ? item.getTime() : "08:00"}; // Default to "08:00" if no value exists

        boolean reminder2WeeksBool = Objects.requireNonNull(item).getReminder2Weeks().equals("true");
        boolean reminder1WeekBool = Objects.requireNonNull(item).getReminder1Week().equals("true");
        boolean reminder5DaysBool = Objects.requireNonNull(item).getReminder5Days().equals("true");
        boolean reminder3DaysBool = Objects.requireNonNull(item).getReminder3Days().equals("true");
        boolean reminder1DayBool = Objects.requireNonNull(item).getReminder1Day().equals("true");
        boolean reminderDayOfBool = Objects.requireNonNull(item).getReminderDayOf().equals("true");

        // Initialize the dialog
        AlertDialog dialog = builder.create();

        // Declare UI elements (EditTexts, Buttons, Spinners)
        EditText nameInput = view.findViewById(R.id.med_name_input);
        EditText dosageInput = view.findViewById(R.id.med_dosage_input);
        EditText otherNamesInput = view.findViewById(R.id.med_other_names_input);
        EditText instructionsInput = view.findViewById(R.id.med_special_instructions);
        EditText bottleInput = view.findViewById(R.id.med_bottle_description);
        EditText sideEffectsInput = view.findViewById(R.id.med_side_effects);
        EditText doctorInput = view.findViewById(R.id.med_prescribing_doctor);
        EditText reasonPrescribedInput = view.findViewById(R.id.med_reason_prescribed);
        EditText notesInput = view.findViewById(R.id.med_notes);

        Button selectRefillDateButton = view.findViewById(R.id.med_refill_date_button);
        Button saveButton = view.findViewById(R.id.save_med_button);
        Button cancelButton = view.findViewById(R.id.cancel_med_button);
        Button addTimeButton = view.findViewById(R.id.add_time_button);


        // Reminder Checkboxes
        CheckBox reminder2Weeks = view.findViewById(R.id.reminder_2_weeks);
        CheckBox reminder1Week = view.findViewById(R.id.reminder_1_week);
        CheckBox reminder5Days = view.findViewById(R.id.reminder_5_days);
        CheckBox reminder3Days = view.findViewById(R.id.reminder_3_days);
        CheckBox reminder1Day = view.findViewById(R.id.reminder_1_day);
        CheckBox reminderDayOf = view.findViewById(R.id.reminder_day_of);

        // Set existing values
        nameInput.setText(item.getName());
        dosageInput.setText(item.getDosage());
        otherNamesInput.setText(item.getOtherNames());
        instructionsInput.setText(item.getSpecialInstructions());
        bottleInput.setText(item.getBottleDescription());
        sideEffectsInput.setText(item.getSideEffects());
        doctorInput.setText(item.getPrescribingDoctor());
        reasonPrescribedInput.setText(item.getReasonPrescribed());
        notesInput.setText(item.getNotes());
        addTimeButton.setText(selectedTime[0]);
        reminder2Weeks.setChecked(reminder2WeeksBool);
        reminder1Week.setChecked(reminder1WeekBool);
        reminder5Days.setChecked(reminder5DaysBool);
        reminder3Days.setChecked(reminder3DaysBool);
        reminder1Day.setChecked(reminder1DayBool);
        reminderDayOf.setChecked(reminderDayOfBool);

        final String[] refillDate = {item.getDate()};

        selectRefillDateButton.setText(refillDate[0]);
        selectRefillDateButton.setOnClickListener(v -> showDatePickerDialog(refillDate, selectRefillDateButton));

        // Convert time selection into multiple buttons
        addTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, hourOfDay, minute1) -> {
                        selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        addTimeButton.setText(selectedTime[0]); // Update button text
                    }, hour, minute, true);

            timePickerDialog.show();
        });

        // Handle Save Button Click
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String dosage = dosageInput.getText().toString().trim();
            String otherNames = otherNamesInput.getText().toString().trim();
            String specialInstructions = instructionsInput.getText().toString().trim();
            String bottleDescription = bottleInput.getText().toString().trim();
            String sideEffects = sideEffectsInput.getText().toString().trim();
            String prescribingDoctor = doctorInput.getText().toString().trim();
            String reasonPrescribed = reasonPrescribedInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            if (name.isEmpty() || dosage.isEmpty()) {
                if (name.isEmpty()) nameInput.setBackgroundColor(Color.RED);
                if (dosage.isEmpty()) dosageInput.setBackgroundColor(Color.RED);
                Toast.makeText(this, "Please fill in required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect selected reminders
            List<String> reminders = new ArrayList<>();
            if (reminder2Weeks.isChecked()) reminders.add("14");
            if (reminder1Week.isChecked()) reminders.add("7");
            if (reminder5Days.isChecked()) reminders.add("5");
            if (reminder3Days.isChecked()) reminders.add("3");
            if (reminder1Day.isChecked()) reminders.add("1");
            if (reminderDayOf.isChecked()) reminders.add("0");

            // Save changes to the item
            item.setName(name);
            item.setDosage(dosage);
            item.setOtherNames(otherNames);
            item.setSpecialInstructions(specialInstructions);
            item.setBottleDescription(bottleDescription);
            item.setSideEffects(sideEffects);
            item.setPrescribingDoctor(prescribingDoctor);
            item.setReasonPrescribed(reasonPrescribed);
            item.setNotes(notes);
            item.setDate(refillDate[0]);
            item.setTime(selectedTime[0]);
            item.setReminder2Weeks(reminder2Weeks.isChecked() ? "true" : "false");
            item.setReminder1Week(reminder1Week.isChecked() ? "true" : "false");
            item.setReminder5Days(reminder5Days.isChecked() ? "true" : "false");
            item.setReminder3Days(reminder3Days.isChecked() ? "true" : "false");
            item.setReminder1Day(reminder1Day.isChecked() ? "true" : "false");
            item.setReminderDayOf(reminderDayOf.isChecked() ? "true" : "false");

            // Update the event
            updateMedication(item);

            Toast.makeText(this, "Medication updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void updateMedication(MedsEquipmentItem item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            Log.e("updateMedication", "Cannot update: Missing event ID.");
            return;
        }

        // INCLUDE REMINDERS in the formatted description
        String formattedDescription = String.format(
                "Dosage: %s | Other Names: %s | Special Instructions: %s | Bottle Description: %s | Side Effects: %s | " +
                        "Prescribing Doctor: %s | Reason Prescribed: %s | Notes: %s | Reminder2Weeks: %s | Reminder1Week: %s " +
                        "| Reminder5Days: %s | Reminder3Days: %s | Reminder1Day: %s | ReminderDayOf: %s",
                item.getDosage(), item.getOtherNames(), item.getSpecialInstructions(), item.getBottleDescription(),
                item.getSideEffects(), item.getPrescribingDoctor(), item.getReasonPrescribed(), item.getNotes(),
                item.getReminder2Weeks(), item.getReminder1Week(), item.getReminder5Days(),
                item.getReminder3Days(), item.getReminder1Day(), item.getReminderDayOf()
        );

        HelperEvent updatedEvent = new HelperEvent(
                item.getId(),
                item.getName(),
                formattedDescription,
                item.getDate(),
                item.getTime(),  // Pass the correct time
                item.getTime(),  // Start & end time same
                "Medication",
                "No Repeat", 0, "DONT SHOW EDIT/DELETE", currentUserSession
        );

        Log.d("MedsAndEquipmentTrackerPage", "Updating medication event: " + updatedEvent.getID());

        eventViewModel.update(updatedEvent);

        // Refresh UI
        runOnUiThread(() -> medsAdapter.notifyDataSetChanged());
    }

    private void showEquipmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_equipment, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // UI elements
        EditText nameInput = view.findViewById(R.id.equip_name_input);
        EditText serialInput = view.findViewById(R.id.equip_serial_number_input);
        EditText weightInput = view.findViewById(R.id.equip_weight_input);
        EditText sizeInput = view.findViewById(R.id.equip_size_input);
        EditText prescribingDoctorInput = view.findViewById(R.id.equip_prescribing_doctor_input);
        EditText insuranceInput = view.findViewById(R.id.equip_insurance_used_input);
        EditText equipmentProviderInput = view.findViewById(R.id.equip_provider_input);
        EditText loanedPurchasedInput = view.findViewById(R.id.equip_loaned_or_purchased_input);
        EditText maintenanceProviderInput = view.findViewById(R.id.equip_maintenance_provider_input);
        EditText sparePartsInput = view.findViewById(R.id.equip_spare_parts_tools_input);
        EditText associatedComponentsInput = view.findViewById(R.id.equip_associated_components_input);
        EditText notesInput = view.findViewById(R.id.equip_notes_input);

        Button selectMaintenanceDateButton = view.findViewById(R.id.equip_maintenance_date_button);
        Button selectDatePrescribedButton = view.findViewById(R.id.equip_date_prescribed_button);
        Button selectDatePurchasedButton = view.findViewById(R.id.equip_date_purchased_button);
        Button selectGoodThroughButton = view.findViewById(R.id.equip_good_through_button);
        Button selectReplacementAvailableButton = view.findViewById(R.id.equip_replacement_available_button);
        Button selectLastMaintenanceButton = view.findViewById(R.id.equip_last_maintenance_button); // NEW

        Button saveButton = view.findViewById(R.id.save_equip_button);
        Button cancelButton = view.findViewById(R.id.cancel_equip_button);

        CheckBox reminder2Weeks = view.findViewById(R.id.reminder_2_weeks);
        CheckBox reminder1Week = view.findViewById(R.id.reminder_1_week);
        CheckBox reminder5Days = view.findViewById(R.id.reminder_5_days);
        CheckBox reminder3Days = view.findViewById(R.id.reminder_3_days);
        CheckBox reminder1Day = view.findViewById(R.id.reminder_1_day);
        CheckBox reminderDayOf = view.findViewById(R.id.reminder_day_of);

        // Dates
        final String[] maintenanceDate = {""};
        final String[] datePrescribed = {""};
        final String[] datePurchased = {""};
        final String[] goodThrough = {""};
        final String[] replacementAvailable = {""};
        final String[] lastMaintenance = {""}; // NEW

        // Date pickers
        selectMaintenanceDateButton.setOnClickListener(v -> showDatePickerDialog(maintenanceDate, selectMaintenanceDateButton));
        selectDatePrescribedButton.setOnClickListener(v -> showDatePickerDialog(datePrescribed, selectDatePrescribedButton));
        selectDatePurchasedButton.setOnClickListener(v -> showDatePickerDialog(datePurchased, selectDatePurchasedButton));
        selectGoodThroughButton.setOnClickListener(v -> showDatePickerDialog(goodThrough, selectGoodThroughButton));
        selectReplacementAvailableButton.setOnClickListener(v -> showDatePickerDialog(replacementAvailable, selectReplacementAvailableButton));
        selectLastMaintenanceButton.setOnClickListener(v -> showDatePickerDialog(lastMaintenance, selectLastMaintenanceButton)); // NEW

        // Validation logic
        saveButton.setEnabled(false);

        Runnable checkRequiredFields = () -> {
            boolean isNameFilled = !nameInput.getText().toString().trim().isEmpty();
            saveButton.setEnabled(isNameFilled);
        };

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkRequiredFields.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save button logic
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            if (name.isEmpty() || maintenanceDate[0].isEmpty()) {
                Toast.makeText(this, "Please fill required fields!", Toast.LENGTH_SHORT).show();
                if (name.isEmpty()) nameInput.setBackgroundColor(Color.RED);
                if (maintenanceDate[0].isEmpty()) selectMaintenanceDateButton.setBackgroundColor(Color.RED);
                return;
            }

            addEquipEventToCalendar(
                    name,
                    maintenanceDate[0],
                    serialInput.getText().toString().trim(),
                    weightInput.getText().toString().trim(),
                    sizeInput.getText().toString().trim(),
                    prescribingDoctorInput.getText().toString().trim(),
                    datePrescribed[0],
                    insuranceInput.getText().toString().trim(),
                    datePurchased[0],
                    goodThrough[0],
                    replacementAvailable[0],
                    equipmentProviderInput.getText().toString().trim(),
                    loanedPurchasedInput.getText().toString().trim(),
                    maintenanceProviderInput.getText().toString().trim(),
                    lastMaintenance[0], // NEW
                    sparePartsInput.getText().toString().trim(),
                    associatedComponentsInput.getText().toString().trim(),
                    notesInput.getText().toString().trim(),
                    reminder2Weeks.isChecked() ? "true" : "false",
                    reminder1Week.isChecked() ? "true" : "false",
                    reminder5Days.isChecked() ? "true" : "false",
                    reminder3Days.isChecked() ? "true" : "false",
                    reminder1Day.isChecked() ? "true" : "false",
                    reminderDayOf.isChecked() ? "true" : "false"
            );

            Toast.makeText(this, "Equipment added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showEquipmentDetailsDialog(MedsEquipmentItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_equipment, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // UI elements
        EditText nameInput = view.findViewById(R.id.equip_name_input);
        EditText serialInput = view.findViewById(R.id.equip_serial_number_input);
        EditText weightInput = view.findViewById(R.id.equip_weight_input);
        EditText sizeInput = view.findViewById(R.id.equip_size_input);
        EditText prescribingDoctorInput = view.findViewById(R.id.equip_prescribing_doctor_input);
        EditText insuranceInput = view.findViewById(R.id.equip_insurance_used_input);
        EditText equipmentProviderInput = view.findViewById(R.id.equip_provider_input);
        EditText loanedPurchasedInput = view.findViewById(R.id.equip_loaned_or_purchased_input);
        EditText maintenanceProviderInput = view.findViewById(R.id.equip_maintenance_provider_input);
        EditText sparePartsInput = view.findViewById(R.id.equip_spare_parts_tools_input);
        EditText associatedComponentsInput = view.findViewById(R.id.equip_associated_components_input);
        EditText notesInput = view.findViewById(R.id.equip_notes_input);

        Button selectMaintenanceDateButton = view.findViewById(R.id.equip_maintenance_date_button);
        Button selectDatePrescribedButton = view.findViewById(R.id.equip_date_prescribed_button);
        Button selectDatePurchasedButton = view.findViewById(R.id.equip_date_purchased_button);
        Button selectGoodThroughButton = view.findViewById(R.id.equip_good_through_button);
        Button selectReplacementAvailableButton = view.findViewById(R.id.equip_replacement_available_button);
        Button selectLastMaintenanceButton = view.findViewById(R.id.equip_last_maintenance_button);


        Button saveButton = view.findViewById(R.id.save_equip_button);
        Button cancelButton = view.findViewById(R.id.cancel_equip_button);

        CheckBox reminder2Weeks = view.findViewById(R.id.reminder_2_weeks);
        CheckBox reminder1Week = view.findViewById(R.id.reminder_1_week);
        CheckBox reminder5Days = view.findViewById(R.id.reminder_5_days);
        CheckBox reminder3Days = view.findViewById(R.id.reminder_3_days);
        CheckBox reminder1Day = view.findViewById(R.id.reminder_1_day);
        CheckBox reminderDayOf = view.findViewById(R.id.reminder_day_of);

        // Fill existing data
        nameInput.setText(item.getName());
        serialInput.setText(item.getSerialNumber());
        weightInput.setText(item.getWeight());
        sizeInput.setText(item.getSize());
        prescribingDoctorInput.setText(item.getPrescribingDoctor());
        insuranceInput.setText(item.getInsuranceUsed());
        equipmentProviderInput.setText(item.getEquipmentProvider());
        loanedPurchasedInput.setText(item.getLoanedOrPurchased());
        maintenanceProviderInput.setText(item.getMaintenanceProvider());
        sparePartsInput.setText(item.getSparePartsTools());
        associatedComponentsInput.setText(item.getAssociatedComponents());
        notesInput.setText(item.getEquipmentNotes());
        selectLastMaintenanceButton.setText(item.getLastMaintenance());

        final String[] maintenanceDate = {item.getDate()};
        final String[] datePrescribed = {item.getDatePrescribed()};
        final String[] datePurchased = {item.getDatePurchased()};
        final String[] goodThrough = {item.getGoodThrough()};
        final String[] replacementAvailable = {item.getReplacementAvailableOn()};
        final String[] lastMaintenance = {item.getLastMaintenance()};

        selectMaintenanceDateButton.setText(maintenanceDate[0]);
        selectDatePrescribedButton.setText(datePrescribed[0]);
        selectDatePurchasedButton.setText(datePurchased[0]);
        selectGoodThroughButton.setText(goodThrough[0]);
        selectReplacementAvailableButton.setText(replacementAvailable[0]);

        selectMaintenanceDateButton.setOnClickListener(v -> showDatePickerDialog(maintenanceDate, selectMaintenanceDateButton));
        selectDatePrescribedButton.setOnClickListener(v -> showDatePickerDialog(datePrescribed, selectDatePrescribedButton));
        selectDatePurchasedButton.setOnClickListener(v -> showDatePickerDialog(datePurchased, selectDatePurchasedButton));
        selectGoodThroughButton.setOnClickListener(v -> showDatePickerDialog(goodThrough, selectGoodThroughButton));
        selectReplacementAvailableButton.setOnClickListener(v -> showDatePickerDialog(replacementAvailable, selectReplacementAvailableButton));
        selectLastMaintenanceButton.setOnClickListener(v -> showDatePickerDialog(lastMaintenance, selectLastMaintenanceButton));


        // Set reminders
        reminder2Weeks.setChecked("true".equals(item.getReminder2Weeks()));
        reminder1Week.setChecked("true".equals(item.getReminder1Week()));
        reminder5Days.setChecked("true".equals(item.getReminder5Days()));
        reminder3Days.setChecked("true".equals(item.getReminder3Days()));
        reminder1Day.setChecked("true".equals(item.getReminder1Day()));
        reminderDayOf.setChecked("true".equals(item.getReminderDayOf()));

        // Validation
        Runnable checkRequiredFields = () -> {
            boolean isNameFilled = !nameInput.getText().toString().trim().isEmpty();
            saveButton.setEnabled(isNameFilled);
        };
        checkRequiredFields.run();

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkRequiredFields.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            if (name.isEmpty() || maintenanceDate[0].isEmpty()) {
                Toast.makeText(this, "Please fill required fields!", Toast.LENGTH_SHORT).show();
                if (name.isEmpty()) nameInput.setBackgroundColor(Color.RED);
                if (maintenanceDate[0].isEmpty()) selectMaintenanceDateButton.setBackgroundColor(Color.RED);
                return;
            }

            // Update fields
            item.setName(name);
            item.setSerialNumber(serialInput.getText().toString().trim());
            item.setWeight(weightInput.getText().toString().trim());
            item.setSize(sizeInput.getText().toString().trim());
            item.setPrescribingDoctor(prescribingDoctorInput.getText().toString().trim());
            item.setInsuranceUsed(insuranceInput.getText().toString().trim());
            item.setEquipmentProvider(equipmentProviderInput.getText().toString().trim());
            item.setLoanedOrPurchased(loanedPurchasedInput.getText().toString().trim());
            item.setMaintenanceProvider(maintenanceProviderInput.getText().toString().trim());
            item.setSparePartsTools(sparePartsInput.getText().toString().trim());
            item.setAssociatedComponents(associatedComponentsInput.getText().toString().trim());
            item.setEquipmentNotes(notesInput.getText().toString().trim());

            item.setDate(maintenanceDate[0]);
            item.setDatePrescribed(datePrescribed[0]);
            item.setDatePurchased(datePurchased[0]);
            item.setGoodThrough(goodThrough[0]);
            item.setReplacementAvailableOn(replacementAvailable[0]);
            item.setLastMaintenance(lastMaintenance[0]);

            item.setReminder2Weeks(reminder2Weeks.isChecked() ? "true" : "false");
            item.setReminder1Week(reminder1Week.isChecked() ? "true" : "false");
            item.setReminder5Days(reminder5Days.isChecked() ? "true" : "false");
            item.setReminder3Days(reminder3Days.isChecked() ? "true" : "false");
            item.setReminder1Day(reminder1Day.isChecked() ? "true" : "false");
            item.setReminderDayOf(reminderDayOf.isChecked() ? "true" : "false");

            updateEquipment(item);

            Toast.makeText(this, "Equipment updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateEquipment(MedsEquipmentItem item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            Log.e("updateEquipment", "Cannot update: Missing event ID.");
            return;
        }

        // Format description with all fields
        String formattedDescription = String.format(
                "Serial Number: %s | Weight: %s | Size: %s | Prescribing Doctor: %s | Date Prescribed: %s | " +
                        "Insurance Used: %s | Date Purchased: %s | Good Through: %s | Replacement Available On: %s | " +
                        "Equipment Provider: %s | Loaned or Purchased: %s | Maintenance Provider: %s | Last Maintenance: %s | " +
                        "Spare Parts/Tools: %s | Associated Components: %s | Notes: %s | Reminder2Weeks: %s | Reminder1Week: %s " +
                        "| Reminder5Days: %s | Reminder3Days: %s | Reminder1Day: %s | ReminderDayOf: %s",
                emptyIfNull(item.getSerialNumber()), emptyIfNull(item.getWeight()), emptyIfNull(item.getSize()), emptyIfNull(item.getPrescribingDoctor()),
                emptyIfNull(item.getDatePrescribed()), emptyIfNull(item.getInsuranceUsed()), emptyIfNull(item.getDatePurchased()),
                emptyIfNull(item.getGoodThrough()), emptyIfNull(item.getReplacementAvailableOn()), emptyIfNull(item.getEquipmentProvider()),
                emptyIfNull(item.getLoanedOrPurchased()), emptyIfNull(item.getMaintenanceProvider()), emptyIfNull(item.getLastMaintenance()),
                emptyIfNull(item.getSparePartsTools()), emptyIfNull(item.getAssociatedComponents()), emptyIfNull(item.getEquipmentNotes()),
                item.getReminder2Weeks(), item.getReminder1Week(), item.getReminder5Days(),
                item.getReminder3Days(), item.getReminder1Day(), item.getReminderDayOf()
        );

        HelperEvent updatedEvent = new HelperEvent(
                item.getId(),
                item.getName(),
                formattedDescription,
                item.getDate(), // Maintenance Date
                "08:00", "08:30",
                "Equipment",
                "No Repeat", 0, "DONT SHOW EDIT/DELETE", currentUserSession
        );

        Log.d("MedsAndEquipmentTrackerPage", "Updating equipment event: " + updatedEvent.getID());

        eventViewModel.update(updatedEvent);

        runOnUiThread(() -> equipAdapter.notifyDataSetChanged());
    }

    private void showSupplyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_supplies, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // UI elements
        EditText nameInput = view.findViewById(R.id.supply_name_input);
        EditText preferredBrandInput = view.findViewById(R.id.supply_preferred_brand_input);
        EditText alternativeBrandsInput = view.findViewById(R.id.supply_alternative_brands_input);
        EditText skuInput = view.findViewById(R.id.supply_sku_input);
        EditText sizeInput = view.findViewById(R.id.supply_size_input);
        EditText orderQuantityInput = view.findViewById(R.id.supply_order_quantity_input);
        EditText orderFrequencyInput = view.findViewById(R.id.supply_order_frequency_input);
        EditText refillsRemainingInput = view.findViewById(R.id.supply_refills_remaining_input);
        EditText supplyCompanyInput = view.findViewById(R.id.supply_company_input);
        EditText supplyCompanyPhoneInput = view.findViewById(R.id.supply_company_phone_input);
        EditText supplyCompanyAddressInput = view.findViewById(R.id.supply_company_address_input);
        EditText prescribingDoctorInput = view.findViewById(R.id.supply_prescribing_doctor_input);
        EditText insuranceUsedInput = view.findViewById(R.id.supply_insurance_used_input);
        EditText alternateSourcesInput = view.findViewById(R.id.supply_alternate_sources_input);
        EditText notesInput = view.findViewById(R.id.supply_notes_input);

        Button selectNextOrderDateButton = view.findViewById(R.id.supply_next_order_date_button);
        Button selectDatePrescribedButton = view.findViewById(R.id.supply_date_prescribed_button);
        Button selectLastOrderDateButton = view.findViewById(R.id.supply_last_order_date_button);
        Button selectExpectedDeliveryDateButton = view.findViewById(R.id.supply_expected_delivery_date_button);
        Button selectExpiryDateButton = view.findViewById(R.id.supply_expiry_date_button);

        Button saveButton = view.findViewById(R.id.save_supply_button);
        Button cancelButton = view.findViewById(R.id.cancel_supply_button);

        CheckBox reminder2Weeks = view.findViewById(R.id.reminder_2_weeks);
        CheckBox reminder1Week = view.findViewById(R.id.reminder_1_week);
        CheckBox reminder5Days = view.findViewById(R.id.reminder_5_days);
        CheckBox reminder3Days = view.findViewById(R.id.reminder_3_days);
        CheckBox reminder1Day = view.findViewById(R.id.reminder_1_day);
        CheckBox reminderDayOf = view.findViewById(R.id.reminder_day_of);

        // Date values
        final String[] nextOrderDate = {""};
        final String[] datePrescribed = {""};
        final String[] lastOrderDate = {""};
        final String[] expectedDeliveryDate = {""};
        final String[] expiryDate = {""};

        // Date pickers
        selectNextOrderDateButton.setOnClickListener(v -> showDatePickerDialog(nextOrderDate, selectNextOrderDateButton));
        selectDatePrescribedButton.setOnClickListener(v -> showDatePickerDialog(datePrescribed, selectDatePrescribedButton));
        selectLastOrderDateButton.setOnClickListener(v -> showDatePickerDialog(lastOrderDate, selectLastOrderDateButton));
        selectExpectedDeliveryDateButton.setOnClickListener(v -> showDatePickerDialog(expectedDeliveryDate, selectExpectedDeliveryDateButton));
        selectExpiryDateButton.setOnClickListener(v -> showDatePickerDialog(expiryDate, selectExpiryDateButton));

        // Validation logic for required fields
        saveButton.setEnabled(false);
        Runnable checkRequiredFields = () -> {
            boolean isNameFilled = !nameInput.getText().toString().trim().isEmpty();
            saveButton.setEnabled(isNameFilled);
        };

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkRequiredFields.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty() || nextOrderDate[0].isEmpty()) {
                Toast.makeText(this, "Please fill required fields!", Toast.LENGTH_SHORT).show();
                if (name.isEmpty()) nameInput.setBackgroundColor(Color.RED);
                if (nextOrderDate[0].isEmpty()) selectNextOrderDateButton.setBackgroundColor(Color.RED);
                return;
            }

            addSupplyEventToCalendar(
                    name, nextOrderDate[0],
                    preferredBrandInput.getText().toString().trim(),
                    alternativeBrandsInput.getText().toString().trim(),
                    skuInput.getText().toString().trim(),
                    sizeInput.getText().toString().trim(),
                    orderQuantityInput.getText().toString().trim(),
                    orderFrequencyInput.getText().toString().trim(),
                    refillsRemainingInput.getText().toString().trim(),
                    supplyCompanyInput.getText().toString().trim(),
                    supplyCompanyPhoneInput.getText().toString().trim(),
                    supplyCompanyAddressInput.getText().toString().trim(),
                    prescribingDoctorInput.getText().toString().trim(),
                    datePrescribed[0],
                    insuranceUsedInput.getText().toString().trim(),
                    lastOrderDate[0],
                    expectedDeliveryDate[0],
                    expiryDate[0],
                    alternateSourcesInput.getText().toString().trim(),
                    notesInput.getText().toString().trim(),
                    reminder2Weeks.isChecked() ? "true" : "false",
                    reminder1Week.isChecked() ? "true" : "false",
                    reminder5Days.isChecked() ? "true" : "false",
                    reminder3Days.isChecked() ? "true" : "false",
                    reminder1Day.isChecked() ? "true" : "false",
                    reminderDayOf.isChecked() ? "true" : "false"
            );

            Toast.makeText(this, "Supply added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showSupplyDetailsDialog(MedsEquipmentItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_supplies, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // UI elements
        EditText nameInput = view.findViewById(R.id.supply_name_input);
        EditText preferredBrandInput = view.findViewById(R.id.supply_preferred_brand_input);
        EditText alternativeBrandsInput = view.findViewById(R.id.supply_alternative_brands_input);
        EditText skuInput = view.findViewById(R.id.supply_sku_input);
        EditText sizeInput = view.findViewById(R.id.supply_size_input);
        EditText orderQuantityInput = view.findViewById(R.id.supply_order_quantity_input);
        EditText orderFrequencyInput = view.findViewById(R.id.supply_order_frequency_input);
        EditText refillsRemainingInput = view.findViewById(R.id.supply_refills_remaining_input);
        EditText supplyCompanyInput = view.findViewById(R.id.supply_company_input);
        EditText supplyCompanyPhoneInput = view.findViewById(R.id.supply_company_phone_input);
        EditText supplyCompanyAddressInput = view.findViewById(R.id.supply_company_address_input);
        EditText prescribingDoctorInput = view.findViewById(R.id.supply_prescribing_doctor_input);
        EditText insuranceUsedInput = view.findViewById(R.id.supply_insurance_used_input);
        EditText alternateSourcesInput = view.findViewById(R.id.supply_alternate_sources_input);
        EditText notesInput = view.findViewById(R.id.supply_notes_input);

        Button selectNextOrderDateButton = view.findViewById(R.id.supply_next_order_date_button);
        Button selectDatePrescribedButton = view.findViewById(R.id.supply_date_prescribed_button);
        Button selectLastOrderDateButton = view.findViewById(R.id.supply_last_order_date_button);
        Button selectExpectedDeliveryDateButton = view.findViewById(R.id.supply_expected_delivery_date_button);
        Button selectExpiryDateButton = view.findViewById(R.id.supply_expiry_date_button);

        Button saveButton = view.findViewById(R.id.save_supply_button);
        Button cancelButton = view.findViewById(R.id.cancel_supply_button);

        CheckBox reminder2Weeks = view.findViewById(R.id.reminder_2_weeks);
        CheckBox reminder1Week = view.findViewById(R.id.reminder_1_week);
        CheckBox reminder5Days = view.findViewById(R.id.reminder_5_days);
        CheckBox reminder3Days = view.findViewById(R.id.reminder_3_days);
        CheckBox reminder1Day = view.findViewById(R.id.reminder_1_day);
        CheckBox reminderDayOf = view.findViewById(R.id.reminder_day_of);

        // Populate fields
        nameInput.setText(item.getName());
        preferredBrandInput.setText(item.getPreferredBrand());
        alternativeBrandsInput.setText(item.getAlternativeBrands());
        skuInput.setText(item.getSku());
        sizeInput.setText(item.getSize());
        orderQuantityInput.setText(item.getOrderQuantity());
        orderFrequencyInput.setText(item.getOrderFrequency());
        refillsRemainingInput.setText(item.getRefillsRemaining());
        supplyCompanyInput.setText(item.getSupplyCompany());
        supplyCompanyPhoneInput.setText(item.getSupplyCompanyPhone());
        supplyCompanyAddressInput.setText(item.getSupplyCompanyAddress());
        prescribingDoctorInput.setText(item.getPrescribingDoctor());
        insuranceUsedInput.setText(item.getInsuranceUsed());
        alternateSourcesInput.setText(item.getAlternateSources());
        notesInput.setText(item.getSupplyNotes());

        final String[] nextOrderDate = {item.getDate()};
        final String[] datePrescribed = {item.getDatePrescribed()};
        final String[] lastOrderDate = {item.getLastOrderDate()};
        final String[] expectedDeliveryDate = {item.getExpectedDeliveryDate()};
        final String[] expiryDate = {item.getExpiryDate()};

        selectNextOrderDateButton.setText(nextOrderDate[0]);
        selectDatePrescribedButton.setText(datePrescribed[0]);
        selectLastOrderDateButton.setText(lastOrderDate[0]);
        selectExpectedDeliveryDateButton.setText(expectedDeliveryDate[0]);
        selectExpiryDateButton.setText(expiryDate[0]);

        selectNextOrderDateButton.setOnClickListener(v -> showDatePickerDialog(nextOrderDate, selectNextOrderDateButton));
        selectDatePrescribedButton.setOnClickListener(v -> showDatePickerDialog(datePrescribed, selectDatePrescribedButton));
        selectLastOrderDateButton.setOnClickListener(v -> showDatePickerDialog(lastOrderDate, selectLastOrderDateButton));
        selectExpectedDeliveryDateButton.setOnClickListener(v -> showDatePickerDialog(expectedDeliveryDate, selectExpectedDeliveryDateButton));
        selectExpiryDateButton.setOnClickListener(v -> showDatePickerDialog(expiryDate, selectExpiryDateButton));

        // Reminders
        reminder2Weeks.setChecked("true".equals(item.getReminder2Weeks()));
        reminder1Week.setChecked("true".equals(item.getReminder1Week()));
        reminder5Days.setChecked("true".equals(item.getReminder5Days()));
        reminder3Days.setChecked("true".equals(item.getReminder3Days()));
        reminder1Day.setChecked("true".equals(item.getReminder1Day()));
        reminderDayOf.setChecked("true".equals(item.getReminderDayOf()));

        // Validation
        Runnable checkRequiredFields = () -> {
            boolean isNameFilled = !nameInput.getText().toString().trim().isEmpty();
            saveButton.setEnabled(isNameFilled);
        };
        checkRequiredFields.run();

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkRequiredFields.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Save logic
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty() || nextOrderDate[0].isEmpty()) {
                Toast.makeText(this, "Please fill required fields!", Toast.LENGTH_SHORT).show();
                if (name.isEmpty()) nameInput.setBackgroundColor(Color.RED);
                if (nextOrderDate[0].isEmpty()) selectNextOrderDateButton.setBackgroundColor(Color.RED);
                return;
            }

            // Update item
            item.setName(name);
            item.setPreferredBrand(preferredBrandInput.getText().toString().trim());
            item.setAlternativeBrands(alternativeBrandsInput.getText().toString().trim());
            item.setSku(skuInput.getText().toString().trim());
            item.setSize(sizeInput.getText().toString().trim());
            item.setOrderQuantity(orderQuantityInput.getText().toString().trim());
            item.setOrderFrequency(orderFrequencyInput.getText().toString().trim());
            item.setRefillsRemaining(refillsRemainingInput.getText().toString().trim());
            item.setSupplyCompany(supplyCompanyInput.getText().toString().trim());
            item.setSupplyCompanyPhone(supplyCompanyPhoneInput.getText().toString().trim());
            item.setSupplyCompanyAddress(supplyCompanyAddressInput.getText().toString().trim());
            item.setPrescribingDoctor(prescribingDoctorInput.getText().toString().trim());
            item.setInsuranceUsed(insuranceUsedInput.getText().toString().trim());
            item.setAlternateSources(alternateSourcesInput.getText().toString().trim());
            item.setSupplyNotes(notesInput.getText().toString().trim());

            item.setDate(nextOrderDate[0]);
            item.setDatePrescribed(datePrescribed[0]);
            item.setLastOrderDate(lastOrderDate[0]);
            item.setExpectedDeliveryDate(expectedDeliveryDate[0]);
            item.setExpiryDate(expiryDate[0]);

            item.setReminder2Weeks(reminder2Weeks.isChecked() ? "true" : "false");
            item.setReminder1Week(reminder1Week.isChecked() ? "true" : "false");
            item.setReminder5Days(reminder5Days.isChecked() ? "true" : "false");
            item.setReminder3Days(reminder3Days.isChecked() ? "true" : "false");
            item.setReminder1Day(reminder1Day.isChecked() ? "true" : "false");
            item.setReminderDayOf(reminderDayOf.isChecked() ? "true" : "false");

            updateSupply(item);

            Toast.makeText(this, "Supply updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateSupply(MedsEquipmentItem item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            Log.e("updateSupply", "Cannot update: Missing event ID.");
            return;
        }

        String formattedDescription = String.format(
                "Preferred Brand: %s | Alternative Brands: %s | SKU: %s | Size: %s | Order Quantity: %s | Order Frequency: %s | " +
                        "Refills Remaining: %s | Medical Supply Company: %s | Supply Company Phone: %s | Supply Company Address: %s | " +
                        "Prescribing Doctor: %s | Date Prescribed: %s | Insurance Used: %s | Last Order Date: %s | Expected Delivery Date: %s | " +
                        "Expiry Date: %s | Alternate Medical Supply Sources: %s | Notes: %s | Reminder2Weeks: %s | Reminder1Week: %s | " +
                        "Reminder5Days: %s | Reminder3Days: %s | Reminder1Day: %s | ReminderDayOf: %s",
                item.getPreferredBrand(), item.getAlternativeBrands(), item.getSku(), item.getSize(), item.getOrderQuantity(), item.getOrderFrequency(),
                item.getRefillsRemaining(), item.getSupplyCompany(), item.getSupplyCompanyPhone(), item.getSupplyCompanyAddress(),
                item.getPrescribingDoctor(), item.getDatePrescribed(), item.getInsuranceUsed(), item.getLastOrderDate(), item.getExpectedDeliveryDate(),
                item.getExpiryDate(), item.getAlternateSources(), item.getSupplyNotes(),
                item.getReminder2Weeks(), item.getReminder1Week(), item.getReminder5Days(),
                item.getReminder3Days(), item.getReminder1Day(), item.getReminderDayOf()
        );

        HelperEvent updatedEvent = new HelperEvent(
                item.getId(),
                item.getName(),
                formattedDescription,
                item.getDate(),
                "08:00", "08:30",
                "Supplies",
                "No Repeat", 0, "DONT SHOW EDIT/DELETE", currentUserSession
        );

        Log.d("MedsAndEquipmentTrackerPage", "Updating supply event: " + updatedEvent.getID());

        eventViewModel.update(updatedEvent);

        runOnUiThread(() -> suppliesAdapter.notifyDataSetChanged());
    }

    // Helper method to avoid null values
    private String emptyIfNull(String input) {
        return (input == null) ? "" : input;
    }


    public void deleteMedsOrEquipment(MedsEquipmentItem item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            Log.e("deleteMedsOrEquipment", "Cannot delete: Missing event ID.");
            return;
        }

        observeOnce(eventViewModel.getEventById(item.getId()), this, event -> {
            if (event != null) {
                eventViewModel.delete(event);
                Toast.makeText(getApplicationContext(), "Item deleted!", Toast.LENGTH_SHORT).show();

                // Remove from correct list
                medsList.removeIf(med -> med.getId().trim().equals(item.getId().trim()));
                equipList.removeIf(equip -> equip.getId().trim().equals(item.getId().trim()));
                suppliesList.removeIf(supply -> supply.getId().trim().equals(item.getId().trim()));

                runOnUiThread(() -> {
                    medsAdapter.notifyDataSetChanged();
                    equipAdapter.notifyDataSetChanged();
                    suppliesAdapter.notifyDataSetChanged();
                });
            } else {
                Log.e("deleteMedsOrEquipment", "Item not found in database (possibly already deleted).");
                Toast.makeText(getApplicationContext(), "Error: Item not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(String[] selectedDate, Button button) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDate[0] = year + "-" + (month + 1) + "-" + dayOfMonth;
                    button.setText(selectedDate[0]);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public <T> void observeOnce(final androidx.lifecycle.LiveData<T> liveData, androidx.lifecycle.LifecycleOwner owner, final Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this); // Immediately remove observer
            }
        });
    }

}