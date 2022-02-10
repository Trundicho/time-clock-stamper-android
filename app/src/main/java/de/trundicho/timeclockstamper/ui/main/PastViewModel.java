package de.trundicho.timeclockstamper.ui.main;

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDataDto;
import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDto;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApi;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApiImpl;

public class PastViewModel extends ViewModel {
    private final AndroidFilePersistence clockTimePersistencePort;
    private final TimeClockStamperApi timeClockStamperApi;

    public PastViewModel() {
        String timeZone = "Europe/Berlin";
//        https://technobyte.org/write-text-files-android-build-scratchpad-app-tutorial/
        clockTimePersistencePort = new AndroidFilePersistence("",
                "test-clockTime-list.json",
                timeZone);
        timeClockStamperApi = new TimeClockStamperApiImpl(timeZone, clockTimePersistencePort);
    }

    public String getWorkedTodayAndOvertime(int year, int month, int day) {
        ClockTimeDataDto clockStamperApiDay = timeClockStamperApi.getDay(year, month, day);
        return clockStamperApiDay.getHoursWorkedToday()
                + ". Month: " + clockStamperApiDay.getOvertimeMonth();
    }

    public List<ClockTimeDto> getClockTimes(int year, int month, int day) {
        return timeClockStamperApi.getDay(year, month, day).getClockTimes();
    }

    public void setActivity(ActivityCallback activityCallback) {
        clockTimePersistencePort.setActivityCallBack(activityCallback);
    }

    public void setClockTimesToday(List<ClockTimeDto> clockTimeDtos, int year, int month, int day) {
        ClockTimeDataDto apiDay = timeClockStamperApi.getDay(year, month, day);
        apiDay.getClockTimes().clear();
        apiDay.getClockTimes().addAll(clockTimeDtos);
        timeClockStamperApi.setDay(apiDay, year, month, day);
    }

    public ClockTimeDataDto stamp(LocalTime localTime, int year, int month, int day) {
        ClockTimeDataDto apiDay = timeClockStamperApi.getDay(year, month, day);
        ClockTimeDto timeDto = new ClockTimeDto();
        timeDto.setDate(localTime.atDate(LocalDate.of(year, month, day)));
        apiDay.getClockTimes().add(timeDto);
        return timeClockStamperApi.setDay(apiDay, year, month, day);
    }

    public String getOvertimeMonth(int year, int month) {
        return null;
    }
}