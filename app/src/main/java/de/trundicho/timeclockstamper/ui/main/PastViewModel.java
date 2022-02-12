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
        this(new AndroidFilePersistence("",
                        "test-clockTime-list.json",
                        "Europe/Berlin"), "Europe/Berlin");
    }

    public PastViewModel(AndroidFilePersistence clockTimePersistencePort, String timeZone) {
        this.clockTimePersistencePort = clockTimePersistencePort;
        timeClockStamperApi = new TimeClockStamperApiImpl(timeZone, clockTimePersistencePort);
    }

    public String getWorkedToday(int year, int month, int day) {
        ClockTimeDataDto clockStamperApiDay = timeClockStamperApi.getDay(year, month, day);
        return clockStamperApiDay.getHoursWorkedToday();
    }

    public String getOvertime(int year, int month, int day) {
        ClockTimeDataDto clockStamperApiDay = timeClockStamperApi.getDay(year, month, day);
        return clockStamperApiDay.getOvertimeMonth();
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