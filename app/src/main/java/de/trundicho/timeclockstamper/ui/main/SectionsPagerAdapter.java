package de.trundicho.timeclockstamper.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import de.trundicho.timeclockstamper.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;

    public SectionsPagerAdapter(FragmentActivity activity) {
        super(activity);
        mContext = activity;
    }

//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return mContext.getResources().getString(TAB_TITLES[position]);
//    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return TodayFragment.newInstance();
            case 1:
                return PastFragment.newInstance();
            case 2:
                return InsightsFragment.newInstance();
            default:
                return InsightsFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}