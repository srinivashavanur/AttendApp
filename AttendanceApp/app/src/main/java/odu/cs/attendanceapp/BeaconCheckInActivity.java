package odu.cs.attendanceapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

/*
BeaconCheckInActivity (Student interaction needed):
Assumption: The student can see multiple beacons on his cell phone
•	Added schedule fragment
•	User click on the beacon and the list of classes under that beacon appears in the listview of ScheduleFragment; the schedule is retrieved from schedule/BeaconID/schedules
•	mFirebaseScheduleRef = mFirebaseRef.child("schedule").child(sBeaconID).child("schedules");
•	https://attendancecs441.firebaseio.com/schedule/04329774-831d-41fc-995d-7496e001e891/schedules
•	User clicks “Check In” and add a node under Attendance, example below
https://attendancecs441.firebaseio.com/attendance/13195/yzhen004%40odu-edu/2015-11-27

 */

public class BeaconCheckInActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "BeaconCheckInActivity";

    //List of beacons that are found
    private ArrayList<Beacon> beaconsList;

    //ArrayAdapter to display information in a listview
    private ArrayAdapter<String> mBeaconsAdapter;

    //Beacon manager needed to connect to beacons
    private BeaconManager beaconManager;

    //List of beacon names
    ArrayList<String> beaconNames = new ArrayList<>();

    //Shared preferences that store all the beacons added
    private SharedPreferences prefs;
    private String beaconPreferences = "BeaconPreferences";
    private String BEACON_IDS_KEY = "BeaconIDsKey";

    private String mStudentEmail;

    private String userPreferences = "UserPreferences";
    private static final String USER_LOGIN = "userLogInPref";
    private static final String USER_EMAIL = "userEmailPref";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private String mBeaconID; // YZ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_selector);

        SharedPreferences prefs = getSharedPreferences(userPreferences, MODE_PRIVATE);
        //---get the values in the EditText view to preferences---

        mStudentEmail = prefs.getString(USER_EMAIL, "");

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

        //update the listview with all the beacons seen by the phone
        updateListView();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            updateListView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            //Update list whenever beacons are found
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                beaconsList.clear();
                for (Beacon beacon : beacons) {
                    beaconsList.add(beacon);
                }
            }
        });
/*
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
        updateListView();*/
    }


    //Method to update the listview
    private void updateListView() {

        //List of beacon names
        //ArrayList<String> beaconNames = new ArrayList<>(); //YZ: Move list of beacon names to class level

        //List of beacons
        final ArrayList<Beacon> detectedBeacons = new ArrayList<>();
        for (Beacon beacon : beaconsList) {
            beaconNames.add(beacon.getBluetoothName());
            detectedBeacons.add(beacon);
        }
        //YZ: ++
        beaconNames.add("04329774-831d-41fc-995d-7496e001e890");
        beaconNames.add("24329774-831d-41ec-995d-7496f001f896");
        beaconNames.add("04329774-831d-41fc-995e-7496e001e890");

        final ScheduleFragment scheduleFragment = (ScheduleFragment) getFragmentManager().findFragmentById(R.id.fragmentSchedule);

        scheduleFragment.mStudentEmail = mStudentEmail;

        //YZ: --
        //Set up adapter to display values in listview
        mBeaconsAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_beacon,
                R.id.beaconID,
                beaconNames);
        final ListView listView = (ListView) findViewById(R.id.listBeacon);
        listView.setAdapter(mBeaconsAdapter);

        //Behavior of the app when the user clicks on a beacon
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

              /*

                final View v = view;
                final Beacon selectedBeacon = detectedBeacons.get(position);

                mBeaconID = selectedBeacon.getId1().toHexString();*/

                mBeaconID = beaconNames.get(position);
                scheduleFragment.UpdateListview(mBeaconID);

                //LaunchScheduleFragment(mBeaconID);

/*                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked
                                // add beacon id to the course
                                //For the moment, the beacon id is stored in a shared preferences
                                prefs = getSharedPreferences(beaconPreferences, MODE_PRIVATE);
                                Set<String> set = new HashSet<String>(prefs.getStringSet(BEACON_IDS_KEY, new HashSet<String>()));

                                set.add(selectedBeacon.getId1().toString());
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putStringSet(BEACON_IDS_KEY, set);
                                editor.commit();
                                Snackbar.make(v, selectedBeacon.getBluetoothName() + " was added to the class.", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // No button clicked
                                // do nothing
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(BeaconSelectorActivity.this);
                builder.setMessage("Do you want to add " + selectedBeacon.getBluetoothName() + " to your class?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();*/
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "BeaconSelector Page",
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://odu.cs.attendanceapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "BeaconSelector Page",
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://odu.cs.attendanceapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}