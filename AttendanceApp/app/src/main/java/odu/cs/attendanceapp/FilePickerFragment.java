package odu.cs.attendanceapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilePickerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilePickerFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "FilePicker";

    private OnFragmentInteractionListener mListener;

    private String[] mFileList;
    private File mPath;    //;/+ "//yourdir//");
    private String mChosenFile;
    private static final String FTYPE1 = ".csv";
    private static final String FTYPE2 = ".txt";
    private static final int DIALOG_LOAD_FILE = 1000;
    EditText etFilePath;

    AbsListView mListView;

    private static final String ERR_SD_MISSING_MSG = "ERRSD cannot see your SD card. Please reinstall it and do not remove it.";
    private static final String ERR_SD_UNREADABLE_MSG = "CITY cannot read your SD (memory) card. This is probably because your phone is plugged into your computer. Please unplug it and try again.";

    public static String getSDCard() throws IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED))
            throw new IOException(ERR_SD_MISSING_MSG);
        else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            throw new IOException(ERR_SD_UNREADABLE_MSG);
        File sdCard = Environment.getExternalStorageDirectory(); // “/mnt/sdcard”
        if (!sdCard.exists())
            throw new IOException(ERR_SD_MISSING_MSG);
        if (!sdCard.canRead()) //for writing à sdCard.canWrite()
            throw new IOException(ERR_SD_UNREADABLE_MSG);
        return sdCard.toString();
    }


    private void loadFileList() {
        try {
            mPath.mkdirs();
        }
        catch(SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }
        if(mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return (filename.contains(FTYPE1) || filename.contains((FTYPE2))) && !sel.isDirectory();
                }

            };
            mFileList = mPath.list(filter);
        }
        else {
            mFileList= new String[0];
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mPath = new File(Environment.getExternalStorageDirectory() + "");

        try
        {
            Log.e(TAG, getSDCard());
        }
        catch(IOException e)
        {
        }



        loadFileList();

        switch(id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                if(mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        etFilePath.setText(mChosenFile);
                        //you can do stuff with the file here too
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FilePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilePickerFragment newInstance(String param1, String param2) {
        FilePickerFragment fragment = new FilePickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FilePickerFragment() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_picker, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        mPath = new File(Environment.getExternalStorageDirectory() + "");

        loadFileList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, mFileList);

        mListView.setAdapter(adapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        getDialog().setTitle("File Selection");
        mListView.requestFocus();

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

/*    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            mListener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(etFilePath, mFileList[position]);
            this.dismiss();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(View v, String filePath);
    }

}
