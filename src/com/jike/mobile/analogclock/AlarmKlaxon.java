
package com.jike.mobile.analogclock;

import com.jike.mobile.analogclock.widget.Log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
//import android.media.RingtoneManager;
//import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 *         
 */
public class AlarmKlaxon extends Service {
    Context mContext;

    /** Play alarm up to 10 minutes before silencing */
    private static final int ALARM_TIMEOUT_SECONDS = 10 * 60;

    //
    /**
	 *          
	 */
    // private static int AlarmTimeOut = 0;
    /**
	 *         
	 */
    private static int AlarmFadeIn;

    private static final long[] sVibratePattern = new long[] {
            500, 500
    };

    private boolean mPlaying = false;

    private Vibrator mVibrator;

    private MediaPlayer mMediaPlayer;

    private Alarm mCurrentAlarm;

    private long mStartTime;

    private TelephonyManager mTelephonyManager;

    private int mInitialCallState;

    //
    private static final int KILLER = 1000;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                    if (Log.LOGV) {
                        Log.v("*********** Alarm killer triggered ***********");
                    }
                    sendKillBroadcast((Alarm)msg.obj);
                    stopSelf();
                    break;
            }
        }
    };

    //
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            if (state != TelephonyManager.CALL_STATE_IDLE && state != mInitialCallState) {
                sendKillBroadcast(mCurrentAlarm);
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        Log.v("Alarm Klaxom OnCreat()");
        mContext = this;
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        // ʵ
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
        stop();
        //
        mTelephonyManager.listen(mPhoneStateListener, 0);
        AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Log.v("Alarm Klaxom onStartCommand");
        if (intent == null) {
            Log.v("return START_NOT_STICKY");
            stopSelf();
            return;
        }

        final Alarm alarm = intent.getParcelableExtra(AlarmsMethod.ALARM_INTENT_EXTRA);

        // AlarmTimeOut = alarm.fadeInLength;
        AlarmFadeIn = alarm.fadeInLength;
        // if (alarm == null) {
        // Log.v("AlarmKlaxon failed to parse the alarm from the intent");
        // stopSelf();
        // return ;
        // }

        if (mCurrentAlarm != null) {
            sendKillBroadcast(mCurrentAlarm);
        }
        Log.v("AlarmKlaxon " + alarm.soundId);
        play(alarm);
        mCurrentAlarm = alarm;
        // Record the initial call state here so that the new alarm has the
        // newest state.
        mInitialCallState = mTelephonyManager.getCallState();

        return;
    }

    // @Override
    // public int onStartCommand(Intent intent, int flags, int startId) {
    // Log.v("Alarm Klaxom onStartCommand");
    // if (intent == null) {
    // Log.v("return START_NOT_STICKY");
    // stopSelf();
    // return START_NOT_STICKY;
    // }
    //
    // final Alarm alarm = intent.getParcelableExtra(
    // AlarmsMethod.ALARM_INTENT_EXTRA);
    //
    // // AlarmTimeOut = alarm.fadeInLength;
    // AlarmFadeIn = alarm.fadeInLength;
    // if (alarm == null) {
    // Log.v("AlarmKlaxon failed to parse the alarm from the intent");
    // stopSelf();
    // return START_NOT_STICKY;
    // }
    //
    // if (mCurrentAlarm != null) {
    // sendKillBroadcast(mCurrentAlarm);
    // }
    // Log.v("AlarmKlaxon "+alarm.soundId);
    // play(alarm);
    // mCurrentAlarm = alarm;
    // // Record the initial call state here so that the new alarm has the
    // // newest state.
    // mInitialCallState = mTelephonyManager.getCallState();
    //
    // return START_STICKY;
    // }

    private void sendKillBroadcast(Alarm alarm) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int)Math.round(millis / 60000.0);
        Intent alarmKilled = new Intent(AlarmsMethod.ALARM_KILLED);
        alarmKilled.putExtra(AlarmsMethod.ALARM_INTENT_EXTRA, alarm);
        alarmKilled.putExtra(AlarmsMethod.ALARM_KILLED_TIMEOUT, minutes);
        sendBroadcast(alarmKilled);
    }

    // IN_CALL_VOLUME
    private static final float IN_CALL_VOLUME = 0.125f;

    private void play(Alarm alarm) {
        // stop()
        stop();

        if (Log.LOGV) {
            Log.v("AlarmKlaxon.play() " + alarm.id + " alert " + alarm.alert);
        }

        if (alarm.soundType == 0 || alarm.soundType == 1) {// !alarm.silent||
        // Uri alert = alarm.alert;
        // // Fall back on the default alarm if the database does not have an
        // // alarm stored.
        // if (alert == null) {
        // alert = RingtoneManager.getDefaultUri(
        // RingtoneManager.TYPE_ALARM);
        // if (Log.LOGV) {
        // Log.v("Using default alarm: " + alert.toString());
        // }
        // }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("Error occurred while playing audio.");
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });
            float setVolume = ((float)alarm.volume) / 100.0f;
            try {
                //
                if (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    Log.v("Using the in-call alarm");
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                    setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.in_call_alarm);
                } else {
                    // 2010-9-8 10:32:44
                    // С
                    mMediaPlayer.reset();
                    // mMediaPlayer.setDataSource(this,
                    // alert);AlarmsMethod.getSoundRsId(alarm.soundId)
                    if (alarm.soundType == 1) {
                        mMediaPlayer = MediaPlayer.create(mContext,
                                AlarmsMethod.getSoundRsId(alarm.soundId));
                    } else if (alarm.soundType == 0) {
                        mMediaPlayer = MediaPlayer.create(this, alarm.musicUri);
                        Log.v("URI " + alarm.musicUri);
                        //
                        if (mMediaPlayer == null) {
                            mMediaPlayer = MediaPlayer.create(mContext,
                                    AlarmsMethod.getSoundRsId(alarm.soundId));
                        }
                    }
                    Log.v("alarmklaxon alarm.soundId" + alarm.soundId);
                }
                startAlarm(mMediaPlayer, setVolume);
            } catch (Exception ex) {
                Log.v("Using the fallback ringtone");
                // SD Ҫtry catch
                try {
                    // Must reset the media player to clear the error state.
                    mMediaPlayer.reset();
                    setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.fallbackring);
                    startAlarm(mMediaPlayer, setVolume);
                } catch (Exception ex2) {
                    Log.e("Failed to play fallback ringtone", ex2);
                }
            }
        }

        if (alarm.vibrate) {
            mVibrator.vibrate(sVibratePattern, 0);
        } else {
            mVibrator.cancel();
        }
        // 2010-9-8 14:46:16 TImeOUT
        // if (AlarmTimeOut!=0) {
        enableKiller(alarm);
        // }
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    private void startAlarm(final MediaPlayer player, Float volume) throws java.io.IOException,
            IllegalArgumentException, IllegalStateException {
        final AudioManager mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        oriVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // С
        Log.v("maxVolume*volume =" + maxVolume + "*" + volume + "(int) (maxVolume*volume)="
                + (int)(maxVolume * volume));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(maxVolume * volume), 0);
        Log.v(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                + " audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)");
        if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.v("volume " + volume);
            player.setLooping(true);
            player.setVolume(0, 0);
            player.start();
            if (AlarmFadeIn != 0) {
                i = 0;
                player.setVolume(0, 0);
                mMediaPlayer = player;
                volumeHandler.post(volumeRunnable);
            } else {
                // fade inΪnever
                player.setVolume(1.0f, 1.0f);
            }
        }
    }

    /**
     */
    private static int oriVolume;

    // private static final MediaPlayer mMediaPlayer;
    private static Handler volumeHandler = new Handler();

    /**
     */
    private static int i;

    private Runnable volumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (i <= 60 && mMediaPlayer != null) {
                i++;
                mMediaPlayer.setVolume(0.016f * i, 0.016f * i);
                Log.v("0.016f*i =  " + 0.016f * i);
                volumeHandler.postDelayed(volumeRunnable, AlarmFadeIn * 60 * 1000 / 60);
            }
        }
    };

    private void setDataSourceFromResource(Resources resources, MediaPlayer player, int res)
            throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        }
    }

    /**
     * Vibrator
     */
    public void stop() {
        if (Log.LOGV)
            Log.v("AlarmKlaxon.stop()");
        if (mPlaying) {
            mPlaying = false;

            Intent alarmDone = new Intent(AlarmsMethod.ALARM_DONE_ACTION);
            sendBroadcast(alarmDone);

            // Stop audio playing
            if (mMediaPlayer != null) {
                ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(
                        AudioManager.STREAM_MUSIC, oriVolume, 0);
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        }
        disableKiller();
    }

    /**
     */
    private void enableKiller(Alarm alarm) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
                1000 * ALARM_TIMEOUT_SECONDS);
    }

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }

}
