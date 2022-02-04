package de.trundicho.timeclockstamper.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDto;
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
        pageViewModel.setActivity(new ActivityCallback(getActivity()));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = TimeClockStamperFragmentBinding.inflate(inflater, container, false);
        TextView workedToday = binding.workedToday;
        workedToday.setText(workedToday());
        TableLayout clockTimeTable = binding.clockTimeTable;
        fillTable(clockTimeTable);
        ToggleButton toggleButton = binding.toggleButton;
        if(pageViewModel.getClockTimes().size() % 2 == 1){
            toggleButton.setChecked(true);
        }
        toggleButton.setOnClickListener(view -> {
            pageViewModel.stamp();
            workedToday.setText(workedToday());
            fillTable(clockTimeTable);
        });
        return binding.getRoot();
    }

    private void fillTable(TableLayout clockTimeTable) {
        clockTimeTable.removeAllViews();
        List<ClockTimeDto> clockTimes = pageViewModel.getClockTimes();
        clockTimes.forEach(c -> {
            TextView textView = new TextView(getContext());
            textView.setText(c.getDate().toString());
            clockTimeTable.addView(textView);
        });
    }

    @NonNull
    private String workedToday() {
        return "Worked today: " + pageViewModel.getWorkedToday();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}