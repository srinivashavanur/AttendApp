package odu.cs.attendanceapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParser;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class ClassActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private SharedPreferences userPrefs;
    private PieChart mPieChart;
    Context context = this;
    private String userPreferences = "UserPreferences";
    private static final String USER_LOGIN = "userLogInPref";
    private static final String USER_EMAIL = "userEmailPref";
    static int attended=0,excused=0,missed=0;
    String crnofstudent,studentDetailss;
    ArrayList<String> attendedDetails = new ArrayList<String>();
    int attend=10,miss=2,missc=2;
    int attendedCounter=0;
    StringBuilder stringBuilder1 = new StringBuilder();
    StringBuilder stringBuilder2 = new StringBuilder();
    StringBuilder stringBuilder3 = new StringBuilder();
    final String[] xData = {"Attended: "+attended, "Missed with Consent: "+excused, "Missed without Consent: "+missed};
    boolean comesFromTeacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userPrefs = getSharedPreferences(userPreferences, MODE_PRIVATE);
        attended=0;
        excused=0;
        missed=0;
        //Get boolean
        boolean userLogged = userPrefs.getBoolean(USER_LOGIN, false);
        if (userLogged) {

            Bundle bundle = getIntent().getExtras();
            final String userEmail = bundle.getString("STEMAIL");
            String textNames=userEmail.replace(".", "-");
            final String crnnumber = bundle.getString("CRNSS");
            final String abbreviation = bundle.getString("ABREV");
            comesFromTeacher = bundle.getBoolean("COMESFROMTEACHER");
            if(!comesFromTeacher)
                getSupportActionBar().setTitle(abbreviation);
            else
                getSupportActionBar().setTitle(userEmail);
            //System.out.println("CRN NUMBER IS: "+crnnumber);
            String str =  "https://attendancecs441.firebaseio.com/attendance" + '/' + crnnumber + '/' + textNames;

            Firebase ref = new Firebase(str);
            // System.out.println("Before invoking child");


            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    studentDetailss = String.valueOf(dataSnapshot.getValue());
                    // System.out.println("Student details : " + studentDetailss);

                    try {

                        String initialDateString = "2015-08-25";
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        Date initialDate = format.parse(initialDateString);
                        System.out.println("INITIAL DATE: "+initialDate);
                        Calendar past = Calendar.getInstance();
                        past.setTime(initialDate);
                        Calendar present = Calendar.getInstance();

                        while(past.getTimeInMillis()<present.getTimeInMillis()){
                            String actualDate = format.format(past.getTime());
                            // System.out.println("Actual date: "+actualDate);
                            try{
                                JSONObject json = new JSONObject(studentDetailss);
                                if(json.has(actualDate)) {


                                    String jsonString = json.get(actualDate).toString();

                                    String jsonstringnew = json.toString();
                                    if(StringUtils.countMatches(json.get(actualDate).toString(), "\"isAttended\":true")!=0)
                                    {
                                        stringBuilder1.append(actualDate+"\n");
                                        attendedDetails.add(actualDate);
                                        System.out.println("Attended dates are: "+actualDate);
                                    }
                                    // Dates of classes that are excused.
                                    if(StringUtils.countMatches(json.get(actualDate).toString(), "\"isExcused\":true")!=0)
                                    {
                                        stringBuilder2.append(actualDate+"\n");
                                        System.out.println("Excused dates are here: " + actualDate);
                                    }
                                    //Dates of classes that are missed.
                                    if(StringUtils.countMatches(json.get(actualDate).toString(), "\"isMissed\":true")!=0)
                                    {
                                        stringBuilder3.append(actualDate+"\n");
                                        System.out.println("Missed dates are here: " + actualDate);
                                    }
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

                    //System.out.println("STDUENTS DETAILS "+studentDetailss);
                    System.out.println("Attended "+attended);
                    System.out.println("Excused "+excused);
                    System.out.println("Missed "+missed);

                    if(!comesFromTeacher && (attended + missed + excused) !=0) {
                        int attendedPercentage = (attended * 100) / (attended + missed + excused);

                        if (attendedPercentage < 75) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // No button clicked
                                            // do nothing
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(ClassActivity.this);
                            builder.setMessage("Your attendance is " + attendedPercentage + "%, please meet with your instructor.")
                                    .setPositiveButton("OK", dialogClickListener).show();
                        }
                    }
                    final String[] xData = {"Attended: "+attended, "Missed with Consent: "+excused, "Missed without Consent: "+missed};
                    float[] yData = {attended,excused,missed};

                    //**START HERE

                    mPieChart = (PieChart) findViewById(R.id.piechart);
                    //Configure pie chart
                    mPieChart.setUsePercentValues(true);

                    String description = "";
                    if(comesFromTeacher) {
                        description = "Attendance data of " +userEmail+" in " + abbreviation + " (CRN " + crnnumber + ")";
                    }else{
                        description = "Your attendance data in " + abbreviation + " (CRN " + crnnumber + ")";
                    }
                    mPieChart.setDescription(description);


                    //Enable hole and configure
                    mPieChart.setDrawHoleEnabled(true);
                    mPieChart.setHoleColorTransparent(true);
                    mPieChart.setHoleRadius(7);
                    mPieChart.setTransparentCircleRadius(10);

                    //Enable rotation by touch
                    mPieChart.setRotationAngle(0);
                    mPieChart.setRotationEnabled(true);

                    //Set a listener for the selected value
                    mPieChart.setOnChartValueSelectedListener(ClassActivity.this);
//


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
                    l.setYEntrySpace(5);


                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });


        }




    }


    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {
        String message="";
        if(xData[entry.getXIndex()].contains("Missed with Consent")){
            message = "Excused Dates are here: \n" + stringBuilder2.toString();
        }else if(xData[entry.getXIndex()].contains("Missed without Consent")){
            message = "Missed Dates are here: \n" + stringBuilder3.toString();
        }else if(xData[entry.getXIndex()].contains("Attended")){
            message = "Attended Dates are here: \n" + stringBuilder1.toString();
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ClassActivity.this);
        builder.setCancelable(true);
        builder.setMessage(message)
                .show();

    }

    @Override
    public void onNothingSelected() {

    }
}

