package odu.cs.attendanceapp;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

/**
 * A fragment representing a list of Items.
 * <p>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ScheduleFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Firebase mFirebaseRef;
    private Firebase mFirebaseScheduleRef;

    private ScheduleAdapter mFirebaseAdapter;

    String mCourseAbbrev;
    String mCRN;
    public String mStudentEmail;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    private Button btnCheckIn;

    private static String mBeaconID;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        mBeaconID = param1;

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScheduleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Setup our Firebase mFirebaseRef
        Firebase.setAndroidContext(getActivity());

    }

    public void UpdateListview(final String sBeaconID)
    {
        if(sBeaconID != null)
        {
            mBeaconID = sBeaconID;
            mFirebaseRef = new Firebase(getString(R.string.firebase_url));

            mFirebaseScheduleRef = mFirebaseRef.child("schedule").child(sBeaconID).child("schedules");
            mFirebaseAdapter = new ScheduleAdapter(mFirebaseScheduleRef.limit(50), this.getActivity(), R.layout.schedule_row) {};

            mListView.setAdapter(mFirebaseAdapter);
            mFirebaseAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    int position = FirebaseUtility.getIndexByBeaconId(mFirebaseAdapter.getItems(), sBeaconID);

                    if (position>0) {
                        mListView.setSelection(position+1);

                        mCourseAbbrev = ((Schedule) mFirebaseAdapter.getItem(position)).getAbbreviation();
                    }
                }
            });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mFirebaseAdapter);


        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        btnCheckIn = (Button) view.findViewById(R.id.btnSave);
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUtility.AddAttendance(mFirebaseRef, mBeaconID, mCourseAbbrev, mStudentEmail, mCRN);
                Toast.makeText(getActivity(), "Attendance added to " + mCourseAbbrev, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mCourseAbbrev = ((Schedule) mFirebaseAdapter.getItem(position)).getAbbreviation();
            mCRN = ((Schedule) mFirebaseAdapter.getItem(position)).getCrn();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
