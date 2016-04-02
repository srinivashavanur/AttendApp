package odu.cs.attendanceapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

//Populate the calendar app or Firebase calendar node and automatically check in
public class CalendarActivity extends AppCompatActivity implements BeaconConsumer {

    private Button btnCalendar;

    private Button btnLaunchCalendar;

    private String email;

    private Activity calActivity;

    //Check In
    Button btnCheckIn;

    Firebase ref;

    private SharedPreferences prefs;
    private String userPreferences = "UserPreferences";
    private static final String USER_LOGIN = "userLogInPref";
    private static final String USER_EMAIL = "userEmailPref";

    //List of beacons that are found
    private ArrayList<Beacon> beaconsList;

    //ArrayAdapter to display information in a listview
    private ArrayAdapter<String> mBeaconsAdapter;

    //Beacon manager needed to connect to beacons
    private BeaconManager beaconManager;


    //List of beacon names
    ArrayList<String> beaconNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Firebase.setAndroidContext(this);

        ref = new Firebase(ApplicationConstants.FIREBASE_URL);

        //Initialize beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        //Initialize beacon list
        beaconsList = new ArrayList<>();

        //Request user to turn on bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int REQUEST_ENABLE_BT = 1;
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        //List of beacons
        final ArrayList<Beacon> detectedBeacons = new ArrayList<>();
        for (Beacon beacon : beaconsList) {
            beaconNames.add(beacon.getBluetoothName());
            detectedBeacons.add(beacon);
        }
        //YZ: ++
        beaconNames.add("04329774-831d-41fc-995d-7496e001e890");
        beaconNames.add("44329884-831d-41ec-105r-7496f001f896");
        beaconNames.add("04329774-831d-41fc-995e-7496e001e890");

        SharedPreferences prefs = getSharedPreferences(userPreferences, MODE_PRIVATE);
        //---get the values in the EditText view to preferences---

        email = prefs.getString(USER_EMAIL, "");
        email = "YZHEN004@ODU.EDU";

        email = email.replace('.','-');

        calActivity = this;

        btnCalendar = (Button) findViewById(R.id.btnCalendar);

        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CalendarUtility.activity = calActivity;
                FirebaseUtility.activity = calActivity;

                //DeleteCalendar();
                //PopulateCalendar(email, true);

                PopulateAllCalendar();

                Toast.makeText(getApplicationContext(), "Schedule added to calendar", Toast.LENGTH_LONG).show();

                //EditCalendar();

                //LaunchCalendar();
            }
        });

        btnLaunchCalendar = (Button) findViewById(R.id.btnLaunchCalendar);

        btnLaunchCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                //EditCalendar();

                LaunchCalendar();
            }
        });


        btnCheckIn = (Button) findViewById(R.id.btnCheckIn);

        btnCheckIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //CheckIn(email);

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        CheckIn(email);
    }

    //Check in according to email
    //Get the current date
    //Look at Student's calendar node in Firebase
    //Example: https://attendancecs441.firebaseio.com/students/YZHEN004%40ODU-EDU/Calendar/2015-11-26
    //Find the list of schedules for the current date
    //Find the class in the schedules by comparing the current time to the fromTime and toTime of the class
    //And also compare the surrounding beacons to the beacon of the class
    //If match, then add the attendance record
    //Example: https://attendancecs441.firebaseio.com/attendance/12467/YZHEN004%40ODU-EDU/2015-11-26
    private void CheckIn(String email)
    {
        final String studentEmail = email;
        ArrayList<Schedule> listSchedule = new ArrayList<Schedule>();

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        final long time = hour * 100 + minute;

        String currentDateString = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

        //Path :https://attendancecs441.firebaseio.com/students/YZHEN004%40ODU-EDU/Calendar/2015-09-29
        ref.child("students").child(email).child("Calendar").child(currentDateString).addChildEventListener(
                new ChildEventListener() {

                    HashSet<String> keys2 = new HashSet<String>();
                    String key2;
                    long fromTime;
                    long toTime;
                    String beaconID;
                    String abbrev;

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        key2 = dataSnapshot.getKey();   //CRN

                        if (!keys2.contains(key2)) {
                            keys2.add(key2);

                            Schedule schedule = dataSnapshot.getValue(Schedule.class);

                            fromTime = Integer.parseInt(schedule.getFromTime());
                            toTime = Integer.parseInt(schedule.getToTime());
                            beaconID = schedule.getBeaconID();

                            abbrev = schedule.getAbbreviation();

                            if (time > fromTime && time < toTime) {

                                for (int i = 0; i < beaconNames.size(); i++) {
                                    if (beaconID.equals(beaconNames.get(i))) {
                                        FirebaseUtility.AddAttendance(ref, beaconID, abbrev, studentEmail, schedule.getCrn());
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

    }

    //From date: beginning of the semester
    //To date: end of the semester
    //Look at the classes under students
    //Example: https://attendancecs441.firebaseio.com/students/YZHEN004%40ODU-EDU/classes
    //Look for the schedules  of the classes of the student under the CRNschedule by CRN number
    //Example: 12467
    //Example: https://attendancecs441.firebaseio.com/CRNschedule/12467
    //Populate the Android calendar app and the Firebase Calendar node under the students
    //Example: https://attendancecs441.firebaseio.com/attendance/12467/YZHEN004%40ODU-EDU/2015-11-26
    private void PopulateCalendar(String email, final boolean populateCalendarApp)
    {
        final String studentEmail = email;
        final Calendar fromDate = GregorianCalendar.getInstance();
        fromDate.set(2015, 8, 25, 0, 0, 1);

        final Calendar toDate = GregorianCalendar.getInstance();
        toDate.set(2015, 12, 13, 0, 0, 1);

        Firebase.setAndroidContext(calActivity);
        FirebaseUtility.activity = calActivity;
        CalendarUtility.activity = calActivity;

        //Find the class under the students node in Firebase
        ref.child("students").child(email).child("classes").addChildEventListener(new ChildEventListener() {
            HashSet<String> keys = new HashSet<String>();
            String key;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                key = dataSnapshot.getKey();   //CRN

                if(!keys.contains(key)) {
                    keys.add(key);
                    Log.d("Add Schedule", dataSnapshot.getValue().toString());

                    //
                    ref.child("CRNschedule").child(key).child("schedules").addChildEventListener(
                            new ChildEventListener() {

                                HashSet<String> keys2 = new HashSet<String>();
                                String key2;

                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    key2 = dataSnapshot.getKey();   //CRN

                                    if (!keys2.contains(key2)) {
                                        keys2.add(key2);
                                        Schedule schedule = dataSnapshot.getValue(Schedule.class);

                                        //Populate the Android calendar app and the Firebase Calendar node under the students
                                        FirebaseUtility.pushCalendar(fromDate.getTime(), toDate.getTime(), studentEmail
                                                , schedule.getCrn(), schedule.getWeekDay(),
                                                schedule.getFromTime(), schedule.getToTime(), schedule.getAbbreviation(), schedule.getBeaconID(), "", populateCalendarApp);
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });


                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    //Populate the calendar nodes of all students.
    private void PopulateAllCalendar()
    {
        ref.child("attendance").setValue(null);

        ref.child("students").addChildEventListener(new ChildEventListener() {
            HashSet<String> keys = new HashSet<String>();
            String key;
            String email;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null) {
                    key = dataSnapshot.getKey();   //CRN

                    if (!keys.contains(key)) {
                        keys.add(key);
                        email = key;
                        PopulateCalendar(email, false);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void ReadCurrentEvent(Activity calActivity)
    {
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, Calendar.getInstance().getTimeInMillis());
        ContentUris.appendId(eventsUriBuilder, Calendar.getInstance().getTimeInMillis() + 1000 * 60 * 60);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = null;

/*        Time t = new Time();

        String dtStart = Long.toString(t.toMillis(false));

        String dtEnd = Long.toString(t.toMillis(false));*/

        String[] columns = new String[]{};

        cursor = calActivity.getContentResolver().query(eventsUri, columns, null, null, CalendarContract.Instances.DTSTART + " ASC");
    }


    private void LaunchCalendar()
    {
        Calendar today = Calendar.getInstance();

        Uri uriCalendar = Uri.parse("content://com.android.calendar/time/" + String.valueOf(today.getTimeInMillis()));
        Intent intentCalendar = new Intent(Intent.ACTION_VIEW,uriCalendar);

        //Use the native calendar app to view the date
        startActivity(intentCalendar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
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

    private void DeleteCalendar()
    {
        String DEBUG_TAG = "MyActivity";

        for(int eventID =0; eventID < 1000; eventID++) {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            Uri deleteUri = null;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            int rows = getContentResolver().delete(deleteUri, null, null);
            Log.i(DEBUG_TAG, "Rows deleted: " + rows);
        }
    }

    @Override
    public void onBeaconServiceConnect() {
/*        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            //Update list whenever beacons are found
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                beaconsList.clear();
                for (Beacon beacon : beacons) {
                    beaconsList.add(beacon);
                }
            }
        });*/
    }
}
