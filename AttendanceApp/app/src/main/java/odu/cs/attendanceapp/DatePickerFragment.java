package odu.cs.attendanceapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public EditText editText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int year,month, day;

        Calendar c = Calendar.getInstance();;
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        String etDateText = editText.getText().toString();
        if (etDateText.length() > 0)
        {
            try
            {
                String [] d = etDateText.split("/");
                year = Integer.parseInt(d[2]);
                month = Integer.parseInt(d[0])-1;
                day = Integer.parseInt(d[1]);

            }
            catch( Exception ex)
            {

            }

        }

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);

    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user

        editText.setText((month+1) + "/" + day + "/" + year);

    }
}
