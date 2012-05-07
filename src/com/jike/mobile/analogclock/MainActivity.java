
package com.jike.mobile.analogclock;

import com.jike.mobile.analogclock.widget.Log;
import com.mobclick.android.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    public static int mRotateCenterX;

    public static int mRotateCenterY;

    public static Intent[] mIntents;

    private Cursor mCursor;

    private Alarm mAlarm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        setContentView(R.layout.main);

        mCursor = AlarmsMethod.getAlarmsCursor(getContentResolver());
        if (!mCursor.moveToFirst()) {
            // First start,
            mAlarm = new Alarm(this);
            mAlarm.hour = 7;
            mAlarm.minutes = 30;
            AlarmsMethod.addAlarm(this, mAlarm);
            Log.v("init 1 alarm");
            Alarm mTimerAlarm = new Alarm(this);
            mTimerAlarm.hour = 0;
            mTimerAlarm.minutes = 1;
            mTimerAlarm.snoozeInterval = 0;
            mTimerAlarm.label = getString(R.string.time_over_string);
            mTimerAlarm.daysOfWeek = new Alarm.DaysOfWeek(0);
            AlarmsMethod.addAlarm(this, mTimerAlarm);
        }
        mCursor.close();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mRotateCenterX = dm.widthPixels / 2;
        mRotateCenterY = dm.heightPixels / 2;

        mIntents = new Intent[2];

        mIntents[0] = new Intent(this, AnalogClockActivity.class);
        mIntents[1] = new Intent(this, StopWatchActivity.class);

        startActivity(mIntents[0]);
        finish();
    }
}
