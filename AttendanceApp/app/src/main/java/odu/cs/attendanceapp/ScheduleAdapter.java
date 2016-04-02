package odu.cs.attendanceapp;


import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Query;

/**
 * Created by yingbozheng on 11/22/2015.
 */
public class ScheduleAdapter extends FirebaseListAdapter<Schedule>
{
    public ScheduleAdapter(Query ref, Activity activity, int layout)
    {
        super(ref, Schedule.class, layout, activity);
    }

    @Override
    protected void populateView(View view, Schedule schedule)
    {
        String abbreviation = schedule.getAbbreviation();
        TextView tvAbbreviation = (TextView) view.findViewById(R.id.tvAbbreviation);
        tvAbbreviation.setText(abbreviation);

        String weekDay = schedule.getWeekDay().substring(0,3).toUpperCase();
        TextView tvWeekDay = (TextView) view.findViewById(R.id.tvWeekDay);
        tvWeekDay.setText(weekDay);

        String fromTime = schedule.getFromTime();
        TextView tvFromTime = (TextView) view.findViewById(R.id.tvFromTime);
        tvFromTime.setText(fromTime);

        String toTime = schedule.getToTime();
        TextView tvToTime = (TextView) view.findViewById(R.id.tvToTime);
        tvToTime.setText(toTime);

        String CRN = schedule.getCrn();
        TextView tvCRN = (TextView) view.findViewById(R.id.tvCRN);
        tvCRN.setText(CRN);
    }
}