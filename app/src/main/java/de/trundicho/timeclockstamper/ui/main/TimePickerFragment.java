package de.trundicho.timeclockstamper.ui.main;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.time.LocalTime;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private final TimeClockStamperViewModel viewModel;
    private final Runnable updateUiCallback;

    public TimePickerFragment(TimeClockStamperViewModel viewModel, Runnable updateUiCallback) {
        this.viewModel = viewModel;
        this.updateUiCallback = updateUiCallback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(
                getActivity(),
                this,
                hourOfDay,
                minute,
                false
        );
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        viewModel.stamp(LocalTime.of(hourOfDay, minute));
        updateUiCallback.run();
    }
}
