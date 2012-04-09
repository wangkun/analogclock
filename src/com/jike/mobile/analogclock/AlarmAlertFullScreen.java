
package com.jike.mobile.analogclock;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
//import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import java.util.Calendar;

import com.jike.mobile.analogclock.widget.Log;



/**
 *     
 *  snooze  dismissed 
 */
public class AlarmAlertFullScreen extends Activity {

    // These defaults must match the values in res/xml/settings.xml
	/**
	 * 
	 */
//    private static final String DEFAULT_VOLUME_BEHAVIOR = "0";
    protected static final String SCREEN_OFF = "screen_off";

    protected Alarm mAlarm;
    Context mContext;
//    private int mVolumeBehavior;
    
    Handler mFadeInHandler = new Handler();

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AlarmsMethod.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(AlarmsMethod.ALARM_DISMISS_ACTION)) {
                dismiss(false);
            } else {
                Alarm alarm = intent.getParcelableExtra(AlarmsMethod.ALARM_INTENT_EXTRA);
                if (alarm != null && mAlarm.id == alarm.id) {
                    dismiss(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        mContext =this;
        mAlarm = getIntent().getParcelableExtra(AlarmsMethod.ALARM_INTENT_EXTRA);

        // Get the volume/camera button behavior setting
//        final String vol =
//                PreferenceManager.getDefaultSharedPreferences(this)
//                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
//                        DEFAULT_VOLUME_BEHAVIOR);
//        mVolumeBehavior = Integer.parseInt(vol);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    );
            // |WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        }

        updateLayout();

        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(AlarmsMethod.ALARM_KILLED);
        filter.addAction(AlarmsMethod.ALARM_SNOOZE_ACTION);
        filter.addAction(AlarmsMethod.ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void setTitle() {
        String label = mAlarm.getLabelOrDefault(this);
        TextView title = (TextView) findViewById(R.id.alertTitle);
        title.setText(label);
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflater.inflate(R.layout.alarm_alert, null));
        /* 2010-9-7 18:00:11      Ƿ snooze ò ͬ     */
        Button snooze = (Button) findViewById(R.id.snooze);
        if(mAlarm.snoozeInterval!=0){
        snooze.requestFocus();
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });}else {
        	View view = (View)findViewById(R.id.blank);
			snooze.setVisibility(View.GONE);
			view.setVisibility(View.GONE);
		}

        /* dismiss button: close notification */
        findViewById(R.id.dismiss).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(false);
                    }
                });
        /* Set the title from the passed in alarm */
        setTitle();
    }

//       ΰ     Ϣ
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
    	Log.v("onKeyDown");
		return super.onKeyDown(keyCode, event);
		
	}
    
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.v("onKeyUp");
		return super.onKeyUp(keyCode, event);
	}

	// Attempt to snooze this alert.
    private void snooze() {
        // Do not snooze if the snooze button is disabled.
        if (!findViewById(R.id.snooze).isEnabled()) {
            dismiss(false);
            return;
        }
        AlarmsMethod.turnSnoozeOnOrOff(mContext, mAlarm, true);
//        final String snooze =
//                PreferenceManager.getDefaultSharedPreferences(this)
//                .getString(SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
//        long snoozeMinutes = Integer.parseInt(snooze);
        long snoozeMinutes = mAlarm.snoozeInterval;

        final long snoozeTime = System.currentTimeMillis()
                + 1000 * 60 *(snoozeMinutes);// 
//        ??AlarmsMethod.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id,
//                snoozeTime);
//            snooze    
        AlarmsMethod.enableSnoozeAlert(AlarmAlertFullScreen.this, mAlarm, snoozeTime);
//        AlarmsMethod.enableAlert(AlarmAlertFullScreen.this, mAlarm, snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(AlarmsMethod.CANCEL_SNOOZE);
//        cancelSnooze.putExtra(AlarmsMethod.ALARM_ID, mAlarm.id);
        Parcel out = Parcel.obtain();
        mAlarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        cancelSnooze.putExtra(AlarmsMethod.ALARM_RAW_DATA, out.marshall());
//        cancelSnooze.putExtra(AlarmsMethod.ALARM_RAW_DATA, mAlarm);
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text,
                    AlarmsMethod.formatTime(this, c)), broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        // Intentionally log the snooze time for debugging.
        Log.v(displayTime);

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime,
                Toast.LENGTH_LONG).show();
        stopService(new Intent(AlarmsMethod.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    private void dismiss(boolean killed) {
        Log.i(killed ? "Alarm killed" : "Alarm dismissed by user");
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.id);
            stopService(new Intent(AlarmsMethod.ALARM_ALERT_ACTION));
        }
        finish();
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Log.LOGV) Log.v("AlarmAlert.OnNewIntent()");

        mAlarm = intent.getParcelableExtra(AlarmsMethod.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the alarm was deleted at some point, disable snooze.
        if (AlarmsMethod.getAlarm(getContentResolver(), mAlarm.id) == null) {
            Button snooze = (Button) findViewById(R.id.snooze);
            snooze.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.LOGV) Log.v("AlarmAlert.onDestroy()");
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
//        boolean up = event.getAction() == KeyEvent.ACTION_UP;
//        switch (event.getKeyCode()) {
//            // Volume keys and camera keys dismiss the alarm
//            case KeyEvent.KEYCODE_VOLUME_UP:
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//            case KeyEvent.KEYCODE_CAMERA:
//            case KeyEvent.KEYCODE_FOCUS:
//                if (up) {
//                    switch (mVolumeBehavior) {
//                        case 1:
//                            snooze();
//                            break;
//
//                        case 2:
//                            dismiss(false);
//                            break;
//
//                        default:
//                            break;
//                    }
//                }
//                return true;
//            default:
//                break;
//        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }
}
