package odu.cs.attendanceapp;

/**
 * Created by yingbozheng on 10/28/2015.
 */
public class ApplicationConstants
{
    public static final String APPKEY = "klhdfhsa5435536254GSH43T43f";

    public static final String prefName = "AttendancePref";

    public static final String NAME_KEY = "name";
    public static final String ID_KEY = "id";
    public static final String PHONE_KEY = "phonenum";

    public static final String FIREBASE_URL = "https://attendancecs441.firebaseio.com/";

    public enum Weekday
    {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
        THURSDAY, FRIDAY, SATURDAY
    }

    public enum Frequency
    {
        SINGLE, WEEKLY, DAILY, MONTHLY, YEARLY
    }
}
