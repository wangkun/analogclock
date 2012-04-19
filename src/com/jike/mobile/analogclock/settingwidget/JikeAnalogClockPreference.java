
package com.jike.mobile.analogclock.settingwidget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author wangkun 2012-3-7 10:19:56
 */

public class JikeAnalogClockPreference {

    private static final String PREFS_NAME_STRING = "com.jike.mobile.analogclock";

    public static int firstLauched = 2;

    private static final String FIRST_LAUCHED = "firstLauched";

    // public static boolean FIRST = true;
    // private static final String FIRST_STRING = "FIRST";

    /**
     * battery mode Never
     */
    public static boolean BatteryNeverAlert = true;

    private static final String BATTERY_NEVER_ALERT = "BatteryNeverAlert";

    /**
	 *     
	 */
    public static int VolumeVal = 80;

    private static final String VOLUME_VAL_STRING = "VolumeVal";

    /**
     * snooze
     */
    public static int SnoozeDuration = 10;

    private static final String SNOOZE_DURATION_STRING = "SnoozeDuration";

    /**
	 */
    public static int FadeInLength = 1;

    private static final String FADE_IN_LENGTH_STRING = "FadeInLength";

    /**
	 */
    public static boolean Vibrate = false;

    private static final String VIBRATE_STRING = "Vibrate";

    /**
	 */
    public static int ColorStyle = 3;

    private static final String COLOR_STYLE = "ColorStyle";

    /**
	 */
    public static boolean ShowSeconds = true;

    private static final String SHOW_SECONDS = "ShowSeconds";

    /**
	 */
    public static boolean ShowWeekday = true;

    private static final String SHOW_WEEKDAY = "ShowWeekday";

    /**
	 */
    public static boolean HourMode = true;

    private static final String HOUR_MODE = "HourMode";

    /**
	 */
    public static boolean SlideFinger = true;

    private static final String SLIDE_FINGER = "SlideFinger";

    /**
	 */
    public static boolean ShakeAble = true;

    private static final String SHAKE_ABLE = "ShakeAble";

    /**
	 */
    public static int BatteryMode = 10;

    private static final String BATTERY_MODE = "BatteryMode";

    /**
	 */
    public static int ChargeMode = 0;

    private static final String CHARGE_MODE = "ChargeMode";

    /**
	 */
    public static float Brightness = 1.0f;

    private static final String BRIGHTNESS = "Brightness";

    /**
     * / am = true; pm = flase;
     */
    public static boolean AlarmAtAmPm = true;

    private static final String ALARMATAMPM = "AlarmAtAmPm";

    /**
     * @param context
     */
    public static void loadStart(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME_STRING, 0);
        Brightness = settings.getFloat(BRIGHTNESS, 1.0f);
        firstLauched = settings.getInt(FIRST_LAUCHED, 2);

    }

    public static void saveStart(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME_STRING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(BRIGHTNESS, Brightness);
        editor.putInt(FIRST_LAUCHED, firstLauched);
        editor.commit();
    }

    /**
     * @param context
     */
    public static void loadAdvancedSettingPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME_STRING, 0);
        VolumeVal = settings.getInt(VOLUME_VAL_STRING, 80);
        SnoozeDuration = settings.getInt(SNOOZE_DURATION_STRING, 10);
        FadeInLength = settings.getInt(FADE_IN_LENGTH_STRING, 0);
        Vibrate = settings.getBoolean(VIBRATE_STRING, false);
    }

    /**
     * @param context
     */
    public static void saveAdvancedSettingPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME_STRING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(VOLUME_VAL_STRING, VolumeVal);
        editor.putInt(SNOOZE_DURATION_STRING, SnoozeDuration);
        editor.putInt(FADE_IN_LENGTH_STRING, FadeInLength);
        editor.putBoolean(VIBRATE_STRING, Vibrate);
        editor.commit();
    }

    /**
     * @param context
     */
    public static void loadBaseSettingPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME_STRING, 0);
        AlarmAtAmPm = settings.getBoolean(ALARMATAMPM, true);
        ColorStyle = settings.getInt(COLOR_STYLE, 3);
        ShowSeconds = settings.getBoolean(SHOW_SECONDS, true);
        ShowWeekday = settings.getBoolean(SHOW_WEEKDAY, true);
        HourMode = settings.getBoolean(HOUR_MODE, true);
        SlideFinger = settings.getBoolean(SLIDE_FINGER, true);
        ShakeAble = settings.getBoolean(SHAKE_ABLE, true);
        BatteryMode = settings.getInt(BATTERY_MODE, 10);
        ChargeMode = settings.getInt(CHARGE_MODE, 0);
        BatteryNeverAlert = settings.getBoolean(BATTERY_NEVER_ALERT, true);
    }

    /**
     * @param context
     */
    public static void saveBaseSettingPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME_STRING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(ALARMATAMPM, AlarmAtAmPm);
        editor.putInt(COLOR_STYLE, ColorStyle);
        editor.putBoolean(SHOW_SECONDS, ShowSeconds);
        editor.putBoolean(SHOW_WEEKDAY, ShowWeekday);
        editor.putBoolean(HOUR_MODE, HourMode);
        editor.putBoolean(SLIDE_FINGER, SlideFinger);
        editor.putBoolean(SHAKE_ABLE, ShakeAble);
        editor.putInt(BATTERY_MODE, BatteryMode);
        editor.putInt(CHARGE_MODE, ChargeMode);
        editor.putBoolean(BATTERY_NEVER_ALERT, BatteryNeverAlert);
        editor.commit();
    }

}
