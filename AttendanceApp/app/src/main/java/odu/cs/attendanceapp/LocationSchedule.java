package odu.cs.attendanceapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liminzheng on 11/14/2015.
 */
public class LocationSchedule
{
    private String beaconID;
    private List<Schedule> schedules;

    private String CRN;

    public LocationSchedule(String abbreviation,
                    String beaconID,
                    String CRN,
                    String weekDay,
                    String fromTime,
                    String toTime)
    {
        this.beaconID = beaconID;
        this.CRN = CRN;
        schedules = new ArrayList<>();
        schedules.add(new Schedule(abbreviation, CRN, weekDay, fromTime, toTime, beaconID));
    }

    public String getBeaconID()
    {
        return beaconID;
    }

    public String getCRN() {return CRN;}

    public void addSchedule(String abbreviation,
                            String CRN,
                            String weekDay,
                            String fromTime,
                            String toTime,
                            String beaconID)
    {
        schedules.add(new Schedule(abbreviation, CRN, weekDay, fromTime, toTime,beaconID));
    }

    public void addSchedule(Schedule s){
        schedules.add(s);
    }

    public List<Schedule> getSchedules(){return schedules;}
}
