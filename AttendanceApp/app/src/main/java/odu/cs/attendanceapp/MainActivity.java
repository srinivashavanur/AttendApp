package odu.cs.attendanceapp;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BootstrapNotifier {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RegionBootstrap regionBootstrap;
    BeaconManager beaconManager;
    int occur, occurance;
    private SharedPreferences userPrefs;
    private String userPreferences = "UserPreferences";
    private static final String USER_LOGIN = "userLogInPref";
    private static final String USER_EMAIL = "userEmailPref";
    ArrayList<String> crndetails = new ArrayList<String>();
    ArrayList<Boolean> isTeacher = new ArrayList<>();
    String crns, crnofstudent, studentDetails;
    boolean inClass = false;
    String nameOfClass = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize preferences
        userPrefs = getSharedPreferences(userPreferences, MODE_PRIVATE);

        //Get boolean
        boolean userLogged = userPrefs.getBoolean(USER_LOGIN, false);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(userLogged) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CharSequence colors[] = new CharSequence[]{"Add manually", "Load from file"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Add a class");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    Intent courseInIntent = new Intent(MainActivity.this, CourseActivity.class);
                                    startActivity(courseInIntent);
                                    break;
                                case 1:
                                    Intent loadIntent = new Intent(MainActivity.this, LoadFile.class);
                                    startActivity(loadIntent);
                                    break;
                            }
                        }
                    });
                    builder.show();


                }
            });
        }else{
            fab.hide();
        }

        Firebase.setAndroidContext(this);


        //If user is logged, get information from Firebase
        if (userLogged) {

            beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
            final ListView listView = (ListView) findViewById(R.id.listView);

            // Create a new Adapter
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1);

            final ArrayList<String> abbreviations = new ArrayList<>();
            // Assign adapter to ListView
            listView.setAdapter(adapter);

            // Use Firebase to populate the list.

            Firebase ref = new Firebase("https://attendancecs441.firebaseio.com/classes");
            final boolean[] bluetoothEnable = {false};
            ref.addChildEventListener(new ChildEventListener() {
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    final String textName = userPrefs.getString(USER_EMAIL, "?¿");

                    Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                    String studentDetails = String.valueOf(newPost.get("students"));
                    String teacherDetails = String.valueOf(newPost.get("teacher"));

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {

                            if (isTeacher.get(position)) {
                                //Go to teacher activity

                                Intent appInfo = new Intent(MainActivity.this, TeacherActivity.class);
                                Bundle bundle = new Bundle();

                                bundle.putString("CRNSS", String.valueOf(crndetails.get(position)));
                                bundle.putString("ABREV", String.valueOf(abbreviations.get(position)));
                                appInfo.putExtras(bundle);

                                startActivity(appInfo);
                            } else {
                                Intent appInfo = new Intent(MainActivity.this, ClassActivity.class);
                                Bundle bundle = new Bundle();

                                bundle.putString("CRNSS", String.valueOf(crndetails.get(position)));
                                bundle.putString("ABREV", String.valueOf(abbreviations.get(position)));
                                bundle.putString("STEMAIL", textName);
                                bundle.putBoolean("COMESFROMTEACHER", false);
                                appInfo.putExtras(bundle);

                                startActivity(appInfo);
                            }
                        }
                    });

                    if (studentDetails.contains(textName)) {
                        crns = String.valueOf(newPost.get("crn"));
                        String abbreviation = String.valueOf(newPost.get("abbreviation"));
                        abbreviations.add(abbreviation);
                        isTeacher.add(false);
                        crndetails.add(crns);
                        adapter.add(abbreviation);
                        readBeaconFromCRN(crns);

                        //If it is a student, the app will require bluetooth
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        int REQUEST_ENABLE_BT = 1;
                        if (mBluetoothAdapter == null) {
                            // Device does not support Bluetooth
                        } else {
                            if (!mBluetoothAdapter.isEnabled() && !bluetoothEnable[0]) {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                bluetoothEnable[0] = true;
                            }
                        }
                    } else if (teacherDetails.contains(textName)) {
                        crns = String.valueOf(newPost.get("crn"));
                        String abbreviation = String.valueOf(newPost.get("abbreviation"));
                        abbreviations.add(abbreviation);
                        crndetails.add(crns);
                        isTeacher.add(true);
                        adapter.add(abbreviation + " (teacher)");

                    }

                }


                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    adapter.remove((String) dataSnapshot.child("students").getValue());

                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(FirebaseError firebaseError) {
                }
            });

        }

    }

    void classEntered(final String crn) {

        //Initialize preferences
        userPrefs = getSharedPreferences(userPreferences, MODE_PRIVATE);

        //Get boolean
        boolean userLogged = userPrefs.getBoolean(USER_LOGIN, false);

        //If user is logged, get information from Firebase
        if (userLogged) {
            //Send data to Firebase
            final String studentEmail = userPrefs.getString(USER_EMAIL, "");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            final String date = df.format(Calendar.getInstance().getTime());

            final Firebase calendarRef = new Firebase(getString(R.string.firebase_url)).child("students").child(studentEmail.replace('.', '-').toLowerCase()).child("Calendar").child(date);



            calendarRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String calendarDetails = String.valueOf(postSnapshot.getValue());

                        JSONObject json = null;
                        try {
                            json = new JSONObject(calendarDetails);
                            int fromTime = json.getInt("fromTime");
                            int toTime = json.getInt("toTime");
                            Calendar calendar = Calendar.getInstance();
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            final long time = hour * 100 + minute;
                            System.out.println("Now is " + time + " and you have class from " + fromTime + " to " + toTime);

                            if (time > fromTime && time < toTime && calendarDetails.contains(crn)) {
                                System.out.println("You entered in class");

                                inClass = true;

                                Map<String, Object> arrivalMap = new HashMap<>();
                                arrivalMap.put("isMissed", false);
                                arrivalMap.put("isAttended", true);

                                final Firebase attendanceRef = new Firebase(getString(R.string.firebase_url)).child("attendance").child(crn).child(studentEmail.replace('.', '-').toLowerCase());
                                attendanceRef.child(date).updateChildren(arrivalMap);
                                //Set notification
                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(getBaseContext())
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentTitle("You are in class!")
                                                .setContentText("You just entered in class " + crn)
                                                .setVibrate(new long[]{0, 300, 200, 300});

                                // Creates an explicit intent for an Activity in your app
                                Intent resultIntent = new Intent(getBaseContext(), MainActivity.class);

                                PendingIntent resultPendingIntent =
                                        PendingIntent.getActivity(getBaseContext(),
                                                0,
                                                resultIntent,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                // Id allows you to update the notification later on.
                                mNotificationManager.notify(0, mBuilder.build());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    void readBeaconFromCRN(final String crn) {
        final ArrayList<String> beaconIDs = new ArrayList<>();
        Firebase ref = new Firebase("https://attendancecs441.firebaseio.com/CRNschedule");

        ref.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                String scheduleDetails = String.valueOf(newPost);
                if (scheduleDetails.contains(crn)) {
                    String beaconID = String.valueOf(newPost.get("beaconID"));
                    beaconIDs.add(beaconID);
                    BeaconDBAdapter beaconDB = new BeaconDBAdapter(MainActivity.this);
                    beaconDB.open();
                    Region region = new Region("com.example.backgroundRegion",
                            Identifier.parse(beaconID), null, null);


                    //Add only if beacon has not been previously added
                    if (!beaconDB.alreadyInDB(beaconID)) {
                        beaconDB.insertEntry(beaconID, crn);
                        regionBootstrap = new RegionBootstrap(MainActivity.this, region);
                        System.out.println(beaconID + " is now being read in the background.");
                    }
                    beaconDB.close();
                }
            }


            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Initialize preferences
        userPrefs = getSharedPreferences(userPreferences, MODE_PRIVATE);

        //Get boolean
        boolean userLogged = userPrefs.getBoolean(USER_LOGIN, false);
        if (userLogged) {
            getMenuInflater().inflate(R.menu.menu_main_log_out, menu);

        } else {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main_log_in, menu);
        }
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
        } else if (id == R.id.action_signIn) {
            Intent signInIntent = new Intent(this, LoginActivity.class);
            startActivity(signInIntent);
            return true;
        } else if (id == R.id.action_signOut) {
            userPrefs = getSharedPreferences(userPreferences, MODE_PRIVATE);
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.putBoolean(USER_LOGIN, false);
            editor.putString(USER_EMAIL, "");
            editor.commit();
            recreate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void didEnterRegion(Region region) {

        System.out.println( "Region "+region.getId1().toString()+" entered");
        //Get the crn associated with the beacon
        BeaconDBAdapter beaconDB = new BeaconDBAdapter(MainActivity.this);
        beaconDB.open();
        String crn = beaconDB.getCRN(region.getId1().toString());
        beaconDB.close();
        System.out.println("CRN is " + crn);
        //Update the attendance data
        classEntered(crn);
    }

    @Override
    public void didExitRegion(Region region) {
        System.out.println("A region was left");
        if(inClass) {
            inClass = false;
            BeaconDBAdapter beaconDB = new BeaconDBAdapter(MainActivity.this);
            beaconDB.open();
            String crn = beaconDB.getCRN(region.getId1().toString());
            beaconDB.close();
            //Set notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getBaseContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("You are no longer in class")
                            .setContentText("You just left class " + crn)
                            .setVibrate(new long[]{0, 300, 200, 300});

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(getBaseContext(), MainActivity.class);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(getBaseContext(),
                            1,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // Id allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
