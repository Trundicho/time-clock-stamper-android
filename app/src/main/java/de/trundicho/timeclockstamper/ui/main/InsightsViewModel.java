package de.trundicho.timeclockstamper.ui.main;

import static de.trundicho.timeclockstamper.ui.main.AppConfig.MINUTES_TO_WORK_PER_DAY;

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDataDto;
import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDto;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApi;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApiImpl;

public class InsightsViewModel extends ViewModel {
    private final AndroidFilePersistence clockTimePersistencePort;
    private final TimeClockStamperApi timeClockStamperApi;

    public InsightsViewModel() {
        this(new AndroidFilePersistence("",
                        "test-clockTime-list.json",
                        "Europe/Berlin"), "Europe/Berlin");
    }

    public InsightsViewModel(AndroidFilePersistence clockTimePersistencePort, String timeZone) {
        this.clockTimePersistencePort = clockTimePersistencePort;
        timeClockStamperApi = new TimeClockStamperApiImpl(timeZone, clockTimePersistencePort, MINUTES_TO_WORK_PER_DAY);
    }

    public ClockTimeDataDto getDay(int year, int month, int day) {
        return timeClockStamperApi.getDay(year, month, day);
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

    public AndroidFilePersistence getFilePersistence() {
        return clockTimePersistencePort;
    }
}