package de.trundicho.timeclockstamper.ui.main;

import static android.app.Activity.RESULT_OK;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.trundicho.timeclockstamper.core.domain.model.ClockTime;
import de.trundicho.timeclockstamper.databinding.InsightsFragmentBinding;


public class InsightsFragment extends Fragment {
    private InsightsFragmentBinding binding;
    private AndroidFilePersistence androidFilePersistence;
    private PastViewModel pastViewModel;
    private ListView clockTimeDayList;

    public static InsightsFragment newInstance() {
        return new InsightsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String timeZone = "Europe/Berlin";
        androidFilePersistence = new AndroidFilePersistence("",
                "test-clockTime-list.json",
                timeZone);
        androidFilePersistence.setActivityCallBack(new ActivityCallback(getActivity()));
        pastViewModel = new PastViewModel(androidFilePersistence, timeZone);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = InsightsFragmentBinding.inflate(inflater, container, false);
        clockTimeDayList = binding.clockTimeDayList;
        clockTimeDayList.setAdapter(createTimeStampListAdapter());
        Button exportButton = binding.exportButton;
        exportButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "TimeClockModel.json"); //not needed, but maybe usefull
            // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
            startActivityForResult(intent, 1);

        });
        Button importButton = binding.importButton;
        importButton.setOnClickListener(view -> {
            Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
            fileintent.setType("*/*");
            try {
                startActivityForResult(fileintent, 2);
            } catch (ActivityNotFoundException e) {
                System.err.println("ActivityNotFoundException " + e);
            }
        });
        return binding.getRoot();
    }

    @NonNull
    private SimpleAdapter createTimeStampListAdapter() {
        String[] from = {"Date"};
        int[] to = {android.R.id.text1};
        return new ColorArrayAdapter(this.getContext(),
                buildStampData(null, null),
                android.R.layout.simple_list_item_1, from, to);
    }

    private List<Map<String, String>> buildStampData(Integer year, Integer month) {
        List<ClockTime> clockTimes = androidFilePersistence.read(year, month);
        Set<String> dates = new HashSet<>();
        clockTimes.forEach(c -> dates.add(c.getDate().getYear() + "_" + c.getDate().getMonthValue() + "_" + c.getDate().getDayOfMonth()));
        List<String> times = dates.stream().map(date -> {
            String[] split = date.split("_");
            int y = Integer.parseInt(split[0]);
            int m = Integer.parseInt(split[1]);
            int d = Integer.parseInt(split[2]);
            return (y-2000) + "." + prependZero(m) + "." + prependZero(d)
                    + "::" + pastViewModel.getWorkedToday(y, m, d) + "::M:" + pastViewModel.getOvertime(y, m, d);
        }).collect(Collectors.toList());
        List<Map<String, String>> list = new ArrayList<>();
        Stream<String> sorted = times.stream().sorted(Comparator.reverseOrder());
        sorted.forEach(t -> list.add(putData(t)));
        return list;
    }

    @NonNull
    private String prependZero(int currentMonth) {
        return currentMonth < 10 ? "0" + currentMonth : "" + currentMonth;
    }

    private Map<String, String> putData(String name) {
        HashMap<String, String> item = new HashMap<>();
        item.put("Date", name);
        return item;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                ParcelFileDescriptor fileDescriptor;
                try {
                    fileDescriptor = getActivity().getApplicationContext()
                            .getContentResolver()
                            .openFileDescriptor(uri, "w");

                    if (fileDescriptor != null) {
                        FileOutputStream outputStream = new FileOutputStream(
                                fileDescriptor.getFileDescriptor()
                        );
                        String value = androidFilePersistence.readJson(null, null);
                        outputStream.write(value.getBytes()
                        );
                        outputStream.close();
                        fileDescriptor.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == 2) {
                Uri uri = data.getData();
                try {
                    ParcelFileDescriptor fileDescriptor = getActivity().getApplicationContext()
                            .getContentResolver()
                            .openFileDescriptor(uri, "r");

                    if (fileDescriptor != null) {
                        InputStream inputStream = new FileInputStream(
                                fileDescriptor.getFileDescriptor()
                        );

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                            String contents = reader.lines().collect(Collectors.joining("\n"));
                            androidFilePersistence.writeJson(contents);
                        }
                        fileDescriptor.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class ColorArrayAdapter extends SimpleAdapter {

        public ColorArrayAdapter(Context context, List<Map<String, String>> data,
                                 int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            String color = "#f7ffe8";
            if (position % 2 == 1) {
                view.setBackgroundColor(Color.parseColor(color));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }
            return view;
        }

    }
}