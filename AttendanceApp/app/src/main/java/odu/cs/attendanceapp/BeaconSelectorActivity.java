package odu.cs.attendanceapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.Firebase;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BeaconSelectorActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "BeaconSelectorActivity";

    //List of beacons that are found
    private ArrayList<Beacon> beaconsList;

    //ArrayAdapter to display information in a listview
    private ArrayAdapter<String> mBeaconsAdapter;

    //Beacon manager needed to connect to beacons
    private BeaconManager beaconManager;

    //Shared preferences that store all the beacons added
    private SharedPreferences prefs;
    private String beaconPreferences = "BeaconPreferences";
    private String BEACON_IDS_KEY = "BeaconIDsKey";
    String crn;
    String abbreviation;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_selector);

        //Initialize beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        //Initialize beacon list
        beaconsList = new ArrayList<>();

        //Get CRN from teacher activity
        Bundle bundle = getIntent().getExtras();
        crn = bundle.getString("CRN");
        abbreviation = bundle.getString("ABREV");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Beacons to "+abbreviation);
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
                    System.out.println("Detecting beacon: " + beacon.getId1().toString());
                    beaconsList.add(beacon);
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
        updateListView();
    }


    //Method to update the listview
    private void updateListView() {

        //List of beacon names
        ArrayList<String> beaconNames = new ArrayList<>();

        //List of beacons
        final ArrayList<Beacon> detectedBeacons = new ArrayList<>();
        for (Beacon beacon : beaconsList) {
            beaconNames.add(beacon.getBluetoothName());
            detectedBeacons.add(beacon);
            System.out.println("Adding beacon " + beacon.getId1().toString());
        }
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

                final View v = view;
                final Beacon selectedBeacon = detectedBeacons.get(position);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked
                                //add beacon id to the course
                                Map<String, Object> beaconID = new HashMap<>();
                                beaconID.put("beaconID", selectedBeacon.getId1().toString());
                                beaconID.put("crn", crn);
                                Firebase beaconRef = new Firebase(ApplicationConstants.FIREBASE_URL).child("CRNschedule").child(crn);
                                beaconRef.updateChildren(beaconID);
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
                        .setNegativeButton("No", dialogClickListener).show();
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