
package com.jike.mobile.analogclock;


import com.jike.mobile.analogclock.widget.Log;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.database.Cursor;

public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     *                     
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Log.LOGV) Log.v("AlarmInitReceiver" + action);

        if (context.getContentResolver() == null) {
            Log.e("AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
//       2011-8-19 18:07:56
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)||action.equals(Intent.ACTION_TIME_CHANGED)||action.equals(Intent.ACTION_TIMEZONE_CHANGED)||action.equals(Intent.ACTION_LOCALE_CHANGED)) {
//                AlarmsMethod.saveSnoozeAlert(context, -1, -1);
        	InitAllAlarms(context);
            AlarmsMethod.disableExpiredAlarms(context);
        }
    }

	private void InitAllAlarms(Context context) {
		// TODO Auto-generated method stub
		long now = System.currentTimeMillis();
        Cursor cursor = AlarmsMethod.getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Alarm a = new Alarm(cursor);
//                    // A time of 0 indicates this is a repeating alarm, so
//                    // calculate the time to get the next alert.
                    AlarmsMethod.turnSnoozeOnOrOff(context, a, false);
                    if (a.daysOfWeek.isRepeatSet()) {
                        a.time = AlarmsMethod.calculateAlarm(a);
                        AlarmsMethod.enableAlert(context, a, a.time);
                    } else if (a.time < now) {
//                        // Expired alarm, disable it and move along.
                    	AlarmsMethod.enableAlarmInternal(context, a, false);
                        continue;
                    }else {
                    	AlarmsMethod.enableAlert(context, a, a.time);
					}
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
	}
}
