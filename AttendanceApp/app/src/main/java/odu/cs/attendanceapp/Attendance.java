package odu.cs.attendanceapp;

/**
 * Created by yingbozheng on 10/26/2015.
 */
public class Attendance
{
    //public Student student;
    public boolean isAttended;
    public boolean isExcused;
    public String reason;
    public String email;
    public String beaconID;
    public String date;
    public String weekday;
    public String courseAbbrev;
    public String CRN;

    public Attendance()
    {}

    public Attendance(String date, String beaconID, String email, String courseAbbrev, String CRN,
                      boolean attended, boolean excused, String reason)
    {
        this.email = email;
        this.date = date;
        this.weekday = weekday;
        this.beaconID = beaconID;
        //this.student = stu;
        this.isAttended = attended;
        this.isExcused = excused;
        this.reason = reason;
        this.courseAbbrev = courseAbbrev;
        this.CRN = CRN;
    }

}
