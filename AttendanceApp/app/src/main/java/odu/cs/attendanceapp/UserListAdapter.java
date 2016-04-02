package odu.cs.attendanceapp;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Query;


public class UserListAdapter extends FirebaseListAdapter<User>
{
    public UserListAdapter(Query ref, Activity activity, int layout)
    {
        super(ref, User.class, layout, activity);
    }

    @Override
    protected void populateView(View view, User user)
    {
        String username = user.getName();
        TextView tvUsername = (TextView) view.findViewById(R.id.tvUserName);
        tvUsername.setText(username);

        String email = user.getEmail();
        TextView tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvEmail.setText(email);
    }
}
