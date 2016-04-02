package odu.cs.attendanceapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Dani on 11/20/2015.
 *
 * Database of all the beacons that are linked to the courses that the student (user) is attending
 */
public class BeaconDBAdapter {

    static final String KEY_ROWID = "_id";
    static final String KEY_BEACONID = "beaconID";
    static final String KEY_CRN = "crn";
    static final String TAG = "BeaconDBAdapter";

    static final String DATABASE_NAME = "BeaconsDB";
    static final String DATABASE_TABLE = "beacons";
    static final int DATABASE_VERSION = 2;

    static final String DATABASE_CREATE =
            "create table beacons (_id integer primary key autoincrement, "
                    + "beaconID text not null, crn text not null);";

    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public BeaconDBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    //---opens the database---
    public BeaconDBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }

    //---insert an entry into the database---
    public long insertEntry(String beaconID, String crn)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BEACONID, beaconID);
        initialValues.put(KEY_CRN, crn);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //---deletes a particular entry---
    public boolean deleteEntry(long rowId)
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //--deletes all entries---
    public boolean deleteAllEntries(){
        return db.delete(DATABASE_TABLE, null, null) > 0;
    }

    //---retrieves all the entries---
    public Cursor getAllEntries()
    {
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_BEACONID}, null, null, null, null, null);
    }

    //---retrieves a particular entry---
    public Cursor getEntry(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_BEACONID, KEY_CRN},
                        KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //true if the beacon with beaconID is already in the database
    public boolean alreadyInDB(String beaconID){
        boolean found = false;
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[]{KEY_ROWID, KEY_BEACONID}, KEY_BEACONID + "=?",
                new String[]{beaconID}, null, null, null, null);

        if(mCursor.moveToFirst()) found = true;

        return found;
    }

    //true if the beacon with beaconID is already in the database
    public String getCRN(String beaconID){
        String crn = "NOTHING";
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[]{KEY_ROWID, KEY_BEACONID, KEY_CRN}, KEY_BEACONID + "=?",
                new String[]{beaconID}, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        crn = mCursor.getString(mCursor.getColumnIndex(KEY_CRN));
        return crn;
    }

    //---updates an entry---
    public boolean updateEntry(long rowId, String beaconID, String crn)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_BEACONID, beaconID);
        args.put(KEY_CRN, crn);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

}
