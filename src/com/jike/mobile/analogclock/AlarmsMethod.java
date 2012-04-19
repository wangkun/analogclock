
package com.jike.mobile.analogclock;

import java.util.Calendar;

import com.jike.mobile.analogclock.settingwidget.ToastMaster;
import com.jike.mobile.analogclock.widget.Log;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.Toast;

/**
 */
public class AlarmsMethod {

    /**
     * ACTION
     */
    public static final String ALARM_ALERT_ACTION = "com.jike.mobile.analogclock.ALARM_ALERT";

    public static final String SNOOZE_ALARM_ALERT_ACTION = "com.jike.mobile.analogclock.SNOOZE_ALARM_ALERT";

    public static final String ALARM_DONE_ACTION = "com.jike.mobile.analogclock.ALARM_DONE";

    public static final String ALARM_SNOOZE_ACTION = "com.jike.mobile.analogclock.ALARM_SNOOZE";

    public static final String ALARM_DISMISS_ACTION = "com.jike.mobile.analogclock.ALARM_DISMISS";

    public static final String ALARM_KILLED = "com.jike.mobile.analogclock.alarm_killed";

    public static final String ALARM_KILLED_TIMEOUT = "com.jike.mobile.analogclock.alarm_killed_timeout";

    public static final String ALARM_ALERT_SILENT = "com.jike.mobile.analogclock.silent";

    public static final String CANCEL_SNOOZE = "com.jike.mobile.analogclock.cancel_snooze";

    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    public static final String ALARM_ID = "alarm_id";

    final static String PREF_SNOOZE_ID = "snooze_id";

    final static String PREF_SNOOZE_TIME = "snooze_time";

    private final static String DM12 = "E h:mm aa";

    private final static String DM24 = "E k:mm";

    final static String M12 = "h:mm aa";

    // Shared with DigitalClock
    public final static String M24 = "kk:mm";

    /**
     * @param Alarm
     */
    public static long addAlarm(Context context, Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        Uri uri = context.getContentResolver().insert(Alarm.Columns.CONTENT_URI, values);
        alarm.id = (int)ContentUris.parseId(uri);
        long timeInMillis = calculateAlarm(alarm);
        if (alarm.enabled) {
            // clearSnoozeIfNeeded(context, timeInMillis);
            enableAlert(context, alarm, timeInMillis);
        } else {
            disableAlert(context, alarm.id);
        }
        // һ
        // setNextAlert(context);
        return timeInMillis;
    }

    /**
     * @Param alarmId
     */
    public static void deleteAlarm(Context context, int alarmId) {
        if (alarmId == -1) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        /* If alarm is snoozing, lose it */
        disableSnoozeAlert(context, alarmId);

        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        contentResolver.delete(uri, "", null);

        // setNextAlert(context);
        disableAlert(context, alarmId);
    }

    /**
     * @return cursor over all alarms
     */
    public static Cursor getAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, Alarm.Columns.DEFAULT_SORT_ORDER);
    }

    /**
     * @param contentResolver
     */
    public static Cursor getFilteredAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS,
                Alarm.Columns.WHERE_ENABLED, null, null);
    }

    /**
     * @param alarm
     * @return
     */
    private static ContentValues createContentValues(Alarm alarm) {
        // ContentValues values = new ContentValues(8);
        ContentValues values = new ContentValues(15);
        //
        long time = 0;
        // if (!alarm.daysOfWeek.isRepeatSet()) {
        time = calculateAlarm(alarm);
        // }

        values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
        values.put(Alarm.Columns.HOUR, alarm.hour);
        values.put(Alarm.Columns.MINUTES, alarm.minutes);
        values.put(Alarm.Columns.ALARM_TIME, time);// alarm.time
        values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
        // values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
        values.put(Alarm.Columns.MESSAGE, alarm.label);

        values.put(Alarm.Columns.SNOOZE_INTERVAL, alarm.snoozeInterval);
        values.put(Alarm.Columns.FADE_IN_LENGTH, alarm.fadeInLength);
        values.put(Alarm.Columns.VOLUME, alarm.volume);
        values.put(Alarm.Columns.VIBRATE, alarm.vibrate ? 1 : 0);
        values.put(Alarm.Columns.SOUND_ID, alarm.soundId);
        values.put(Alarm.Columns.SOUND_TYPE, alarm.soundType);
        values.put(Alarm.Columns.MUSIC_URI, alarm.musicUri == null ? ALARM_ALERT_SILENT
                : alarm.musicUri.toString());
        values.put(Alarm.Columns.TITLE, alarm.title);
        // 2010-9-7 10:19:45
        // A null alert Uri indicates a silent alarm.
        values.put(Alarm.Columns.ALERT,
                alarm.alert == null ? ALARM_ALERT_SILENT : alarm.alert.toString());

        return values;
    }

    public static void updateAlarmTime(Context context, Alarm alarm) {
        // ContentValues values = createContentValues(alarm);
        ContentValues values = createContentValuesSimple(alarm);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
    }

    /**
     * @param alarm
     * @return
     */
    private static ContentValues createContentValuesSimple(Alarm alarm) {
        // ContentValues values = new ContentValues(8);
        ContentValues values = new ContentValues(10);
        // Set the alarm_time value if this alarm does not repeat. This will be
        // used later to disable expire alarms.
        //
        long time = 0;
        if (!alarm.daysOfWeek.isRepeatSet()) {
            time = calculateAlarm(alarm);
        }
        time = alarm.time;

        values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
        values.put(Alarm.Columns.HOUR, alarm.hour);
        values.put(Alarm.Columns.MINUTES, alarm.minutes);
        values.put(Alarm.Columns.ALARM_TIME, time);// alarm.time
        values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
        // values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
        values.put(Alarm.Columns.MESSAGE, alarm.label);

        // values.put(Alarm.Columns.SNOOZE_INTERVAL, alarm.snoozeInterval);
        // values.put(Alarm.Columns.FADE_IN_LENGTH, alarm.fadeInLength);

        // 2010-9-7 10:19:45
        // A null alert Uri indicates a silent alarm.
        values.put(Alarm.Columns.SOUND_ID, alarm.soundId);
        values.put(Alarm.Columns.SOUND_TYPE, alarm.soundType);
        values.put(Alarm.Columns.MUSIC_URI, alarm.musicUri == null ? ALARM_ALERT_SILENT
                : alarm.musicUri.toString());
        values.put(Alarm.Columns.TITLE, alarm.title);
        values.put(Alarm.Columns.ALERT,
                alarm.alert == null ? ALARM_ALERT_SILENT : alarm.alert.toString());

        return values;
    }

    // private static void clearSnoozeIfNeeded(Context context, long alarmTime)
    // {
    // // If this alarm fires before the next snooze, clear the snooze to
    // // enable this alarm.
    // SharedPreferences prefs =
    // context.getSharedPreferences(AlarmClock.PREFERENCES, 0);
    // long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
    // // alarmTime֮ ӵ snooze ô snooze á
    // if (alarmTime < snoozeTime) {
    // clearSnoozePreference(context, prefs);
    // }
    // }

    /**
     * @param ID
     * @return Alarm
     */
    public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
        if (alarmId == -1) {
            return null;
        }
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
                Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }

    /**
 *            
 * 
 */
    public static void updateAlarm(Context context, Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        // ContentValues values = createContentValuesSimple(alarm);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
    }

    /**
     * @param Alarm
     */
    public static long setAlarm(Context context, Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        // ContentValues values = createContentValuesSimple(alarm);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
        alarm = getAlarm(context.getContentResolver(), alarm.id);
        long timeInMillis = calculateAlarm(alarm);
        disableSnoozeAlert(context, alarm.id);
        if (alarm.enabled) {
            // Disable the snooze if we just changed the snoozed alarm. This
            // only does work if the snoozed alarm is the same as the given
            // alarm.
            // TODO: disableSnoozeAlert should have a better name.
            // disableSnoozeAlert(context, alarm.id);

            enableAlert(context, alarm, timeInMillis);

            // Disable the snooze if this alarm fires before the snoozed alarm.
            // This works on every alarm since the user most likely intends to
            // have the modified alarm fire next.
            // clearSnoozeIfNeeded(context, timeInMillis);
        } else {
            disableAlert(context, alarm.id);
        }

        // setNextAlert(context);
        setStatusBarIcon(context);

        return timeInMillis;
    }

    /**
     * @param id
     * @param enabled
     */

    public static void enableAlarm(final Context context, final int id, boolean enabled) {
        enableAlarmInternal(context, getAlarm(context.getContentResolver(), id), enabled);
        // enableAlarmInternal(context, id, enabled);
        // setNextAlert(context);
    }

    // private static void enableAlarmInternal(final Context context,
    // final int id, boolean enabled) {
    // enableAlarmInternal(context, getAlarm(context.getContentResolver(), id),
    // enabled);
    // }
    /**
     * @param Alarm
     * @param enabled
     */
    public static void enableAlarmInternal(final Context context, final Alarm alarm, boolean enabled) {
        if (alarm == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

        // AlarmInitReceiver
        if (enabled) {
            long time = 0;
            // 2010-9-3 14:07:00
            time = calculateAlarm(alarm);
            if (!alarm.daysOfWeek.isRepeatSet()) {
                values.put(Alarm.Columns.ALARM_TIME, time);
            } else {
                values.put(Alarm.Columns.ALARM_TIME, 0);
            }
            android.util.Log.d("enable alarm", "" + time + " " + alarm.hour);

            values.put(Alarm.Columns.ALARM_TIME, time);
            enableAlert(context, alarm, time);
        } else {
            // Clear the snooze if the id matches.
            // disableSnoozeAlert(context, alarm.id);
            disableAlert(context, alarm.id);
        }
        disableSnoozeAlert(context, alarm.id);
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
        setStatusBarIcon(context);

    }

    // public static Alarm calculateNextAlert(final Context context) {
    // Alarm alarm = null;
    // long minTime = Long.MAX_VALUE;
    // long now = System.currentTimeMillis();
    // Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
    // if (cursor != null) {
    // if (cursor.moveToFirst()) {
    // do {
    // Alarm a = new Alarm(cursor);
    // // A time of 0 indicates this is a repeating alarm, so
    // // calculate the time to get the next alert.
    // if (a.time == 0) {
    // a.time = calculateAlarm(a);
    // } else if (a.time < now) {
    // // Expired alarm, disable it and move along.
    // enableAlarmInternal(context, a, false);
    // continue;
    // }
    // if (a.time < minTime) {
    // minTime = a.time;
    // alarm = a;
    // }
    // } while (cursor.moveToNext());
    // }
    // cursor.close();
    // }
    // return alarm;
    // }

    /**
	     */
    public static void disableExpiredAlarms(final Context context) {
        Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
        long now = System.currentTimeMillis();

        if (cur.moveToFirst()) {
            do {
                Alarm alarm = new Alarm(cur);
                // A time of 0 means this alarm repeats. If the time is
                // non-zero, check if the time is before now.
                if (alarm.daysOfWeek.isRepeatSet() == false && alarm.time < now) {
                    if (Log.LOGV) {
                        Log.v("** DISABLE " + alarm.id + " now " + now + " set " + alarm.time);
                    }
                    enableAlarmInternal(context, alarm, false);
                }
            } while (cur.moveToNext());
        }
        cur.close();
        setStatusBarIcon(context);
    }

    // public static void setNextAlert(final Context context,final Alarm alarm)
    // {
    // // snooze
    // // if (!enableSnoozeAlert(context)) {
    // // Alarm alarm = calculateNextAlert(context);
    // if (alarm != null) {
    // enableAlert(context, alarm, alarm.time);
    // } else {
    // disableAlert(context,alarm.id);
    // }
    // // }
    // }
    /**
     * Snooze
     * 
     * @param Alarm
     * @param atTimeInMillis
     */
    public static void enableSnoozeAlert(Context context, final Alarm alarm,
            final long atTimeInMillis) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (Log.LOGV) {
            Log.v("** setAlert id " + alarm.id + " atTime " + atTimeInMillis);
        }

        Intent intent = new Intent(SNOOZE_ALARM_ALERT_ACTION);

        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());
        // alarm.id
        PendingIntent sender = PendingIntent.getBroadcast(context, alarm.id, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        android.util.Log.d("enableSnoozeAlert -alarm.id", (-alarm.id) + "enableSnoozeAlert");

        am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

        setStatusBarIcon(context);// , true
        updateAlarmTime(context, alarm, atTimeInMillis);
        // Calendar c = Calendar.getInstance();
        // c.setTimeInMillis(atTimeInMillis);
        // String timeString = formatDayAndTime(context, c);
        // saveNextAlarm(context, timeString);
        turnSnoozeOnOrOff(context, alarm, true);
    }

    /**
     * SnoozeOnOrOff
     * 
     * @param alarm
     * @param isOn
     */
    public static void turnSnoozeOnOrOff(Context context, Alarm alarm, boolean isOn) {
        ContentValues values = new ContentValues(1);
        values.put(Alarm.Columns.IS_ON_SNOOZE, isOn ? 1 : 0);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
        //
    }

    /**
     * @param alarm Alarm.
     * @param atTimeInMillis milliseconds since epoch
     */
    public static void enableAlert(Context context, final Alarm alarm, final long atTimeInMillis) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        // atTimeInMillis+=(int)Math.random()*100;

        if (Log.LOGV) {
            Log.v("** setAlert id " + alarm.id + " atTime " + atTimeInMillis);
        }
        alarm.time = atTimeInMillis;
        Log.v("enableAlert  " + alarm.soundId);
        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the Alarm class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the Alarm
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());
        // ID 2010-9-1 11:07:55
        // PendingIntent sender = PendingIntent.getBroadcast(
        // context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarm.id, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        android.util.Log.d("enable +alarm.id", alarm.id + "enable  ++++");

        am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

        // setStatusBarIcon(context, true);
        setStatusBarIcon(context);
        updateAlarmTime(context, alarm, atTimeInMillis);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(atTimeInMillis);
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
        if (alarm.id == 2) {
            Log.v("cancel Toast");
            return;
        }
        popAlarmSetToast(context, atTimeInMillis);

    }

    /**
     * ALARM_TIME
     * 
     * @param alarm
     * @param atTimeInMillis
     */
    public static void updateAlarmTime(Context context, final Alarm alarm, final long atTimeInMillis) {
        ContentValues values = new ContentValues(1);
        values.put(Alarm.Columns.ALARM_TIME, atTimeInMillis);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
        //
    }

    /**
     * findLatestAlert snooze
     * 
     * @param context
     * @return Alarm
     */
    public static Alarm findLatestAlert(final Context context) {
        Alarm alarm = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Alarm a = new Alarm(cursor);
                    // // A time of 0 indicates this is a repeating alarm, so
                    // // calculate the time to get the next alert.
                    // if (a.time == 0) {
                    // a.time = calculateAlarm(a);
                    // } else if (a.time < now) {
                    // // Expired alarm, disable it and move along.
                    // enableAlarmInternal(context, a, false);
                    // continue;
                    // }
                    if (a.time < minTime && a.time > now) {
                        minTime = a.time;
                        alarm = a;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return alarm;
    }

    /**
     * snooze
     * 
     * @param ID
     */
    public static void disableSnoozeAlert(Context context, final int AlarmId) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, AlarmId, new Intent(
                SNOOZE_ALARM_ALERT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        android.util.Log.d("DISABLE Snooze -alarm.id", (-AlarmId) + "Snooze DISABLE----");

        am.cancel(sender);
        // setStatusBarIcon(context, false);
        // 2010-9-9 15:55:49
        NotificationManager nm = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(AlarmId);

        setStatusBarIcon(context);

        Alarm alarm = getAlarm(context.getContentResolver(), AlarmId);
        turnSnoozeOnOrOff(context, alarm, false);
        // saveNextAlarm(context, "");
    }

    /**
     * Disables alert in AlarmManger and StatusBar.
     * 
     * @param id Alarm ID.
     */
    // static void disableAlert_old(Context context) {
    // AlarmManager am = (AlarmManager)
    // context.getSystemService(Context.ALARM_SERVICE);
    // PendingIntent sender = PendingIntent.getBroadcast(
    // context, 0, new Intent(ALARM_ALERT_ACTION),
    // PendingIntent.FLAG_CANCEL_CURRENT);
    // am.cancel(sender);
    // // setStatusBarIcon(context, false);
    // setStatusBarIcon(context);
    // saveNextAlarm(context, "");
    // }
    /**
     * @param context
     * @param ID
     */
    static void disableAlert(Context context, int ID) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, ID, new Intent(
                ALARM_ALERT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        android.util.Log.d("disableAlert ID", "disableAlert" + ID);
        am.cancel(sender);
        // setStatusBarIcon(context, false);
        setStatusBarIcon(context);
        saveNextAlarm(context, "");
    }

    /**
	     */
    private static void setStatusBarIcon(Context context) {

        Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
        alarmChanged.putExtra("alarmSet", isHaveEnableAlarm(context));
        context.sendBroadcast(alarmChanged);

    }

    /**
	     */
    public static boolean isHaveEnableAlarm(Context context) {
        Cursor cursor = AlarmsMethod.getFilteredAlarmsCursor(context.getContentResolver());
        boolean isHave;
        if (cursor.moveToFirst()) {
            isHave = true;
        } else {
            isHave = false;
        }
        cursor.close();
        return isHave;
    }

    /**
	     */
    public static boolean isHaveSnoozeAlarm(Context context) {
        Cursor cursor = (context.getContentResolver()).query(Alarm.Columns.CONTENT_URI,
                Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_SNOOZED, null, null);
        boolean isHave;
        if (cursor.moveToFirst()) {
            isHave = true;
        } else {
            isHave = false;
        }
        cursor.close();
        return isHave;
    }

    /**
     * @param alarm
     * @return long
     */
    public static long calculateAlarm(Alarm alarm) {
        return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis();
    }

    /**
     * @param hour
     * @param minute
     * @param daysOfWeek
     * @return Calendar
     */
    static Calendar calculateAlarm(int hour, int minute, Alarm.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0)
            c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    /**
     * @param context
     * @param hour
     * @param minute
     * @param daysOfWeek
     * @return
     */
    static String formatTime(final Context context, int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    /**
	 */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
	     */
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
	     */
    static void saveNextAlarm(final Context context, String timeString) {
        // Settings.System.putString(context.getContentResolver(),
        // Settings.System.NEXT_ALARM_FORMATTED,
        // timeString);
    }

    /**
     * @return true
     */
    public static boolean get24HourMode(final Context context) {
        // return android.text.format.DateFormat.is24HourFormat(context);
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                SettingsPreferenceActivity.KEY_HOUR_MODE, true);
    }

    /**
	     */
    static void popAlarmSetToast(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek) {
        popAlarmSetToast(context, AlarmsMethod.calculateAlarm(hour, minute, daysOfWeek)
                .getTimeInMillis());
    }

    private static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        ToastMaster.setToast(toast);
        toast.show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from now"
     */
    static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" : (days == 1) ? context.getString(R.string.day) : context
                .getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context.getString(R.string.minute)
                : context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" : (hours == 1) ? context.getString(R.string.hour)
                : context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0) | (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    /**
     * @return sound sound resID
     */
    public static int getSoundRsId(int i) {
        int RsId = 0;
        switch (i + 1) {
            case 1:
                RsId = R.raw.ascending;
                break;
            case 2:
                RsId = R.raw.birds;
                break;
            case 3:
                RsId = R.raw.classic;
                break;
            case 4:
                RsId = R.raw.cuckoo;
                break;
            case 5:
                RsId = R.raw.digital;
                break;
            case 6:
                RsId = R.raw.electronic;
                break;
            case 7:
                RsId = R.raw.high_tone;
                break;
            case 8:
                RsId = R.raw.mbira;
                break;
            case 9:
                RsId = R.raw.old_clock;
                break;
            case 10:
                RsId = R.raw.rooster;
                break;
            case 11:
                RsId = R.raw.school_bell;
                break;
            default:
                break;
        }
        return RsId;
    }

}
