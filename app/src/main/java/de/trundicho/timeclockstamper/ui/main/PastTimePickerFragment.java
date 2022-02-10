package de.trundicho.timeclockstamper.ui.main;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.time.LocalTime;
import java.util.Calendar;

public class PastTimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private final PastViewModel viewModel;
    private final Runnable updateUiCallback;
    private final int year;
    private final int month;
    private final int day;

    public PastTimePickerFragment(PastViewModel viewModel,
                                  Runnable updateUiCallback,
                                  int year,
                                  int month,
                                  int day) {
        this.viewModel = viewModel;
        this.updateUiCallback = updateUiCallback;
        this.year = year;
        this.month = month;
        this.day = day;
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
        viewModel.stamp(LocalTime.of(hourOfDay, minute), year, month, day);
        updateUiCallback.run();
    }
}
