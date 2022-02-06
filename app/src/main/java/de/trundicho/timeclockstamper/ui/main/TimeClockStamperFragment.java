package de.trundicho.timeclockstamper.ui.main;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        ListView clockTimeTable = binding.clockTimeList;
        clockTimeTable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        clockTimeTable.setAdapter(createListAdapter());
        ToggleButton toggleButton = binding.toggleButton;
        if (pageViewModel.getClockTimes().size() % 2 == 1) {
            toggleButton.setChecked(true);
        }
        toggleButton.setOnClickListener(view -> {
            pageViewModel.stamp();
            updateUiWidgets(clockTimeTable, toggleButton, workedToday);
        });
        binding.deleteButton.setOnClickListener(view -> {
            SparseBooleanArray checkedItemPositions = clockTimeTable.getCheckedItemPositions();

            List<ClockTimeDto> currentStampState = pageViewModel.getClockTimes();
            List<ClockTimeDto> clockTimeDtos = new ArrayList<>(currentStampState);
            for (int i = clockTimeDtos.size() - 1; i >= 0; i--) {
                if (checkedItemPositions.get(i)) {
                    clockTimeDtos.remove(i);
                }
            }
            pageViewModel.setClockTimesToday(clockTimeDtos);
            updateUiWidgets(clockTimeTable, toggleButton, workedToday);
        });
        return binding.getRoot();
    }

    private void updateUiWidgets(ListView clockTimeTable, ToggleButton toggleButton, TextView workedToday) {
        workedToday.setText(workedToday());
        clockTimeTable.setAdapter(createListAdapter());
        if (pageViewModel.getClockTimes().size() % 2 == 1) {
            toggleButton.setChecked(true);
        } else {
            toggleButton.setChecked(false);
        }
    }

    @NonNull
    private SimpleAdapter createListAdapter() {
        String[] from = {"Date"};
        int[] to = {android.R.id.text1};
        return new SimpleAdapter(this.getContext(),
                buildData(),
                android.R.layout.simple_list_item_multiple_choice, from, to);
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

    private List<Map<String, String>> buildData() {
        List<String> times = pageViewModel.getClockTimes().stream().map(c -> formatDate(c)).collect(Collectors.toList());
        ArrayList<Map<String, String>> list = new ArrayList<>();
        times.forEach(t -> list.add(putData(t)));
        return list;
    }

    @NonNull
    private String formatDate(ClockTimeDto c) {
        return new DateTimeFormatterBuilder().appendPattern("dd.MM.yy - HH:mm:ss").toFormatter().format(c.getDate());
    }

    private Map<String, String> putData(String name) {
        HashMap<String, String> item = new HashMap<>();
        item.put("Date", name);
        return item;
    }
}