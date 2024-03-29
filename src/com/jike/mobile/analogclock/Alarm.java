
package com.jike.mobile.analogclock;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import com.jike.mobile.analogclock.settingwidget.JikeAnalogClockPreference;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * 
 *
 */

public final class Alarm implements Parcelable {
    // Public fields
    public int id;

    public boolean enabled;

    public int hour;

    public int minutes;

    public DaysOfWeek daysOfWeek;

    public long time;

    public boolean vibrate;

    public String label;

    public Uri alert;

    public boolean silent;

    // add 2010-9-2 11:51:18
    // edit2010-9-7 11:20:32
    public int fadeInLength;

    public int snoozeInterval;

    // ad2010-9-6 16:41:24
    public int volume;

    // add 2010-9-19 10:07:37
    public int soundId;

    // add 2010 9 30 14:12:11
    public boolean isOnSnooze;

    // add2010-10-31 16:53:31
    /**
     * music
     */
    public int soundType;

    public Uri musicUri;

    public String title;

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    // ////////////////////////////
    // Parcelable apis
    // ////////////////////////////
    public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
        public Alarm createFromParcel(Parcel p) {
            return new Alarm(p);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    public void writeToParcel(Parcel p, int flags) {
        // TODO Auto-generated method stub
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(time);
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeInt(fadeInLength);// fix bugs add by wk @ 2011-8-19 18:13:31
        p.writeInt(snoozeInterval);

        p.writeInt(volume);
        p.writeInt(soundId);

        p.writeInt(isOnSnooze ? 1 : 0);
        p.writeInt(soundType);
        p.writeParcelable(musicUri, flags);
        p.writeString(title);
        p.writeParcelable(alert, flags);
        p.writeInt(silent ? 1 : 0);
    }

    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri
                .parse("content://com.jike.mobile.analogclock/alarm");

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String MINUTES = "minutes";

        /**
         * Days of week coded as integer
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * Alarm time in UTC milliseconds from the epoch.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String ALARM_TIME = "alarmtime";

        /**
         * True if alarm is active
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String ENABLED = "enabled";

        /**
         * True if alarm should vibrate
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String VIBRATE = "vibrate";

        /**
         * Message to show when alarm triggers Note: not currently used
         * <P>
         * Type: STRING
         * </P>
         */
        public static final String MESSAGE = "message";

        /**
         * fadeInLength time
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String FADE_IN_LENGTH = "fadeInLength";

        /**
         * snoozeInterval time
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String SNOOZE_INTERVAL = "snoozeInterval";

        /**
         * volume
         */
        public static final String VOLUME = "volume";

        /**
         * soundId
         */
        public static final String SOUND_ID = "soundId";

        /**
         * isOnSnooze
         */
        public static final String IS_ON_SNOOZE = "isOnSnooze";

        /**
         * music
         */
        public static final String SOUND_TYPE = "soundType";

        public static final String MUSIC_URI = "musicUri";

        public static final String TITLE = "title";

        /**
         * Audio alert to play when alarm triggers
         * <P>
         * Type: STRING
         * </P>
         */
        public static final String ALERT = "alert";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = HOUR + ", " + MINUTES + " ASC";

        // Used when filtering enabled alarms.
        public static final String WHERE_ENABLED = ENABLED + "=1";

        // Used when filtering OnSnooze alarms.
        public static final String WHERE_SNOOZED = IS_ON_SNOOZE + "=1";

        static final String[] ALARM_QUERY_COLUMNS = {
                _ID, HOUR, MINUTES, DAYS_OF_WEEK, ALARM_TIME, ENABLED, VIBRATE, MESSAGE,
                FADE_IN_LENGTH, SNOOZE_INTERVAL, VOLUME, SOUND_ID, IS_ON_SNOOZE, SOUND_TYPE,
                MUSIC_URI, TITLE, ALERT
        };

        /**
         * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT
         * IN SYNC WITH ABOVE QUERY COLUMNS
         */
        public static final int ALARM_ID_INDEX = 0;

        public static final int ALARM_HOUR_INDEX = 1;

        public static final int ALARM_MINUTES_INDEX = 2;

        public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;

        public static final int ALARM_TIME_INDEX = 4;

        public static final int ALARM_ENABLED_INDEX = 5;

        public static final int ALARM_VIBRATE_INDEX = 6;

        public static final int ALARM_MESSAGE_INDEX = 7;

        public static final int ALARM_FADE_IN_LENGTH_INDEX = 8;

        public static final int ALARM_SNOOZE_INTERVAL_INDEX = 9;

        public static final int ALARM_VOLUME_INDEX = 10;

        public static final int ALARM_SOUND_ID_INDEX = 11;

        public static final int ALARM_IS_ON_SNOOZE_INDEX = 12;

        public static final int ALARM_SOUND_TYPE_INDEX = 13;

        public static final int ALARM_MUSIC_URI_INDEX = 14;

        public static final int ALARM_TITLE_INDEX = 15;

        public static final int ALARM_ALERT_INDEX = 16;
    }

    // ////////////////////////////
    // End column definitions
    // ////////////////////////////

    public Alarm(Cursor c) {
        id = c.getInt(Columns.ALARM_ID_INDEX);
        enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.ALARM_HOUR_INDEX);
        minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
        daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
        time = c.getLong(Columns.ALARM_TIME_INDEX);
        vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;

        fadeInLength = c.getInt(Columns.ALARM_FADE_IN_LENGTH_INDEX);
        snoozeInterval = c.getInt(Columns.ALARM_SNOOZE_INTERVAL_INDEX);

        volume = c.getInt(Columns.ALARM_VOLUME_INDEX);
        soundId = c.getInt(Columns.ALARM_SOUND_ID_INDEX);
        isOnSnooze = c.getInt(Columns.ALARM_IS_ON_SNOOZE_INDEX) == 1;
        label = c.getString(Columns.ALARM_MESSAGE_INDEX);
        soundType = c.getInt(Columns.ALARM_SOUND_TYPE_INDEX);
        String musicString = c.getString(Columns.ALARM_MUSIC_URI_INDEX);
        if (musicString != null && musicString.length() != 0) {
            musicUri = Uri.parse(musicString);
        }

        // If the database alert is null or it failed to parse, use the
        // default alert.
        if (musicUri == null) {
            musicUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        title = c.getString(Columns.ALARM_TITLE_INDEX);
        String alertString = c.getString(Columns.ALARM_ALERT_INDEX);

        if (AlarmsMethod.ALARM_ALERT_SILENT.equals(alertString)) {
            Log.d("jike", "Alarm is marked as silent");
            silent = true;
        } else {
            if (alertString != null && alertString.length() != 0) {
                alert = Uri.parse(alertString);
            }

            // If the database alert is null or it failed to parse, use the
            // default alert.
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
        }
    }

    public Alarm(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        vibrate = p.readInt() == 1;
        label = p.readString();

        fadeInLength = p.readInt();
        snoozeInterval = p.readInt();
        volume = p.readInt();
        soundId = p.readInt();
        isOnSnooze = p.readInt() == 1;
        soundType = p.readInt();
        musicUri = (Uri)p.readParcelable(null);
        title = p.readString();
        alert = (Uri)p.readParcelable(null);
        silent = p.readInt() == 1;

    }

    // Creates a default alarm at the current time.
    public Alarm(Context context) {
        JikeAnalogClockPreference.loadAdvancedSettingPreference(context);
        id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);

        daysOfWeek = new DaysOfWeek(0x7f);
        musicUri = alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        // 2010-9-8 16:28:00
        vibrate = JikeAnalogClockPreference.Vibrate;
        snoozeInterval = JikeAnalogClockPreference.SnoozeDuration;
        fadeInLength = JikeAnalogClockPreference.FadeInLength;
        volume = JikeAnalogClockPreference.VolumeVal;
        // Ĭ
        soundId = 2;
        isOnSnooze = false;
        soundType = 1;
        title = "Jike";
    }

    public String getLabelOrDefault(Context context) {
        if (label == null || label.length() == 0) {
            return context.getString(R.string.default_label);
        }
        return label;
    }

    /*
     * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
     * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
     * Sunday
     */
    public static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
                Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,
        };

        // Bitmask of all repeating days
        private int mDays;

        public DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
                return showNever ? context.getText(R.string.never_repeat).toString() : "";
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1)
                    dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ? dfs.getShortWeekdays() : dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0)
                        ret.append(context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        /**
         * returns number of days from today until next alarm
         * 
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
    }

}
