package odu.cs.attendanceapp;

import android.app.Activity;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yingbozheng on 11/22/2015.
 */
public class FirebaseUtility
{

    public static Activity activity;


    //Add class sessions as appointments in Android Calendar App
    //Add Class sessions under students/email/Calendar
    //Parameters:
    // fromDate  -- Semester begin date
    // toDate
    //
    public static void pushCalendar(Date fromDate, Date toDate,String email, String CRN, String weekDay, String fromTime, String toTime, String abbreviation,String beaconID, String place, boolean populateCalendarApp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        int dayOfWeek;
        String dayWeek;
        String startdateString;
        String enddateString;
        long startDate;
        long endDate;
        Date startDt;
        Date endDt;
        DateFormat dateFormat;
        Date currentDate;
        String currentDateString;
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int fTime;
        int tTime;

        while (calendar.getTime().before(toDate))
        {
            startDate = calendar.getTimeInMillis();
            endDate = calendar.getTimeInMillis();

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);


            dateFormat = new SimpleDateFormat("EE");
            currentDate = calendar.getTime();

            currentDateString = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
            dayWeek = dateFormat.format(currentDate).toUpperCase();

            //dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if(dayWeek.equals(weekDay.toUpperCase().substring(0,3)))
            {
                fTime = Integer.parseInt(fromTime);
                hour = (int)(fTime/100.0);
                minute = (fTime % 100);
                second = 0;
                calendar.set(year, month,day, hour, minute,second);
                startDate = calendar.getTimeInMillis();

                tTime = Integer.parseInt(toTime);
                hour = (int)(tTime/100.0);
                minute = (tTime % 100);
                second = 0;
                calendar.set(year, month, day, hour, minute, second);
                endDate = calendar.getTimeInMillis();

                if(populateCalendarApp)
                {
                    CalendarUtility.pushAppointmentsToCalender(CalendarUtility.activity, abbreviation, beaconID, "", 1, startDate, endDate, true, false);
                }

                AddStudentCalendar(email, abbreviation, CRN, weekDay, fromTime, toTime, currentDateString, beaconID);

            }

            calendar.add(Calendar.DATE, 1);
        }
    }

    //Add Class sessions under students/email/Calendar
    //Example URL: https://attendancecs441.firebaseio.com/students/YZHEN004%40ODU-EDU/Calendar/2015-09-26/930
    public static void AddStudentCalendar(String StudentEmail, String CourseAbbrev, String CRN, String weekday, String fromTime, String toTime, String currentDate, String beaconID )
    {
        Schedule s = new Schedule(CourseAbbrev, CRN, weekday, fromTime, toTime, beaconID);

        Firebase.setAndroidContext(FirebaseUtility.activity);
        Firebase studentRef = new Firebase(ApplicationConstants.FIREBASE_URL).child("students").child(StudentEmail.replace('.', '-').toLowerCase()).child("Calendar").child(currentDate).child(fromTime);
        studentRef.setValue(s);

        Firebase attendenceRef = new Firebase(ApplicationConstants.FIREBASE_URL).child("attendance").child(CRN).child(StudentEmail.replace('.', '-').toLowerCase()).child(currentDate);
        final Map<String, Object> attendanceMap = new HashMap<>();
        attendanceMap.put("isAttended", false);
        attendanceMap.put("isExcused", false);
        attendanceMap.put("isMissed", true);
        attendenceRef.setValue(attendanceMap);
    }

    //Add classes to Firebase node students/Email/classes
    //Ex: https://attendancecs441.firebaseio.com/students/YZHEN004%40ODU-EDU/classes
    public static void AddStudentClass(String StudentEmail, String CourseAbbrev, String CRN )
    {
        Map<String, String> classMap = new HashMap<String, String>();
        classMap.put("Abbreviation", CourseAbbrev);
        Firebase studentRef = new Firebase(ApplicationConstants.FIREBASE_URL).child("students").child(StudentEmail.replace('.', '-').toLowerCase()).child("classes");
        studentRef.child(CRN).setValue(classMap);

    }

    public static void AddAttendance(Firebase firebase, String beaconID, String CourseAbbrev, String StudentEmail, String crn)
    {

        DateFormat fullDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String fullDate = fullDateFormat.format(date);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = dateFormat.format(date);

/*        Attendance(String date, String beaconID, String email, String courseAbbrev, String CRN,
        boolean attended, boolean excused, String reason)*/
        Attendance attendance = new Attendance(sDate, beaconID, StudentEmail, CourseAbbrev, crn, true, false, "");


        //final Firebase attendanceRef = firebase.child("attendance").child("all").push();
        //attendanceRef.setValue(attendance);

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        final String sTime = timeFormat.format(date);

        //YZ: Creating a course Firebase reference
        //Firebase checkInRef; //= firebase.child("attendance").child(email).child(abbrev).child("sessions");
        //checkInRef.setValue(this);

        final Map<String, Object> arrivalMap = new HashMap<>();
        arrivalMap.put("Arrival Time", sTime);
        arrivalMap.put("isAttended", true);


        final Firebase checkInRef = firebase.child("attendance").child(crn).child(StudentEmail.replace('.', '-').toLowerCase()).child(sDate);
            checkInRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    checkInRef.setValue(arrivalMap);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public static int getIndexByBeaconId(List<Schedule> schedules, String beaconID)
    {
        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        Date date = new Date();
        String sWeekday = dateFormat.format(date);
        String time = new SimpleDateFormat("km").format(date);
        int iTime = Integer.parseInt(time);
        int fromTime;
        int toTime;

        Schedule s;

        for (int i = 0; i < schedules.size(); i++)
        {
            s = schedules.get(i);
            Log.d("schedules",s.toString());

            fromTime = Integer.parseInt(s.getFromTime());
            toTime = Integer.parseInt(s.getToTime());

            if (s.getWeekDay().equals( sWeekday) && iTime < toTime && iTime > fromTime)
            {
                return i;
            }
        }

        return -1;
    }
//
//    public static String getCRNBasedOnTime(final String userEmail, String beaconID){
//        // Use Firebase to populate the list.
//        Firebase ref = new Firebase("https://attendancecs441.firebaseio.com/classes");
//        final ArrayList<String> crns = new ArrayList<>();
//        ref.addChildEventListener(new ChildEventListener() {
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
//                String studentDetails = String.valueOf(newPost.get("students"));
//                if (studentDetails.contains(userEmail)) {
//                    String crn = String.valueOf(newPost.get("crn"));
//
//                    crns.add(crn);
//                }
//
//            }
//
//
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            }
//
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//            }
//
//            public void onCancelled(FirebaseError firebaseError) {
//            }
//        });
//    }
//
//
//    public boolean beaconMatches(String beaconID, String crn){
//        Firebase ref = new Firebase("https://attendancecs441.firebaseio.com/CRNschedule");
//
//
//    }
}
