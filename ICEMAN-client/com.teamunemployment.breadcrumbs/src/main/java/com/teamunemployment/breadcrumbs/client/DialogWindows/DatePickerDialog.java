package com.teamunemployment.breadcrumbs.client.DialogWindows;

/**
 * Popup dialog for a user to easily pick a date.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.teamunemployment.breadcrumbs.R;

public class DatePickerDialog  extends DialogFragment {
    private View dialogView;

    public interface DatePickerDialogListener {
        public void onDateClick(int day, int month, int year);
    }

    DatePickerDialogListener listener;

    public DatePickerDialog() {
        //Apparently an empty constructor is needed? Sounds like bullshit.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

        dialogView = inflater.inflate(R.layout.date_picker, container);
        getDialog().setTitle("Select Date");
        setUpClickListener();
        return dialogView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (DatePickerDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    private DatePicker.OnDateChangedListener dateChangedListener = new DatePicker.OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            listener.onDateClick(dayOfMonth, monthOfYear+1, year);
            //dismiss();
        }
    };

    private void setUpClickListener() {
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
        datePicker.init(2015, 11, 8, dateChangedListener);
       // datePicker.onDateChaged()
    }


}


