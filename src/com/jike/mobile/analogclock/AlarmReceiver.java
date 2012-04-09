package com.jike.mobile.analogclock;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
//import android.database.Cursor;
import android.os.Parcel;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jike.mobile.analogclock.widget.Log;



/**
*/
public class AlarmReceiver extends BroadcastReceiver {

   

    @Override
    public void onReceive(Context context, Intent intent) {
        

        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(AlarmsMethod.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }
        
        if (alarm == null) {
            Log.v("AlarmReceiver failed to parse the alarm from the intent");
            return;
        }
        Log.v("AlarmReceiver  "+alarm.soundId+"");
//        2010-9-3 14:31:52 ޸ ȴлȡӵ
        if (AlarmsMethod.ALARM_KILLED.equals(intent.getAction())) {
            // The alarm has been killed, update the notification
            updateNotification(context, (Alarm)
                    intent.getParcelableExtra(AlarmsMethod.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(AlarmsMethod.ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (AlarmsMethod.CANCEL_SNOOZE.equals(intent.getAction())) {
//            AlarmsMethod.saveSnoozeAlert(context, -1, -1);
        	Log.v("AlarmsMethod.CANCEL_SNOOZE");
        	AlarmsMethod.disableSnoozeAlert(context, alarm.id);
            return;
        }
//        
        AlarmsMethod.turnSnoozeOnOrOff(context, alarm, false);
        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        
        SimpleDateFormat format =
                new SimpleDateFormat("HH:mm:ss.SSS aaa");
        Log.v("AlarmReceiver.onReceive() id " + alarm.id + " setFor "
                + format.format(new Date(alarm.time)));
//        /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
//        is probably the result of a time or timezone change */
        long now = System.currentTimeMillis();
    	int STALE_WINDOW = 60 * 30;
        if (now > alarm.time + STALE_WINDOW * 1000) {
            if (Log.LOGV) {
                Log.v("AlarmReceiver ignoring stale alarm");
            }
            return;
        }

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Decide which activity to start based on the state of the keyguard.
        Class<?> c = AlarmAlert.class;
        KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            // Use the full screen activity for security.
            c = AlarmAlertFullScreen.class;
        }

        /* launch UI, explicitly stating that this is not due to user action
         * so that the current app's notification management is not disturbed */
        Intent alarmAlert = new Intent(context, c);
        alarmAlert.putExtra(AlarmsMethod.ALARM_INTENT_EXTRA, alarm);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(alarmAlert);

        // Disable the snooze alert if this alarm is the snooze.
        
//      ȥsnoozeĲ  
//        ??AlarmsMethod.disableSnoozeAlert(context, alarm.id);
        // Disable this alarm if it does not repeat.
        if (!alarm.daysOfWeek.isRepeatSet()) {
            AlarmsMethod.enableAlarm(context, alarm.id, false);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
//            AlarmsMethod.setNextAlert(context);
        	long atTimeInMillis = AlarmsMethod.calculateAlarm(alarm);
            AlarmsMethod.enableAlert(context, alarm, atTimeInMillis);
        }

        // Play the alarm alert and vibrate the device.
        Intent playAlarm = new Intent(AlarmsMethod.ALARM_ALERT_ACTION);
        playAlarm.putExtra(AlarmsMethod.ALARM_INTENT_EXTRA, alarm);
        context.startService(playAlarm);

        // Trigger a notification that, when clicked, will show the alarm alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(AlarmsMethod.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                alarm.id, notify, 0);

        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_notify_text),
                pendingNotify);
        n.flags |= Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_ONGOING_EVENT;
        n.defaults |= Notification.DEFAULT_LIGHTS;

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = getNotificationManager(context);
        nm.notify(alarm.id, n);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(Context context, Alarm alarm, int timeout) {
        NotificationManager nm = getNotificationManager(context);

        // If the alarm is null, just cancel the notification.
        if (alarm == null) {
            if (Log.LOGV) {
                Log.v("Cannot update notification for killer callback");
            }
            return;
        }

        // Launch SetAlarm when clicked.
        //2012-3-7 17:13:12
//       wangkun ????
//        Intent viewAlarm = new Intent(context, SetAlarm.class);
//        viewAlarm.putExtra(AlarmsMethod.ALARM_ID, alarm.id);
//        PendingIntent intent =
//                PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);

        // Update the notification to indicate that the alert has been
        // silenced.
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
//        n.setLatestEventInfo(context, label,
//                context.getString(R.string.alarm_alert_alert_silenced, timeout),
//                intent);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        // We have to cancel the original notification since it is in the
        // ongoing section and we want the "killed" notification to be a plain
        // notification.
        nm.cancel(alarm.id);
        nm.notify(alarm.id, n);
    }
}
