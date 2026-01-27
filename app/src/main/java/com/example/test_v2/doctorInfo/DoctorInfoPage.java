package com.example.test_v2.doctorInfo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import com.example.test_v2.R;
import com.example.test_v2.homeIntroLogin.HomePage;

public class DoctorInfoPage extends AppCompatActivity{

    DoctorViewModel doctorViewModel;
    private DoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_info_page);

        RecyclerView recyclerView = findViewById(R.id.doctor_recycler_view);
        Button backButton = findViewById(R.id.back_button);
        Button addDoctorButton = findViewById(R.id.add_doctor_button);
        EditText searchBar = findViewById(R.id.doctor_search_bar); // NEW: EditText-based search

        adapter = new DoctorAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);
        doctorViewModel.getAllDoctors().observe(this, adapter::setDoctorList);

        // Search logic using EditText instead of SearchView
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePage.class));
            finish();
        });

        addDoctorButton.setOnClickListener(v -> showDoctorDialog(null));
    }

    public void showDoctorDialog(DoctorItem doctorItem) {
        DoctorDialogFragment dialogFragment = DoctorDialogFragment.newInstance(doctorItem);
        dialogFragment.show(getSupportFragmentManager(), "doctor_dialog");
    }
}