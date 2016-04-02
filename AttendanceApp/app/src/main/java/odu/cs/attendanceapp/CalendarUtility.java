package odu.cs.attendanceapp;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by yingbozheng on 11/25/2015.
 */
public class CalendarUtility {
        public static ArrayList<String> nameOfEvent = new ArrayList<String>();
        public static ArrayList<String> startDates = new ArrayList<String>();
        public static ArrayList<String> endDates = new ArrayList<String>();
        public static ArrayList<String> descriptions = new ArrayList<String>();

        public static Activity activity;

        public static ArrayList<String> readCalendarEvent(Context context) {
            Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                    .buildUpon();

            //Start time for the event
            ContentUris.appendId(eventsUriBuilder, Calendar.getInstance().getTimeInMillis());

            //End time for the event
            ContentUris.appendId(eventsUriBuilder, Calendar.getInstance().getTimeInMillis() + 1000 * 60 * 60);

            //Build the event
            Uri eventsUri = eventsUriBuilder.build();

            Cursor cursor = context.getContentResolver()
                    .query(
                            eventsUri,
                            new String[] { "calendar_id", "title", "description",
                                    "dtstart", "dtend", "eventLocation" }, null,
                            null, null);

            cursor.moveToFirst();
            // fetching calendars name
            String CNames[] = new String[cursor.getCount()];

            // fetching calendars id
            nameOfEvent.clear();
            startDates.clear();
            endDates.clear();
            descriptions.clear();
            for (int i = 0; i < CNames.length; i++) {

                nameOfEvent.add(cursor.getString(1));
                startDates.add(getDate(Long.parseLong(cursor.getString(3))));
//                endDates.add(getDate(Long.parseLong(cursor.getString(4))));
                descriptions.add(cursor.getString(2));
                //CNames[i] = cursor.getString(1);
                cursor.moveToNext();

            }
            return nameOfEvent;
        }

        public static String getDate(long milliSeconds) {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "dd/MM/yyyy hh:mm:ss a");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliSeconds);
            return formatter.format(calendar.getTime());
        }

    private void ReadCurrentEvent(Activity calActivity)
    {
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, Calendar.getInstance().getTimeInMillis());
        ContentUris.appendId(eventsUriBuilder, Calendar.getInstance().getTimeInMillis() + 1000 * 60 * 60);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = null;

/*        Time t = new Time();

        String dtStart = Long.toString(t.toMillis(false));

        String dtEnd = Long.toString(t.toMillis(false));*/

        String[] columns = new String[]{};

        cursor = calActivity.getContentResolver().query(eventsUri, columns, null, null, CalendarContract.Instances.DTSTART + " ASC");
    }



    public static long pushAppointmentsToCalender(Activity curActivity, String title, String addInfo, String place, int status, long startDate, long endDate, boolean needReminder, boolean needMailService) {
        /***************** Event: note(without alert) *******************/

        String eventUriString = "content://com.android.calendar/events";
        ContentValues eventValues = new ContentValues();

        eventValues.put("calendar_id", 1); // id, We need to choose from
        // our mobile for primary
        // its 1
        eventValues.put("title", title);
        eventValues.put("description", addInfo);
        eventValues.put("eventLocation", place);

        //long endDate = startDate + 1000 * 60 * 60; // For next 1hr

        //Log.d("Insert Start Date", new SimpleDateFormat("yyyy-MM-dd HH:MM").format(startDate));
        //Log.d("Insert End Date", new SimpleDateFormat("yyyy-MM-dd HH:MM").format(endDate));

        eventValues.put("dtstart", startDate);
        eventValues.put("dtend", endDate);
        eventValues.put("allDay", 0);

        // values.put("allDay", 1); //If it is bithday alarm or such
        // kind (which should remind me for whole day) 0 for false, 1
        // for true
        eventValues.put("eventStatus", status); // This information is
        // sufficient for most
        // entries tentative (0),
        // confirmed (1) or canceled
        // (2):

   /*Comment below visibility and transparency  column to avoid java.lang.IllegalArgumentException column visibility is invalid error */

    /*eventValues.put("visibility", 3); // visibility to default (0),
                                        // confidential (1), private
                                        // (2), or public (3):
    eventValues.put("transparency", 0); // You can control whether
                                        // an event consumes time
                                        // opaque (0) or transparent
                                        // (1).
      */
        eventValues.put("hasAlarm", 1); // 0 for false, 1 for true

        eventValues.put("eventTimezone", TimeZone.getDefault().getID());

        Uri eventUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(eventUriString), eventValues);
        long eventID = Long.parseLong(eventUri.getLastPathSegment());

        if (needReminder) {
            /***************** Event: Reminder(with alert) Adding reminder to event *******************/

            String reminderUriString = "content://com.android.calendar/reminders";

            ContentValues reminderValues = new ContentValues();

            reminderValues.put("event_id", eventID);
            reminderValues.put("minutes", 5); // Default value of the
            // system. Minutes is a
            // integer
            reminderValues.put("method", 1); // Alert Methods: Default(0),
            // Alert(1), Email(2),
            // SMS(3)

            Uri reminderUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
        }

        /***************** Event: Meeting(without alert) Adding Attendies to the meeting *******************/

        if (needMailService) {
            String attendeuesesUriString = "content://com.android.calendar/attendees";

            /********
             * To add multiple attendees need to insert ContentValues multiple
             * times
             ***********/
            ContentValues attendeesValues = new ContentValues();

            attendeesValues.put("event_id", eventID);
            attendeesValues.put("attendeeName", "xxxxx"); // Attendees name
            attendeesValues.put("attendeeEmail", "yyyy@gmail.com");// Attendee
            // E
            // mail
            // id
            attendeesValues.put("attendeeRelationship", 0); // Relationship_Attendee(1),
            // Relationship_None(0),
            // Organizer(2),
            // Performer(3),
            // Speaker(4)
            attendeesValues.put("attendeeType", 0); // None(0), Optional(1),
            // Required(2), Resource(3)
            attendeesValues.put("attendeeStatus", 0); // NOne(0), Accepted(1),
            // Decline(2),
            // Invited(3),
            // Tentative(4)

            Uri attendeuesesUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(attendeuesesUriString), attendeesValues);
        }

        return eventID;

    }

    private void EditCalendar()
    {
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", cal.getTimeInMillis());
        intent.putExtra("allDay", true);
        intent.putExtra("rrule", "FREQ=YEARLY");
        intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
        intent.putExtra("title", "A Test Event from android app");
        //startActivity(intent);
    }

    private void LaunchCalendar()
    {
        Calendar today = Calendar.getInstance();

        Uri uriCalendar = Uri.parse("content://com.android.calendar/time/" + String.valueOf(today.getTimeInMillis()));
        Intent intentCalendar = new Intent(Intent.ACTION_VIEW,uriCalendar);

        //Use the native calendar app to view the date
        //startActivity(intentCalendar);
    }
}
