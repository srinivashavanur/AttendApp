package odu.cs.attendanceapp;

/**
 * Created by yingbozheng on 11/14/2015.
 */
public class Schedule
{
    private String abbreviation;
    private String weekDay;
    private String fromTime;
    private String toTime;
    private String crn;
    private String beaconID;

    public Schedule()
    {}

    public Schedule(String abbreviation,
                    String crn,
                    String weekDay,
                    String fromTime,
                    String toTime,
                    String beaconID
                    )
    {
        this.abbreviation = abbreviation;
        this.crn = crn;
        this.weekDay = weekDay;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.beaconID = beaconID;
    }

    public String getAbbreviation()
    {
        return abbreviation;
    }

    public String getWeekDay()
    {
        return weekDay;
    }

    public String getFromTime()
    {
        return fromTime;
    }

    public String getToTime()
    {
        return toTime;
    }

    public String getCrn()
    {
        return crn;
    }

    public String getBeaconID() {return beaconID;}
}
