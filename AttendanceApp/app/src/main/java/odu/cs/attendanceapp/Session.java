package odu.cs.attendanceapp;

import java.util.ArrayList;

/**
 * Created by yingbozheng on 10/26/2015.
 */
public class Session
{
    public String sessionDate;
    public String fromTime;
    public String toTime;
    public boolean isCancelled;
    public ArrayList<Attendance> attendance;

    public Session()
    {}

    public Session(String _sessionDate, String _fromTime, String _toTime, boolean cancelled, ArrayList<Attendance> attendance)
    {
        this.sessionDate = _sessionDate;
        this.fromTime = _fromTime;
        this.toTime = _toTime;
        this.isCancelled = cancelled;
        this.attendance = attendance;
    }

    public String getSessionDate()
    {
        return sessionDate;
    }

    public String getFromTime()
    {
        return fromTime;
    }

    public String getToTime()
    {
        return toTime;
    }

    //public boolean g
}
