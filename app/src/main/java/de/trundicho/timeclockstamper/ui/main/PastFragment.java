package de.trundicho.timeclockstamper.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
import de.trundicho.timeclockstamper.databinding.PastFragmentBinding;


public class PastFragment extends Fragment {
    private final Handler handler = new Handler();
    private PastViewModel pageViewModel;
    private PastFragmentBinding binding;
    private ListView clockTimeTable;
    private TextView workedToday;
    private Runnable updateUiLoop;
    private DatePicker datePicker;

    public static PastFragment newInstance() {
        return new PastFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PastViewModel.class);
        pageViewModel.setActivity(new ActivityCallback(getActivity()));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = PastFragmentBinding.inflate(inflater, container, false);
        datePicker = binding.datePicker;
        datePicker.setOnDateChangedListener((datePicker, year, month, day) ->
                updateUiWidgets(year, month + 1, day));
        workedToday = binding.workedToday;
        workedToday.setText(workedToday(datePicker.getYear(),
                getMonth(),
                datePicker.getDayOfMonth()));
        clockTimeTable = binding.clockTimeList;
        clockTimeTable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        binding.addButton.setOnClickListener(view -> {
            DialogFragment dlg = new PastTimePickerFragment(pageViewModel, () ->
                    updateUiWidgets(datePicker.getYear(),
                            getMonth(),
                            datePicker.getDayOfMonth()),
                    datePicker.getYear(), getMonth(), datePicker.getDayOfMonth());
            dlg.show(getActivity().getSupportFragmentManager(), "TimePicker");
        });
        binding.deleteButton.setOnClickListener(view -> {
            SparseBooleanArray checkedItemPositions = clockTimeTable.getCheckedItemPositions();

            List<ClockTimeDto> currentStampState = pageViewModel.getClockTimes(
                    datePicker.getYear(), getMonth(), datePicker.getDayOfMonth()
            );
            List<ClockTimeDto> clockTimeDtos = new ArrayList<>(currentStampState);
            for (int i = clockTimeDtos.size() - 1; i >= 0; i--) {
                if (checkedItemPositions.get(i)) {
                    clockTimeDtos.remove(i);
                }
            }
            pageViewModel.setClockTimesToday(clockTimeDtos, datePicker.getYear(),
                    getMonth(), datePicker.getDayOfMonth());
            updateUiWidgets(datePicker.getYear(), getMonth(), datePicker.getDayOfMonth());
        });
        updateUiWidgets(datePicker.getYear(), getMonth(), datePicker.getDayOfMonth());
        return binding.getRoot();
    }

    private int getMonth() {
        return datePicker.getMonth() + 1;
    }

    @Override
    public void onResume() {
        int delay = 10000;
        updateUiLoop = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, delay);
                updateWorkedToday(datePicker.getYear(),
                        getMonth(),
                        datePicker.getDayOfMonth());
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

    private void updateUiWidgets(int year, int month, int day) {
        updateWorkedToday(year, month, day);
        clockTimeTable.setAdapter(createTimeStampListAdapter(year, month, day));
        selectLatestInTable();
    }

    private void selectLatestInTable() {
        int count = clockTimeTable.getCount();
        if (count >= 1) {
            clockTimeTable.setSelection(count - 1);
        }
    }

    private void updateWorkedToday(int year, int month, int day) {
        String text = workedToday(year, month, day);
        workedToday.setText(text);
    }

    @NonNull
    private SimpleAdapter createTimeStampListAdapter(int year, int month, int day) {
        String[] from = {"Date"};
        int[] to = {android.R.id.text1};
        return new ColorArrayAdapter(this.getContext(),
                buildStampData(year, month, day),
                android.R.layout.simple_list_item_multiple_choice, from, to,
                clockTimeTable);
    }

    public static class ColorArrayAdapter extends SimpleAdapter {

        private final ListView clockTimeTable;

        public ColorArrayAdapter(Context context, List<Map<String, String>> data,
                                 int resource, String[] from, int[] to,
                                 ListView clockTimeTable) {
            super(context, data, resource, from, to);
            this.clockTimeTable = clockTimeTable;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            String color = "#f7ffe8";
            if (clockTimeTable.getCount() % 2 == 1) {
                color = "#fff2da";
            }
            if (position % 2 == 1) {
                view.setBackgroundColor(Color.parseColor(color));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }
            return view;
        }

    }

    @NonNull
    private String workedToday(int year, int month, int day) {
        String workedToday = pageViewModel.getWorkedToday(year, month, day);
        String overtime = pageViewModel.getOvertime(year, month, day);
        return "Worked: " + workedToday + " Month: " + overtime;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<Map<String, String>> buildStampData(int year, int month, int day) {
        List<String> times = pageViewModel.getClockTimes(year, month, day).stream()
                .map(this::formatDate).collect(Collectors.toList());
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