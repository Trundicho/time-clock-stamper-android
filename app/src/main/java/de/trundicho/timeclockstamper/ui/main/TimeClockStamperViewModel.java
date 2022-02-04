package de.trundicho.timeclockstamper.ui.main;

import androidx.lifecycle.ViewModel;

import java.util.List;

import de.trundicho.timeclockstamper.core.adapters.api.ClockTimeDto;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApi;
import de.trundicho.timeclockstamper.core.adapters.api.TimeClockStamperApiImpl;

public class TimeClockStamperViewModel extends ViewModel {
    private final AndroidFilePersistence clockTimePersistencePort;
    private final TimeClockStamperApi timeClockStamperApi;

    public TimeClockStamperViewModel() {
        String timeZone = "Europe/Berlin";
        https://technobyte.org/write-text-files-android-build-scratchpad-app-tutorial/
        clockTimePersistencePort = new AndroidFilePersistence("",
                "test-clockTime-list.json",
                timeZone);
        timeClockStamperApi = new TimeClockStamperApiImpl(timeZone, clockTimePersistencePort);
    }

    public String getWorkedToday() {
        return timeClockStamperApi.getTimeClockResponse().getHoursWorkedToday();
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
}