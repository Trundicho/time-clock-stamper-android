package de.trundicho.timeclockstamper.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import de.trundicho.timeclockstamper.databinding.TimeClockStamperFragmentBinding;

public class TimeClockStamperFragment extends Fragment {
    private TimeClockStamperViewModel pageViewModel;
    private TimeClockStamperFragmentBinding binding;

    public static TimeClockStamperFragment newInstance() {
        return new TimeClockStamperFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(TimeClockStamperViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = TimeClockStamperFragmentBinding.inflate(inflater, container, false);
        TextView textView = binding.sectionLabel2;
//        pageViewModel.getText().observe(getViewLifecycleOwner(), x -> textView.setText("blub" + x));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}