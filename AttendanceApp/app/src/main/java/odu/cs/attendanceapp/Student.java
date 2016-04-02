package odu.cs.attendanceapp;

/**
 * Created by yingbozheng on 10/26/2015.
 */
public class Student
{
    private String studentName;
    private String studentID;
    private String studentEmail;
    private String studentPhone;

    public Student()
    {}

    public Student (String _studentName, String _studentID, String studentEmail, String studentPhone)
    {
        this.studentID = _studentID;
        this.studentName = _studentName;
        this.studentEmail = studentEmail;
        this.studentPhone = studentPhone;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public String getStudentID()
    {
        return studentID;
    }

    public String getStudentEmail() {return studentEmail;}

    public String getStudentPhone() {return studentPhone;}

}
