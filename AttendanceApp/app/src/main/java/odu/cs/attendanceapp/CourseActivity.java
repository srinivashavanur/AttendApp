package odu.cs.attendanceapp;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CourseActivity extends FragmentActivity {

    EditText etName;
    EditText etAbbrev;
    EditText etDate;
    EditText etEndDate;
    Button btnDate;
    Button btnEndDate;
    EditText etStartTime;
    Button btnFromTime;
    EditText etEndTime;
    Button btnEndTime;
    Button btnSave;


    TextView tvLocation;
    EditText etLocation;

    CheckBox monday;
    CheckBox tuesday;
    CheckBox wednesday;
    CheckBox thursday;
    CheckBox friday;
    CheckBox saturday;


    String sLocation;

    Firebase mFirebaseRef;

    //Barcode.GeoPoint p;

    private LocationManager lm;
    private LocationListener locationListener;

    private class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {

                sLocation = "Location changed : Lat: " + loc.getLatitude() + " Lng: "
                        + loc.getLongitude();

                tvLocation.setText(sLocation);
            }
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(getString(R.string.firebase_url));

        etName = (EditText) findViewById(R.id.etName);
        etAbbrev = (EditText) findViewById(R.id.etAbbrev);
        etDate = (EditText) findViewById(R.id.etDate);
        etEndDate = (EditText) findViewById(R.id.etEndDate);
        btnDate = (Button) findViewById(R.id.btnDate);
        btnEndDate = (Button) findViewById(R.id.btnEndDate);
        etStartTime = (EditText) findViewById(R.id.etStartTime);
        btnFromTime = (Button) findViewById(R.id.btnFromTime);
        etEndTime = (EditText) findViewById(R.id.etEndTime);
        btnEndTime = (Button) findViewById(R.id.btnEndTime);
        btnSave = (Button) findViewById(R.id.btnSave);

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        etLocation = (EditText) findViewById(R.id.etLocation);

        monday = (CheckBox) findViewById(R.id.monday);
        tuesday = (CheckBox) findViewById(R.id.tuesday);
        wednesday = (CheckBox) findViewById(R.id.wednesday);
        thursday = (CheckBox) findViewById(R.id.thursday);
        friday = (CheckBox) findViewById(R.id.friday);
        saturday = (CheckBox) findViewById(R.id.saturday);

        //---use the LocationManager class to obtain locations data---
        lm = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();


    /*    lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                locationListener);*/

        //---get the SharedPreferences object---
        SharedPreferences prefs = getSharedPreferences(ApplicationConstants.prefName, MODE_PRIVATE);

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialog(DATE_DIALOG_ID);

                DatePickerFragment df = new DatePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.editText =  etDate;

                df.show(fm, "ToDatePicker");

            }
        });

        btnEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialog(DATE_DIALOG_ID);

                DatePickerFragment df = new DatePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.editText =  etEndDate;

                df.show(fm, "ToDatePicker");

            }
        });

        btnFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment df = new TimePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.editText = etStartTime;
                df.show(fm, "ToTimePicker");
            }
        });

        btnEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment df = new TimePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.editText = etEndTime;
                df.show(fm, "ToTimePicker");
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tvPasscode.setText(generate_code() + "");

                insertCalendar();

                saveCourse();
            }


        });

//        btnTeacher.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                UserFragment userFragment = new UserFragment();
//
//                FragmentManager fm = getFragmentManager();
//                /*
//                FragmentTransaction fragmentTransaction = fm.beginTransaction();
//                fragmentTransaction.replace(android.R.id.content, userFragment);
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
//                */
//
//                userFragment.show(fm, "fragment_edit_teacher");
//            }
//        });
    }

    private void saveCourse()
    {

        Course course = new Course(etName.getText().toString(),
                etAbbrev.getText().toString(),
                new User("teacher@odu.edu", "I'm a teacher"),
                tvLocation.getText().toString());

        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
        Date date = sdf.parse(etDate.getText().toString());
        cal1.setTime(date);
    } catch (ParseException e) {
        e.printStackTrace();
    }
        try {
            Date date = sdf.parse(etEndDate.getText().toString());
            cal2.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        while(cal1.getTimeInMillis() < cal2.getTimeInMillis()){
            boolean add = false;
            if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && monday.isChecked()){
                add = true;
            }else if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && tuesday.isChecked()){
                add = true;
            }else if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && wednesday.isChecked()){
                add = true;
            }else if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && thursday.isChecked()){
                add = true;
            }else if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && friday.isChecked()){
                add = true;
            }else if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && saturday.isChecked()){
                add = true;
            }


            if(add){
                String date = cal1.getDisplayName(Calendar.DATE, Calendar.LONG, Locale.ENGLISH);
                course.addSession(new Session(date,
                        etStartTime.getText().toString(),
                        etEndTime.getText().toString(), false, null));
               // Log.d("Added day:", date);
            }
            cal1.add(Calendar.DAY_OF_MONTH, 1);
        }
/*
        course.addStudent(new User("alice@wonderland.com", "Alice"));
        course.addStudent(new User("thedoctor@gallifrey.com", "Doctor Who"));
        course.addStudent(new User("Walter@white.com", "Walter White"));
*/



        course.addBeacon("This is a beacon id");
        //YZ: Creating a user Firebase reference
        Firebase courseRef = mFirebaseRef.child("classes").child(etName.getText().toString());
        courseRef.setValue(course);
    }

    private void insertCalendar()
    {
        long calID = 3;
        long startMillis = 0;
        long endMillis = 0;

        int year;
        int month;
        int day;

        int hour;
        int minute;

        Calendar c = Calendar.getInstance();;
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        String etDateText = etDate.getText().toString();
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

        String etTimeText = etStartTime.getText().toString();
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



        Calendar beginTime = Calendar.getInstance();
        beginTime.set(year, month, day, hour, minute);

        etTimeText = etEndTime.getText().toString();
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

        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, hour, minute);

        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, etName.getText().toString());
        values.put(CalendarContract.Events.DESCRIPTION, "Group workout");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
        values.put(CalendarContract.Events.EVENT_LOCATION, sLocation);
        values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;COUNT=10;WKST=SU");


       // Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);



        // get the event ID that is the last element in the Uri
        //long eventID = Long.parseLong(uri.getLastPathSegment());
        //
        // ... do something with event ID

        //
        //

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
