
package com.jike.mobile.analogclock;

import com.jike.mobile.analogclock.settingwidget.AlertSoundPreference;
import com.jike.mobile.analogclock.settingwidget.JikeAnalogClockPreference;
import com.jike.mobile.analogclock.settingwidget.JikeVolumePreference;
import com.jike.mobile.analogclock.settingwidget.RepeatPreference;
import com.jike.mobile.analogclock.widget.Log;
import com.mobclick.android.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;

public class SettingsPreferenceActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";

    static final String KEY_COLOR_STYLE = "color_style";

    static final String KEY_SHOW_SECONDS = "show_secends";

    static final String KEY_SHOW_WEEKDAY = "show_weekday";

    static final String KEY_HOUR_MODE = "hour_mode";

    static final String KEY_SLIDE_FINGER = "slide_finger";

    static final String KEY_SHAKE = "shake";

    static final String KEY_BATTERY_MODE = "battery_mode";

    static final String KEY_CHARGE_MODE = "charge_mode";

    private static Context mContext;

    // private static CheckBoxPreference mAmPmPref;
    private static RepeatPreference mRepeatPref;

    private static AlertSoundPreference mAlertSoundPref;

    private static CheckBoxPreference mVibratePref;

    private static ListPreference mFadeInLengthListPref;

    private static ListPreference mSnoozeDurationListPref;

    private static JikeVolumePreference mJikeVolumePref;

    private static PreferenceCategory mPreferenceCategory;

    private static ListPreference soundTypeListPreference;

    private static Preference mSongPreference;

    private static int soundType;

    private static Uri uri;

    private Cursor myCursor;

    private Boolean preferenceChange;

    private static final int SONG_CODE = 0;

    // private static int ID;
    private static Alarm mAlarm;

    private static int mVolumeVal;

    private static int mSnoozeDuration;

    private static int mFadeInLength;

    private static boolean mVibrate;
    
    private static Map<String,String> settingsProperty= new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        preferenceChange = false;
        uri = null;
        mAlertSoundPref = new AlertSoundPreference(this);
        mAlertSoundPref.setTitle(R.string.alert_sound);
        mAlertSoundPref.setDialogTitle(R.string.alert_sound);
        mAlertSoundPref.setOnPreferenceChangeListener(this);
        // Select a Song
        mSongPreference = new Preference(this);
        mSongPreference.setTitle(R.string.select_song);
        mSongPreference.setOnPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.settings);

        mPreferenceCategory = (PreferenceCategory)findPreference("sound");
        soundTypeListPreference = (ListPreference)findPreference("sound_type");
        soundTypeListPreference.setSummary(soundTypeListPreference.getEntry());
        soundTypeListPreference
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        // TODO Auto-generated method stub
                        String val = (String)newValue;
                        mPreferenceCategory.removePreference(mAlertSoundPref);
                        mPreferenceCategory.removePreference(mSongPreference);
                        int i = Integer.parseInt(val);
                        soundType = i;
                        switch (i) {
                            case 2:
                                val = "None";
                                break;
                            case 1:
                                val = getString(R.string.sound);
                                mPreferenceCategory.addPreference(mAlertSoundPref);
                                break;
                            case 0:
                                val = getString(R.string.music);
                                mPreferenceCategory.addPreference(mSongPreference);
                                uri = mAlarm.musicUri;
                                if (uri == null) {
                                    break;
                                }
                                Log.v("updatePrefs" + uri);
                                try {
                                    myCursor = getContentResolver().query(uri, null, null, null,
                                            null);
                                    myCursor.moveToFirst();
                                    String title = myCursor.getString(myCursor
                                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
                                    mSongPreference.setSummary(title);
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                break;
                            default:
                                break;
                        }
                        preference.setSummary(val);

                        return SettingsPreferenceActivity.this.onPreferenceChange(preference,
                                newValue);
                    }
                });

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        JikeAnalogClockPreference.saveBaseSettingPreference(mContext);
        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        refresh();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (preferenceChange) {
            saveAndSetAlarm();
        } else {
            savaAlarm();
        }
        JikeAnalogClockPreference.saveBaseSettingPreference(mContext);
        MobclickAgent.onPause(this);
        MobclickAgent.onEvent(this, "setting_alarm",settingsProperty);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        preferenceChange = true;
        if (preference == mSongPreference) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(intent, SONG_CODE);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SONG_CODE) {
            if (data != null)
                if (resultCode == RESULT_CANCELED) {
                    Log.v("data.getDataString**********  " + data.getDataString());

                } else {
                    String title = null;
                    uri = Uri.parse(data.getDataString());
                    try {
                        myCursor = getContentResolver().query(uri, null, null, null, null);
                        myCursor.moveToFirst();
                        title = myCursor.getString(myCursor
                                .getColumnIndex(MediaStore.Audio.Media.TITLE));
                        mSongPreference.setSummary(title);
                        savaAlarm();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    Log.v("data.getDataString*****" + data.getDataString());
                    Log.v("Song Title**** " + title);
                }
        }
    }

    private void refresh() {
        // TODO Auto-generated method stub
        JikeAnalogClockPreference.loadBaseSettingPreference(mContext);
        // Cursor mCursor = AlarmsMethod.getAlarmsCursor(getContentResolver());
        // mCursor.moveToFirst();
        // mAlarm = new Alarm(mCursor);
        // Log.e("malarm ID" + mAlarm.id);
        // mCursor.close();
        mAlarm = AlarmsMethod.getAlarm(getContentResolver(), 1);
        mVolumeVal = mAlarm.volume;
        mSnoozeDuration = mAlarm.snoozeInterval;
        mFadeInLength = mAlarm.fadeInLength;
        mVibrate = mAlarm.vibrate;

        mAlertSoundPref.setSoundPrefSummary(mAlarm.soundId);
        soundType = mAlarm.soundType;
        soundTypeListPreference.setValueIndex(soundType);

        // mAmPmPref= (CheckBoxPreference)findPreference("alarm_ampm");
        // mAmPmPref.setChecked(mAlarm.hour>12?false:true);
        // mAmPmPref.setOnPreferenceChangeListener(new
        // OnPreferenceChangeListener() {
        // @Override
        // public boolean onPreferenceChange(Preference preference, Object
        // newValue) {
        // // TODO Auto-generated method stub
        // JikeAnalogClockPreference.AlarmAtAmPm = (Boolean)newValue;
        // if ((Boolean)newValue) {
        // mAlarm.hour=mAlarm.hour>=12?mAlarm.hour-12:mAlarm.hour;
        // }else {
        // mAlarm.hour=mAlarm.hour>12?mAlarm.hour:mAlarm.hour+12;
        // }
        // saveAndSetAlarm();
        // return true;
        // }
        // });

        mRepeatPref = (RepeatPreference)findPreference("setRepeat");
        mRepeatPref.setOnPreferenceChangeListener(this);
        Log.e("mAlarm.daysOfWeek " + mAlarm.daysOfWeek.toString());
        mRepeatPref.setDaysOfWeek(mAlarm.daysOfWeek);

        // mAlertSoundPref = (AlertSoundPreference)
        // findPreference("alertSounds");
        // mAlertSoundPref.setOnPreferenceChangeListener(this);
        // mAlertSoundPref.setSoundPrefSummary(mAlarm.soundId);

        String summString = null;
        mPreferenceCategory.removePreference(mAlertSoundPref);
        mPreferenceCategory.removePreference(mSongPreference);
        switch (mAlarm.soundType) {
            case 2:
                summString = "None";
                break;
            case 1:
                summString = getString(R.string.sound);
                mPreferenceCategory.addPreference(mAlertSoundPref);
                break;
            case 0:
                summString = getString(R.string.music);
                mPreferenceCategory.addPreference(mSongPreference);
                String title = null;
                uri = mAlarm.musicUri;
                Log.v("updatePrefs" + uri);
                try {
                    myCursor = getContentResolver().query(uri, null, null, null, null);
                    myCursor.moveToFirst();
                    title = myCursor.getString(myCursor
                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
                    mSongPreference.setSummary(title);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                break;
            default:
                break;
        }
        soundTypeListPreference.setSummary(summString);

        mVibratePref = (CheckBoxPreference)findPreference("vibrate");
        mVibratePref.setChecked(mVibrate);
        mVibratePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference p, Object newValue) {
                mVibrate = (Boolean)newValue;
                return true;
            }
        });

        mSnoozeDurationListPref = (ListPreference)findPreference("snooze_duration");
        mSnoozeDurationListPref.setSummary(mAlarm.snoozeInterval == 0 ? getString(R.string.never)
                : mAlarm.snoozeInterval + getString(R.string.min));
        mSnoozeDurationListPref.setValue(Integer.toString(mAlarm.snoozeInterval));// Integer.toString(mAlarm.snoozeInterval)
        mSnoozeDurationListPref
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference p, Object newValue) {
                        String val = (String)newValue;
                        mSnoozeDuration = Integer.parseInt(val);
                        val = val.equals("0") ? getString(R.string.never) : val
                                + getString(R.string.min);
                        p.setSummary(val);
                        if (val != null && !val.equals(p.getSummary())) {
                            // Call through to the generic listener.
                            // return SetAlarm.this
                            // .onPreferenceChange(p, newValue);
                        }
                        return true;
                    }
                });

        mFadeInLengthListPref = (ListPreference)findPreference("fade_in_length");
        mFadeInLengthListPref.setSummary(mAlarm.fadeInLength == 0 ? getString(R.string.never)
                : mAlarm.fadeInLength + getString(R.string.min));
        mFadeInLengthListPref.setValue(Integer.toString(mAlarm.fadeInLength));
        mFadeInLengthListPref
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference p, Object newValue) {
                        String val = (String)newValue;
                        mFadeInLength = Integer.parseInt(val);
                        val = val.equals("0") ? getString(R.string.never) : val
                                + getString(R.string.min);
                        p.setSummary(val);
                        if (val != null && !val.equals(p.getSummary())) {
                            // Call through to the generic listener.
                            // return SetAlarm.this
                            // .onPreferenceChange(p, newValue);
                        }
                        return true;
                    }
                });

        mJikeVolumePref = (JikeVolumePreference)findPreference("alarm_volume");
        mJikeVolumePref.setVolume(mAlarm.volume);
        // mDialogVolumePref.setSummary()
        mJikeVolumePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference p, Object newValue) {
                // String val = (String) newValue;
                mVolumeVal = (Integer)newValue;
                Log.v("val " + (Integer)newValue);
                // Set the summary based on the new label.
                p.setSummary(mVolumeVal + "%");
                // if (val != null && !val.equals(mLabel.getText())) {
                // Call through to the generic listener.
                // return SetAlarm.this.onPreferenceChange(p,
                // newValue);
                // }
                return true;
            }
        });

    }

    private void saveAndSetAlarm() {
        // TODO Auto-generated method stub
        // ContentValues values = createAdvancedContentValues(mAlarm);
        // ContentResolver resolver = this.getContentResolver();
        // resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI,
        // mAlarm.id), values, null, null);
        // Cursor mCursor = AlarmsMethod.getAlarmsCursor(getContentResolver());
        // mCursor.moveToFirst();
        // mAlarm = new Alarm(mCursor);
        // Log.e("malarm ID" + mAlarm.id);
        // mCursor.close();
        savaAlarm();
        AlarmsMethod.setAlarm(mContext, mAlarm);
    }

    private static void savaAlarm() {
        mAlarm.daysOfWeek = mRepeatPref.getDaysOfWeek();
        mAlarm.soundId = mAlertSoundPref.getSoundId();
        mAlarm.soundType = soundType;

        mAlarm.musicUri = uri;
        Log.v("uri =" + uri);
        mAlarm.vibrate = mVibrate;
        mAlarm.snoozeInterval = mSnoozeDuration;
        mAlarm.fadeInLength = mFadeInLength;
        mAlarm.volume = mVolumeVal;
        AlarmsMethod.updateAlarm(mContext, mAlarm);
        settingsProperty.put("daysOfWeek", mAlarm.daysOfWeek.getCoded()+"");
        settingsProperty.put("soundType",mAlarm.soundType+"");
        settingsProperty.put("soundId",mAlarm.soundId+"");
        settingsProperty.put("mVibrate",mAlarm.vibrate+"");
        settingsProperty.put("snoozeInterval",mAlarm.snoozeInterval+"");
        settingsProperty.put("fadeInLength",mAlarm.fadeInLength+"");
        settingsProperty.put("volume",mAlarm.volume+"");
    }

    // private static ContentValues createAdvancedContentValues(Alarm alarm) {
    // ContentValues values = new ContentValues(4);
    //
    // // values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
    // // values.put(Alarm.Columns.HOUR, alarm.hour);
    // // values.put(Alarm.Columns.MINUTES, alarm.minutes);
    // // values.put(Alarm.Columns.ALARM_TIME, alarm.time);
    // Log.e("mRepeatPref "+ mRepeatPref.getDaysOfWeek().getCoded());
    // values.put(Alarm.Columns.DAYS_OF_WEEK,
    // mRepeatPref.getDaysOfWeek().getCoded());
    // values.put(Alarm.Columns.SOUND_ID, mAlertSoundPref.getSoundId());
    // // values.put(Alarm.Columns.MESSAGE, alarm.label);
    // values.put(Alarm.Columns.VIBRATE, mVibrate);
    // values.put(Alarm.Columns.SNOOZE_INTERVAL, mSnoozeDuration);
    // values.put(Alarm.Columns.FADE_IN_LENGTH, mFadeInLength);
    // values.put(Alarm.Columns.VOLUME, mVolumeVal);
    // return values;
    // }
    
}
