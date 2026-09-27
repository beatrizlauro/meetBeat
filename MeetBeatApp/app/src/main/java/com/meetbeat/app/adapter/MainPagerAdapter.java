package com.meetbeat.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.meetbeat.app.fragment.CarpoolFragment;
import com.meetbeat.app.fragment.HomeFragment;
import com.meetbeat.app.fragment.MatchingFragment;
import com.meetbeat.app.fragment.ProfileFragment;
import com.meetbeat.app.fragment.TicketFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public static final int QUANTIDADE_ABAS = 5;

    public MainPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new TicketFragment();
            case 2: return new MatchingFragment();
            case 3: return new CarpoolFragment();
            case 4: return new ProfileFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return QUANTIDADE_ABAS;
    }
}
