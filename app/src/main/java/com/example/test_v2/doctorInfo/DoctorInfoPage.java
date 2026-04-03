package com.example.test_v2.doctorInfo;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.example.test_v2.homeIntroLogin.HomePage;

public class DoctorInfoPage extends AppCompatActivity {

    DoctorViewModel doctorViewModel;
    private DoctorAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_info_page);

        recyclerView = findViewById(R.id.doctor_recycler_view);
        Button backButton = findViewById(R.id.back_button);
        Button addDoctorButton = findViewById(R.id.add_doctor_button);
        EditText searchBar = findViewById(R.id.doctor_search_bar);

        adapter = new DoctorAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);
        doctorViewModel.getAllDoctors().observe(this, doctors -> {
            adapter.setDoctorList(doctors);

            // 如果从 Calendar 跳转过来，滚动到对应医生
            int highlightId = getIntent().getIntExtra("highlightDoctorId", -1);
            if (highlightId != -1) {
                for (int i = 0; i < doctors.size(); i++) {
                    if (doctors.get(i).id == highlightId) {
                        recyclerView.scrollToPosition(i);
                        break;
                    }
                }
            }
        });

        // Search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Back button
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePage.class));
            finish();
        });

        // Add button
        addDoctorButton.setOnClickListener(v -> showDoctorDialog(null));

        // Swipe left to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_ID) return;
                DoctorItem item = adapter.getItemAt(position);
                new AlertDialog.Builder(DoctorInfoPage.this)
                        .setTitle("Delete Doctor")
                        .setMessage("Are you sure you want to delete " + item.name + "?")
                        .setPositiveButton("Delete", (dialog, which) ->
                                doctorViewModel.delete(item))
                        .setNegativeButton("Cancel", (dialog, which) ->
                                adapter.notifyItemChanged(position))
                        .setOnCancelListener(dialog ->
                                adapter.notifyItemChanged(position))
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#E53935"));
                c.drawRect(
                        itemView.getRight() + dX,
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom(),
                        paint
                );

                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(40f);
                textPaint.setTypeface(Typeface.DEFAULT_BOLD);
                textPaint.setAntiAlias(true);
                float textWidth = textPaint.measureText("Delete");
                float x = itemView.getRight() - textWidth - 40f;
                float y = itemView.getTop() + (itemView.getHeight() / 2f) + 14f;
                c.drawText("Delete", x, y, textPaint);

                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    public void showDoctorDialog(DoctorItem doctorItem) {
        DoctorDialogFragment dialogFragment = DoctorDialogFragment.newInstance(doctorItem);
        dialogFragment.show(getSupportFragmentManager(), "doctor_dialog");
    }
}