package odu.cs.attendanceapp;

/**
 * Created by Dani on 11/27/2015.
 */
public class Teacher
{
    private String teacherName;
    private String teacherEmail;

    public Teacher()
    {
    }

    public Teacher (String teacherName, String teacherEmail)
    {
        this.teacherName = teacherName;
        this.teacherEmail = teacherEmail;
    }

    public String getTeacherName()
    {
        return teacherName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }
}
