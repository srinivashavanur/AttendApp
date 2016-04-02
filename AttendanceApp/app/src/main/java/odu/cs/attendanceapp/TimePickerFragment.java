package odu.cs.attendanceapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;


/**
 * Created by yingbozheng on 10/1/2015.
 */
public class TimePickerFragment  extends DialogFragment implements TimePickerDialog.OnTimeSetListener{


    public EditText editText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        String etTimeText = editText.getText().toString();
        if (etTimeText.length() > 0)
        {
            try
            {
                String [] d = etTimeText.split(":");
                hour = Integer.parseInt(d[1]);
                minute = Integer.parseInt(d[0]);
            }
            catch( Exception ex)
            {

            }

        }

        // Create a new instance of TimePickerDialog and return it

        return new TimePickerDialog(getActivity(), this, hour, minute, true);

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        editText.setText(hourOfDay + ":" + minute);

    }
}