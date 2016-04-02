package odu.cs.attendanceapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class TeacherActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences userPrefs;
    private PieChart mPieChart;
    private String userPreferences = "UserPreferences";
    private static final String USER_LOGIN = "userLogInPref";
    private static final String USER_EMAIL = "userEmailPref";
    static int attended=0,excused=0,missed=0;
    String crnofstudent,studentDetailss;
    String crn;
    ArrayList<String> students;
    ArrayList<String> studentsEmail;
    Button studentsButton;
    String abbreviation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        students = new ArrayList<>();
        studentsEmail = new ArrayList<>();
        studentsButton = (Button) findViewById(R.id.studentsButton);
        studentsButton.setOnClickListener(this);
        userPrefs = getSharedPreferences(userPreferences, MODE_PRIVATE);
        attended=0;
        excused=0;
        missed=0;
        //Get boolean
        boolean userLogged = userPrefs.getBoolean(USER_LOGIN, false);
        if (userLogged) {

            Bundle bundle = getIntent().getExtras();
            crn = bundle.getString("CRNSS");
            abbreviation = bundle.getString("ABREV");
            getSupportActionBar().setTitle(abbreviation);

            //System.out.println("CRN NUMBER IS: "+crnnumber);
            String str =  "https://attendancecs441.firebaseio.com/attendance/" + crn;
            Firebase.setAndroidContext(this);
            Firebase ref = new Firebase(str);
            // System.out.println("Before invoking child");


            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        studentDetailss = String.valueOf(postSnapshot.getValue());

                        try {
                            String initialDateString = "2015-08-25";
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date initialDate = dateFormat.parse(initialDateString);
                            //System.out.println("INITIAL DATE: " + initialDate);
                            Calendar past = Calendar.getInstance();
                            past.setTime(initialDate);
                            Calendar present = Calendar.getInstance();

                            while (past.getTimeInMillis() < present.getTimeInMillis()) {
                                String actualDate = dateFormat.format(past.getTime());
                                //System.out.println(actualDate);
                                try {
                                    JSONObject json = new JSONObject(studentDetailss);
                                    if (json.has(actualDate)) {
                                        //System.out.println("[DEBUG] JSON: " + json.get(actualDate));
                                        String jsonString = json.get(actualDate).toString();
                                        //System.out.println("[DEBUG] JSON string is " + jsonString);
                                        attended += StringUtils.countMatches(json.get(actualDate).toString(), "\"isAttended\":true");
                                        excused += StringUtils.countMatches(json.get(actualDate).toString(), "\"isExcused\":true");
                                        missed += StringUtils.countMatches(json.get(actualDate).toString(), "\"isMissed\":true");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                past.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("Attended "+attended);
                    System.out.println("Excused "+excused);
                    System.out.println("Missed "+missed);
                    final String[] xData = {"Attended: "+attended, "Missed with Consent: "+excused, "Missed without Consent: "+missed};
                    float[] yData = {attended,excused,missed};

                    //**START HERE

                    mPieChart = (PieChart) findViewById(R.id.piechartteacher);
                    //Configure pie chart
                    mPieChart.setUsePercentValues(true);
                    mPieChart.setDescription("Attendance of class " + abbreviation + " (CRN " + crn + ")");

                    //Enable hole and configure
                    mPieChart.setDrawHoleEnabled(true);
                    mPieChart.setHoleColorTransparent(true);
                    mPieChart.setHoleRadius(7);
                    mPieChart.setTransparentCircleRadius(10);

                    //Enable rotation by touch
                    mPieChart.setRotationAngle(0);
                    mPieChart.setRotationEnabled(true);


                    //Set a listener for the selected value
                    mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry entry, int i, Highlight highlight) {
                            Toast.makeText(TeacherActivity.this, xData[entry.getXIndex()] + " = " + entry.getVal() + "%", Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });

                    //Add data
                    ArrayList<Entry> yVals = new ArrayList<Entry>();

                    for (int i=0; i<yData.length; i++)
                        yVals.add(new Entry(yData[i], i));

                    ArrayList<String> xVals = new ArrayList<String>();

                    for (int i=0; i<xData.length; i++)
                        xVals.add(xData[i]);

                    //Create pie data set
                    PieDataSet dataSet = new PieDataSet(yVals, "");
                    dataSet.setSliceSpace(3);
                    dataSet.setSelectionShift(5);

                    //Add colors

                    ArrayList<Integer> colors = new ArrayList<Integer>();

                    colors.add(Color.parseColor("#4CAF50"));
                    colors.add(Color.parseColor("#FFA000"));
                    colors.add(Color.parseColor("#F44336"));

                    dataSet.setColors(colors);

                    //Instantiate pie data object
                    PieData data = new PieData(xVals, dataSet);
                    data.setValueFormatter(new PercentFormatter());
                    data.setValueTextSize(11f);
                    data.setValueTextColor(Color.WHITE);
                    mPieChart.setData(data);
                    mPieChart.setDrawSliceText(false);
                    //Undo highlights
                    mPieChart.highlightValues(null);

                    //Update pie chart
                    mPieChart.invalidate();

                    //Customize legends
                    Legend l = mPieChart.getLegend();
                    l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
                    l.setXEntrySpace(7);
                    l.setYEntrySpace(2);






                    //Get students based on crn

                    Firebase studentsRef = new Firebase(getString(R.string.firebase_url)).child("classes").child(crn).child("students");

                    studentsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                Map<String, Object> newStudent = (Map<String, Object>) postSnapshot.getValue();
                                System.out.println(String.valueOf(newStudent.get("studentEmail")));
                                students.add(String.valueOf(newStudent.get("studentName")));
                                studentsEmail.add(String.valueOf(newStudent.get("studentEmail")));
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });


                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });


        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_teacher_activity, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

         if (id == R.id.action_addBeacons) {
             Intent beaconSelect = new Intent(this, BeaconSelectorActivity.class);
             Bundle bundle = new Bundle();

             bundle.putString("CRN", String.valueOf(crn));
             bundle.putString("ABREV", abbreviation);
             beaconSelect.putExtras(bundle);

             startActivity(beaconSelect);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        final CharSequence studentsSeqence[] = new CharSequence[students.size()];

        for(int i = 0; i<studentsSeqence.length; i++){
            studentsSeqence[i] = students.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a student");
        builder.setItems(studentsSeqence, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent appInfo = new Intent(TeacherActivity.this, ClassActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("CRNSS", crn);

                bundle.putString("STEMAIL", studentsEmail.get(which));
                bundle.putString("ABREV", abbreviation);
                bundle.putBoolean("COMESFROMTEACHER", true);
                appInfo.putExtras(bundle);

                startActivity(appInfo);
            }
        });
        builder.show();
    }
}
