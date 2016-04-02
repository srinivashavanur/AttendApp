package odu.cs.attendanceapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingbozheng on 11/14/2015.
 */
public class CRNSchedule
{
    private String CRN;
    private List<Schedule> schedules;

    public CRNSchedule(String abbreviation,
                       String CRN,
                       String weekDay,
                       String fromTime,
                       String toTime)
    {
        this.CRN = CRN;
        schedules = new ArrayList<>();
        schedules.add(new Schedule(abbreviation, CRN,weekDay, fromTime, toTime, ""));
    }

    public String getCRN()
    {
        return CRN;
    }

    public void addSchedule(String abbreviation,
                            String weekDay,
                            String fromTime,
                            String toTime){
        schedules.add(new Schedule(abbreviation, CRN, weekDay, fromTime, toTime, ""));
    }

    public void addSchedule(Schedule s){
        schedules.add(s);
    }

    public List<Schedule> getSchedules(){return schedules;}
}
