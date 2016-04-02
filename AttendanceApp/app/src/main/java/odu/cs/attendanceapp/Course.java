package odu.cs.attendanceapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingbozheng on 10/26/2015.
 */
public class Course
{

    private String courseName;
    private String courseAbbrev;
    private String CRN;
    private User teacher;
    private String courseNum;
    private List<Student> students;
    private String classLocation;
    private List<Session> sessions;
    private List<String> beaconIDs;
    private List<Teacher> teachers;

    public Course()
    {
        courseName = "";
        courseAbbrev = "";
        teacher = null;
        classLocation = null;
        this.students = new ArrayList<>();
        this.sessions = new ArrayList<>();
        beaconIDs = new ArrayList<>();
        teachers = new ArrayList<>();
    }

    public Course(String courseAbbrev, String CRN)
    {
        this.CRN = CRN;
        this.courseAbbrev = courseAbbrev;
        teacher = null;
        classLocation = null;
        this.students = new ArrayList<>();
        this.sessions = new ArrayList<>();
        beaconIDs = new ArrayList<>();
        this.teachers = new ArrayList<>();
    }

    public Course (String courseName, String courseAbbrev, User teacher, String classLocation)
    {
        this.teacher = teacher;
        this.classLocation = classLocation;
        this.courseAbbrev = courseAbbrev;
        this.courseName = courseName;
        this.courseNum = courseNum;
        this.students = new ArrayList<>();
        this.sessions = new ArrayList<>();
        beaconIDs = new ArrayList<>();
        this.teachers = new ArrayList<>();
    }

    public String getName()
    {
        return courseName;
    }

    public String getAbbreviation()
    {
        return courseAbbrev;
    }

    public List<Student> getStudents()
    {
        return students;
    }

    public List<Session> getSessions() {return sessions;}

    public String getCRN() {return CRN;}

    public String getLocation()
    {
        return classLocation;
    }

    public List<Teacher> getTeachers()
    {
        return teachers;
    }

    public void addBeacon(String beaconId){
        beaconIDs.add(beaconId);
    }

    public void addStudent(Student student){
        students.add(student);
    }

    public void addTeacher(Teacher teacher) { teachers.add(teacher);}

    public void addSession(Session session){
        sessions.add(session);
    }

    public List<String> getBeacons(){return beaconIDs;}
}
