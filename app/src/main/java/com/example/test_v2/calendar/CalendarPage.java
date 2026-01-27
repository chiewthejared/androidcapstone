package com.example.test_v2.calendar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.test_v2.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CalendarPage extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private CalendarPagerAdapter adapter;
    private final String[] tabTitles = new String[]{"Daily", "Weekly", "Monthly"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);

        // Initialize the ViewPager2 and TabLayout
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Set up the adapter that will provide fragments for each view
        adapter = new CalendarPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach the TabLayout to the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(tabTitles[position]);
                    }
                }).attach();
    }
}
