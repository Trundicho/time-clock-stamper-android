package de.trundicho.timeclockstamper.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.fragment.app.DialogFragment;
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


public class TodayFragment extends Fragment {
    private final Handler handler = new Handler();
    private TodayViewModel pageViewModel;
    private TimeClockStamperFragmentBinding binding;
    private ListView clockTimeTable;
    private ToggleButton toggleButton;
    private TextView workedToday;
    private Runnable updateUiLoop;

    public static TodayFragment newInstance() {
        return new TodayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(TodayViewModel.class);
        pageViewModel.setActivity(new ActivityCallback(getActivity()));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = TimeClockStamperFragmentBinding.inflate(inflater, container, false);
        workedToday = binding.workedToday;
        workedToday.setText(workedToday());
        clockTimeTable = binding.clockTimeList;
        clockTimeTable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        toggleButton = binding.toggleButton;

        toggleButton.setOnClickListener(view -> {
            pageViewModel.stamp();
            updateUiWidgets();
        });
        binding.addButton.setOnClickListener(view -> {
            DialogFragment dlg = new TimePickerFragment(pageViewModel, this::updateUiWidgets);
            dlg.show(getActivity().getSupportFragmentManager(), "TimePicker");
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
            updateUiWidgets();
        });
        updateUiWidgets();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        int delay = 10000;
        updateUiLoop = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, delay);
                updateWorkedToday();
            }
        };
        handler.postDelayed(updateUiLoop, delay);
        super.onResume();
    }

    @Override
    public void onPause() {
        //stop handler when activity not visible super.onPause();
        handler.removeCallbacks(updateUiLoop);
        super.onPause();
    }

    private void updateUiWidgets() {
        updateWorkedToday();
        clockTimeTable.setAdapter(createTimeStampListAdapter());
        selectLatestInTable();
        toggleButton.setChecked(pageViewModel.getClockTimes().size() % 2 == 1);
    }

    private void selectLatestInTable() {
        int count = clockTimeTable.getCount();
        if (count >= 1) {
            clockTimeTable.setSelection(count - 1);
        }
    }

    private void updateWorkedToday() {
        String text = workedToday();
        workedToday.setText(text);
    }

    @NonNull
    private SimpleAdapter createTimeStampListAdapter() {
        String[] from = {"Date"};
        int[] to = {android.R.id.text1};
        return new ColorArrayAdapter(this.getContext(),
                buildStampData(),
                android.R.layout.simple_list_item_multiple_choice, from, to);
    }

    public static class ColorArrayAdapter extends SimpleAdapter {

        public ColorArrayAdapter(Context context, List<Map<String, String>> data,
                                 int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = (View) super.getView(position, convertView, parent);
            if (position % 2 == 1) {
                view.setBackgroundColor(Color.parseColor("#f7ffe8"));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }
            return view;
        }

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

    private List<Map<String, String>> buildStampData() {
        List<String> times = pageViewModel.getClockTimes().stream().map(this::formatDate).collect(Collectors.toList());
        ArrayList<Map<String, String>> list = new ArrayList<>();
        times.forEach(t -> list.add(putData(t)));
        return list;
    }

    @NonNull
    private String formatDate(ClockTimeDto c) {
        return new DateTimeFormatterBuilder().appendPattern("HH:mm a").toFormatter().format(c.getDate());
    }

    private Map<String, String> putData(String name) {
        HashMap<String, String> item = new HashMap<>();
        item.put("Date", name);
        return item;
    }
}