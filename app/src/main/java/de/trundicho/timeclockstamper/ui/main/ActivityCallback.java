package de.trundicho.timeclockstamper.ui.main;

import androidx.fragment.app.FragmentActivity;

class ActivityCallback {
    private final FragmentActivity activity;

    public ActivityCallback(FragmentActivity activity) {
        this.activity = activity;
    }

    public FragmentActivity getActivity() {
        return activity;
    }
}
