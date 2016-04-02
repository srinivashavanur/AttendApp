package odu.cs.attendanceapp;

/**
 * Created by yingbozheng on 10/24/2015.
 */
public class User
{
    private String email;
    private String name;

    public User(){}

    public User (String email, String name)
    {
        this.email = email;
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public String getName()
    {
        return name;
    }
}
