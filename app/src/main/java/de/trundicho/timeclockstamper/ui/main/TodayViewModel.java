package de.trundicho.timeclockstamper.ui.main;

import androidx.lifecycle.ViewModel;

import java.time.LocalTime;
import java.util.List;

import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDataDto;
import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDto;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApi;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApiImpl;

public class TodayViewModel extends ViewModel {
    private final AndroidFilePersistence clockTimePersistencePort;
    private final TimeClockStamperApi timeClockStamperApi;

    public TodayViewModel() {
        String timeZone = "Europe/Berlin";
//        https://technobyte.org/write-text-files-android-build-scratchpad-app-tutorial/
        clockTimePersistencePort = new AndroidFilePersistence("",
                "test-clockTime-list.json",
                timeZone);
        timeClockStamperApi = new TimeClockStamperApiImpl(timeZone, clockTimePersistencePort);
    }

    public String getWorkedToday() {
        return timeClockStamperApi.getTimeClockResponse().getHoursWorkedToday();
    }

    public String getOvertimeMonth() {
        return timeClockStamperApi.getTimeClockResponse().getOvertimeMonth();
    }

    public List<ClockTimeDto> getClockTimes() {
        return timeClockStamperApi.getTimeClockResponse().getClockTimes();
    }

    public void stamp() {
        timeClockStamperApi.stampInOrOut();
    }

    public void setActivity(ActivityCallback activityCallback) {
        clockTimePersistencePort.setActivityCallBack(activityCallback);
    }

    public void setClockTimesToday(List<ClockTimeDto> clockTimeDtos) {
        ClockTimeDataDto clockTimeDto = new ClockTimeDataDto();
        clockTimeDto.setClockTimes(clockTimeDtos);
        timeClockStamperApi.setToday(clockTimeDto);
    }

    public ClockTimeDataDto stamp(LocalTime localTime) {
        return timeClockStamperApi.stamp(localTime);
    }

}