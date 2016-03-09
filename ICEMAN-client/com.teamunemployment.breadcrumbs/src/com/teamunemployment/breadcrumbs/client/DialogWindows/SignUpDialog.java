package com.teamunemployment.breadcrumbs.client.DialogWindows;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.teamunemployment.breadcrumbs.R;

/**
 * Created by jek40 on 7/03/2016.
 */
public class SignUpDialog extends DialogFragment {
    private View dialogView;

    public interface DatePickerDialogListener {
        public void onDateClick(int day, int month, int year);
    }

    DatePickerDialogListener listener;

    public SignUpDialog() {
        //Apparently an empty constructor is needed? Sounds like bullshit.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialogView = inflater.inflate(R.layout.signup_dialog, container);
        getDialog().setTitle("Select Date");
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


}


