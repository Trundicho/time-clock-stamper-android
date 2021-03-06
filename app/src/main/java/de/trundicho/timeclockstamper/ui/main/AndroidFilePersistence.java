package de.trundicho.timeclockstamper.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.trundicho.timeclockstamper.core.domain.model.ClockTime;
import de.trundicho.timeclockstamper.core.service.ClockTimePersistencePort;

public class AndroidFilePersistence implements ClockTimePersistencePort {

    private final ObjectMapper objectMapper;
    private final String persistenceFile;
    private final String persistenceFolder;
    private final String timezone;
    private ActivityCallback activityCallback;

    /**
     * https://technobyte.org/write-text-files-android-build-scratchpad-app-tutorial/
     * https://stackoverflow.com/questions/1239026/how-to-create-a-file-in-android
     */
    public AndroidFilePersistence(String persistenceFolder, String persistenceFile, String timeZone) {
        this.persistenceFile = persistenceFile;
        this.persistenceFolder = persistenceFolder;
        this.timezone = timeZone;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    public void write(List<ClockTime> clockTimes, Integer year, Integer month) {
        LocalDateTime localDate = localDate(year, month);
        List<ClockTime> clockTimesOfCurrentMonth = clockTimes.stream()
                .filter(c -> localDate.getMonth().equals(c.getDate().getMonth())
                        && localDate.getYear() == c.getDate().getYear())
                .sorted()
                .collect(Collectors.toList());
        try {
            String valueAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(clockTimesOfCurrentMonth);
            writeToFile(valueAsString, createFileName(year, month));
        } catch (IOException e) {
            System.err.println("Can not write to file " + e.getMessage());
        }
    }

    public String createFileName(Integer year, Integer month) {
        LocalDateTime currentDate = localDate(year, month);
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();
        return persistenceFolder + currentYear + "-" + prependZero(currentMonth) + "-" + persistenceFile;
    }

    @NonNull
    private String prependZero(int currentMonth) {
        return currentMonth < 10 ? "0" + currentMonth : "" + currentMonth;
    }

    private LocalDateTime localDate(Integer year, Integer month) {
        LocalDateTime now = getLocalDateTime();
        return LocalDateTime.of(year == null ? now.getYear() : year, month == null ? now.getMonth().getValue() : month, 1, 0, 0);
    }

    private LocalDateTime getLocalDateTime() {
        return LocalDateTime.now(ZoneId.of(timezone));
    }

    public void writeJson(String json) {
        FragmentActivity activity = activityCallback.getActivity();
        List<String> completeDb = getFilePersistence(activity);
        try {
            List<ClockTime> clockTimes = objectMapper.readValue(json, new TypeReference<List<ClockTime>>() {

            });
            Map<String, List<ClockTime>> map = new HashMap<>();
            clockTimes.forEach(c -> {
                String key = c.getDate().getYear() + "_" + c.getDate().getMonthValue();
                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(c);
            });
            map.keySet().forEach(k -> {
                String[] split = k.split("_");
                Integer year = Integer.valueOf(split[0]);
                Integer month = Integer.valueOf(split[1]);
                String fileName = createFileName(year, month);
                Optional<String> any = completeDb.stream().filter(c -> c.equals(fileName)).findAny();
                any.ifPresent(activity::deleteFile);
                write(map.get(k), year, month);
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private List<String> getFilePersistence(FragmentActivity activity) {
        String[] files = activity.fileList();
        List<String> strings = new ArrayList<>(Arrays.asList(files));
        return strings.stream().filter(pathname -> pathname.endsWith(persistenceFile)).collect(Collectors.toList());
    }

    public String readJson(Integer year, Integer month) {
        List<ClockTime> clockTimes = read(year, month);
        List<ClockTime> currentClockTimesOrAll = clockTimes;
        if (year != null && month != null) {
            LocalDateTime localDate = localDate(year, month);
            currentClockTimesOrAll = clockTimes.stream()
                    .filter(c -> localDate.getMonth().equals(c.getDate().getMonth())
                            && localDate.getYear() == c.getDate().getYear())
                    .sorted()
                    .collect(Collectors.toList());
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentClockTimesOrAll);
        } catch (JsonProcessingException e) {
            System.err.println("Can not read from file " + e.getMessage());
            return "Can not read from file " + e.getMessage();
        }
    }

    public List<ClockTime> read(Integer year, Integer month) {
        List<ClockTime> clockTimes = new ArrayList<>();
        try {
            FragmentActivity activity = activityCallback.getActivity();
            List<String> collect = getFilePersistence(activity);

            for (String file : collect) {
                FileInputStream fileInputStream = activity.openFileInput(file);
                clockTimes.addAll(objectMapper.readValue(fileInputStream, new TypeReference<List<ClockTime>>() {

                }));
            }

        } catch (NullPointerException | IOException e) {
            System.err.println("Can not read from file " + e.getMessage());
        }
        if (year != null && month != null) {
            return clockTimes.stream()
                    .filter(c -> c.getDate().getYear() == year)
                    .filter(c -> c.getDate().getMonthValue() == month).collect(Collectors.toList());
        }
        return clockTimes;
    }

    private void writeToFile(String message, String fileName) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(activityCallback.getActivity().openFileOutput(fileName,
                    Context.MODE_PRIVATE));
            outputStreamWriter.write(message);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setActivityCallBack(ActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
    }
}
