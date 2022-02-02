package de.trundicho.timeclockstamper.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import de.trundicho.timeclockstamper.core.adapters.persistence.FilePersistence;
import de.trundicho.timeclockstamper.core.domain.ports.ClockTimePersistencePort;

public class TimeClockStamperViewModel extends ViewModel {
    private final ClockTimePersistencePort clockTimePersistencePort;

    public TimeClockStamperViewModel() {
        clockTimePersistencePort = new FilePersistence("", "", "");
    }

    public LiveData<Object> getText() {
        return null;
    }
}