package odu.cs.attendanceapp;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class LoadFile extends AppCompatActivity implements FilePickerFragment.OnFragmentInteractionListener {

    EditText etFilePath;
    Button btnFilePath;

    Button btnLoad;

    Button btnEnroll;

    Button btnEnrollPath;

    Button btnLSPath;

    Button btnLoadLSFile;

    EditText etLSPath;

    EditText etEnrollPath;

    EditText etTeacherPath;

    Button btnLoadTeacherFile;

    Button btnTeacherPath;

    private String mChosenFile;

    Firebase mFirebaseRef;

    HashMap<String, CRNSchedule> scheduleMap = new HashMap<>();
    HashMap<String, LocationSchedule> locationScheduleMap = new HashMap<>();
    HashMap<String, LocationSchedule> CRNScheduleMap = new HashMap<>();
    HashMap<String, Course> enrollmentMap = new HashMap<>();
    HashMap<String, Course> teacherMap = new HashMap<>();

    //Read enrollment file
    //Call FirebaseUtility.AddStudentClass()
    //Add classes to Firebase node students/Email/classes
    //Ex: https://attendancecs441.firebaseio.com/students/YZHEN004%40ODU-EDU/classes
    //
    //Call saveEnrollment()
    //Populates the Firebase node classes/CRN/students
    //Example: https://attendancecs441.firebaseio.com/classes/12467/students
    private void readEnrollmentFile()
    {
        String abbreviation;

        boolean isFirst = true;

        Course course;

        Student student;

        String courseAbbrev;
        String courseNum;
        String studentName;
        String studentID;
        String studentEmail;
        String studentPhone;

        enrollmentMap.clear();

        //Get the csv file
        File file = new File(Environment.getExternalStorageDirectory(), etEnrollPath.getText().toString());

        //Read text from file
        StringBuilder text = new StringBuilder();

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if(!isFirst)
                {
                    courseAbbrev = line.substring(0, 3);
                    courseNum = line.substring(4, 9);
                    studentName = line.substring(10, 41);
                    studentID = line.substring(42, 51);
                    studentEmail = line.substring(52, 68).toLowerCase();
                    studentPhone = line.substring(69, 79);

                    FirebaseUtility.AddStudentClass(studentEmail,courseAbbrev,courseNum);

                    student = new Student(studentName, studentID, studentEmail, studentPhone);

                    if(enrollmentMap.containsKey(courseAbbrev))
                    {
                        course = enrollmentMap.get(courseAbbrev);
                        course.addStudent(student);
                    }

                    else
                    {
                        course = new Course(courseAbbrev, courseNum);

                        course.addStudent(student);

                        enrollmentMap.put(courseAbbrev, course);
                    }
                }
                else
                {
                    isFirst = false;
                }
            }
            br.close();

            saveEnrollment();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

    }

    //Populates the Firebase node classes/CRN/students
    //Example: https://attendancecs441.firebaseio.com/classes/12467/students
    private void saveEnrollment()
    {
        Course course;

        Iterator it = enrollmentMap.entrySet().iterator();
        while (it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();

            course = (Course) pair.getValue();

            //YZ: Creating a course Firebase reference
            Firebase enrollmentRef = mFirebaseRef.child("classes").child(course.getCRN());
            enrollmentRef.setValue(course);

            enrollmentRef = mFirebaseRef.child("classes").child(course.getCRN()).child("students");
            enrollmentRef.setValue(course.getStudents());
        }
    }

    //Read the schedule file without the beacon IDs
    //Populate the CRNschedule node
    private void readScheduleFile()
    {
        String beaconID;

        boolean isFirst = true;

        CRNSchedule ls;

        String[] splitSchedule;

        scheduleMap.clear();

        //Get the csv file
        File file = new File(Environment.getExternalStorageDirectory(), etFilePath.getText().toString());

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if(!isFirst)
                {
                    splitSchedule = line.split(",");

                    beaconID = splitSchedule[1];

                    if(scheduleMap.containsKey(beaconID))
                    {
                        ls = scheduleMap.get(beaconID);

                        ls.addSchedule(splitSchedule[0], splitSchedule[2], splitSchedule[3], splitSchedule[4]);
                    }

                    else {
                        ls = new CRNSchedule(splitSchedule[0], splitSchedule[1], splitSchedule[2], splitSchedule[3], splitSchedule[4]);

                        scheduleMap.put(beaconID, ls);
                    }


                }
                else
                {
                    isFirst = false;
                }
            }
            br.close();

            saveSchedule();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

    }

    private void saveSchedule()
    {
        CRNSchedule ls;

        mFirebaseRef.child("CRNschedule").setValue(null);

        Iterator it = scheduleMap.entrySet().iterator();
        while (it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();

            ls = (CRNSchedule) pair.getValue();

            //YZ: Creating a user Firebase reference
            Firebase scheduleRef = mFirebaseRef.child("CRNschedule").child(ls.getCRN());
            scheduleRef.setValue(ls);
        }
    }

    private void saveTeacherFile()
    {
        Course course;

        Iterator it = enrollmentMap.entrySet().iterator();
        while (it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();

            course = (Course) pair.getValue();

            //YZ: Creating a course Firebase reference
            Firebase enrollmentRef = mFirebaseRef.child("classes").child(course.getCRN());
            enrollmentRef.setValue(course);

            enrollmentRef = mFirebaseRef.child("classes").child(course.getCRN()).child("teachers");
            enrollmentRef.setValue(course.getStudents());
        }
    }

    private void readTeacherFile()
    {
        String abbreviation;

        boolean isFirst = true;

        Course course;

        Teacher teacher;

        String courseAbbrev;
        String courseNum;
        String teacherName;
        String teacherID;
        String teacherEmail;
        String teacherPhone;

        //String substring(int beginIndex, int endIndex)
        Firebase teacherRef;


        //Read text from file
        StringBuilder text = new StringBuilder();

        //Get the csv file
        File file = new File(Environment.getExternalStorageDirectory(), etTeacherPath.getText().toString());
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if(!isFirst)
                {
                    courseAbbrev = line.substring(0, 3);
                    courseNum = line.substring(4, 9);
                    teacherName = line.substring(10, 41);
                    teacherID = line.substring(43, 50);
                    teacherEmail = line.substring(52, 68).toLowerCase();
                    teacherPhone = line.substring(69, 79);

                    teacher = new Teacher(teacherName, teacherEmail);
                    teacherRef = mFirebaseRef.child("classes").child(courseNum).child("teacher");
                    teacherRef.setValue(teacher);

                }
                else
                {
                    isFirst = false;
                }
            }
            br.close();

            saveTeacherFile();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

    }

    //Read the schedule file including the beacon IDs
    //
    //Populate the schedule node and the CRNschedule node
    //Ex: https://attendancecs441.firebaseio.com/CRNschedule/12467/schedules
    //Ex: https://attendancecs441.firebaseio.com/schedule/04329774-831d-41fc-995d-7496e001e890/schedules
    private void readLocationScheduleFile()
    {
        String beaconID;

        String CRN;

        boolean isFirst = true;

        LocationSchedule ls;

        String[] splitSchedule;

        locationScheduleMap.clear();

        CRNScheduleMap.clear();

        //Get the csv file
        File file = new File(Environment.getExternalStorageDirectory(), etLSPath.getText().toString());

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if(!isFirst)
                {
                    splitSchedule = line.split(",");

                    beaconID = splitSchedule[1];

                    CRN = splitSchedule[2];

                    if(locationScheduleMap.containsKey(beaconID))
                    {
                        ls = locationScheduleMap.get(beaconID);

                        ls.addSchedule(splitSchedule[0], splitSchedule[2], splitSchedule[3], splitSchedule[4],splitSchedule[5],beaconID);
                    }

                    else {
                        ls = new LocationSchedule(splitSchedule[0], splitSchedule[1], splitSchedule[2], splitSchedule[3], splitSchedule[4], splitSchedule[5]);

                        locationScheduleMap.put(beaconID, ls);
                    }

                    if(CRNScheduleMap.containsKey(CRN))
                    {
                        ls = CRNScheduleMap.get(CRN);

                        ls.addSchedule(splitSchedule[0], splitSchedule[2], splitSchedule[3], splitSchedule[4],splitSchedule[5], beaconID);
                    }

                    else
                    {
                        ls = new LocationSchedule(splitSchedule[0], splitSchedule[1], splitSchedule[2], splitSchedule[3], splitSchedule[4],splitSchedule[5]);

                        CRNScheduleMap.put(CRN, ls);
                    }
                }
                else
                {
                    isFirst = false;
                }
            }
            br.close();

            saveLocationSchedule();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

    }

    //Populate the schedule node and the CRNschedule node
    //Ex: https://attendancecs441.firebaseio.com/CRNschedule/12467/schedules
    //Ex: https://attendancecs441.firebaseio.com/schedule/04329774-831d-41fc-995d-7496e001e890/schedules
    private void saveLocationSchedule()
    {
        LocationSchedule ls;

        mFirebaseRef.child("schedule").setValue(null);

        Iterator it = locationScheduleMap.entrySet().iterator();
        while (it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();

            ls = (LocationSchedule) pair.getValue();

            //YZ: Creating a user Firebase reference
            Firebase scheduleRef = mFirebaseRef.child("schedule").child(ls.getBeaconID());
            scheduleRef.setValue(ls);
        }

        mFirebaseRef.child("CRNschedule").setValue(null);

        it = CRNScheduleMap.entrySet().iterator();

        while (it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();

            ls = (LocationSchedule) pair.getValue();

            //YZ: Creating a user Firebase reference
            Firebase scheduleRef = mFirebaseRef.child("CRNschedule").child(ls.getCRN());
            scheduleRef.setValue(ls);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_file);

        Firebase.setAndroidContext(this);

        mFirebaseRef = new Firebase(getString(R.string.firebase_url));

        etFilePath = (EditText) findViewById(R.id.etFilePath);

        btnFilePath = (Button) findViewById(R.id.btnFilePath);

        btnFilePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerFragment df = new FilePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.etFilePath = etFilePath;

                df.show(fm, "ScheduleFilePicker");
            }
        });

        btnLoad = (Button) findViewById(R.id.btnLoad);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readScheduleFile();
            }
        });



        etEnrollPath = (EditText) findViewById(R.id.etEnrollPath);

        btnEnrollPath = (Button) findViewById(R.id.btnEnrollPath);

        btnEnrollPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerFragment df = new FilePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.etFilePath =  etEnrollPath;

                df.show(fm, "ScheduleFilePicker");
            }
        });


        btnEnroll = (Button) findViewById(R.id.btnLoadEnroll);

        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readEnrollmentFile();
            }
        });

        etLSPath = (EditText) findViewById(R.id.etLSPath);


        btnLSPath = (Button) findViewById(R.id.btnLSPath);

        btnLSPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerFragment df = new FilePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.etFilePath =  etLSPath;

                df.show(fm, "LocationScheduleFilePicker");
            }
        });

        btnLoadLSFile = (Button) findViewById(R.id.btnLoadLSFile);

        btnLoadLSFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etLSPath.getText().length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please enter a file path", Toast.LENGTH_SHORT).show();
                    //btnLSPath.setFocus
                    return;
                }

                readLocationScheduleFile();
            }
        });

        etTeacherPath = (EditText) findViewById(R.id.etTeacherPath);


        btnTeacherPath = (Button) findViewById(R.id.btnTeacherPath);

        btnTeacherPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerFragment df = new FilePickerFragment();
                FragmentManager fm = getFragmentManager();
                df.etFilePath =  etTeacherPath;

                df.show(fm, "Teacher File Picker");
            }
        });

        btnLoadTeacherFile = (Button) findViewById(R.id.btnTeacherFile);

        btnLoadTeacherFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etTeacherPath.getText().length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please enter a file path", Toast.LENGTH_SHORT).show();
                    return;
                }

                readTeacherFile();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load_file, menu);
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

    @Override
    public void onFragmentInteraction(View v, String filePath) {
        EditText et = (EditText) v;

        et.setText(filePath);
        mChosenFile = filePath;
    }
}
