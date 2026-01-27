package com.example.test_v2.calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.test_v2.DailyFragment;
import com.example.test_v2.calendar.WeeklyFragment;


public class CalendarPagerAdapter extends FragmentStateAdapter {
    public CalendarPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DailyFragment();  // For Daily view
            case 1:
                return new WeeklyFragment(); // For Weekly view
            case 2:
                return new MonthlyFragment(); // For Monthly view
            default:
                return new DailyFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of views (Daily, Weekly, Monthly)
    }
}
