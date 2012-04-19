
package com.jike.mobile.analogclock;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jike.mobile.analogclock.settingwidget.JikeAnalogClockPreference;
import com.jike.mobile.analogclock.widget.HandImageView;
import com.jike.mobile.analogclock.widget.Log;

/**
 * @author Kun Wang 2012-3-23 15:30:31
 */
public class AnalogClockActivity extends Activity implements OnTouchListener, OnGestureListener {

    public static int mScreenHeight;

    /**
	 */
    private final int SEC_HAND_REFRESH_VALUE = 1000;

    /**
	 */
    private final int AUTO_LOCK_VALUE = 5 * 1000;

    // private RelativeLayout mAnalogClockRelativeLayout ;
    private HandImageView mHourHandImageView;

    private HandImageView mMinHandImageView;

    private HandImageView mSecHandImageView;

    private HandImageView mAlarmHandImageView;

    private ImageButton mAlarmButton;

    private ImageButton mStopWatchButton;

    // private Button mButton1;
    // private Button mButton2;
    private ImageButton mSettingButton;

    private ImageButton mLockButton;

    private boolean isSlideLocked = true;

    private LinearLayout mShowAlarmTimeLinearLayout;

    private TextView mTimeTextView;

    // boolean isAlarmHandShow=false;
    // boolean isButtonsShow=false;

    boolean on_off = false;

    int secAngle = 0;

    int lastSecAngle;

    int hourAngle = 0;

    int lastHourAngle;

    int minAngle = 0;

    int lastMinAngle;

    float AlarmHandAngle = 0;

    float LastAlarmHandAngle = 0;

    private View.OnTouchListener mHandImageViewTouchListener;

    // private View.OnClickListener mRelativeLayoutOnClickListener;
    private View.OnClickListener mLockButtonClickListener;

    // private OnLongClickListener mHandImageViewOnLongClickListener;

    private Alarm mAlarm;

    private int Alarm_Hour;

    private int Alarm_Min;

    private static boolean am_pm;

    private String hourTimeString;

    private String minTimeString;

    private String ampmString;

    private String showalarmtimeString;

    private static WindowManager.LayoutParams WinManParams;

    GestureDetector gestureDetector;

    private static float rateBrightness = 0.85f;

    GetRunHandTask mGetRunHandTask;

    int alarmHandPressedImg;

    /**
     *  
     */
    int mRotateCenterX;

    int mRotateCenterY;

    private Context mContext;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Debug.startMethodTracing("analogclock_jk");
        mContext = this;
        alarmHandPressedImg = R.drawable.alarm_hand_pressed_jk1;
        WinManParams = getWindow().getAttributes();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        MainActivity.mRotateCenterX = dm.widthPixels / 2;
        MainActivity.mRotateCenterY = dm.heightPixels / 2;
        mScreenHeight = dm.heightPixels;
        mRotateCenterX = MainActivity.mRotateCenterX;
        mRotateCenterY = MainActivity.mRotateCenterY;

        setContentView(R.layout.analogclock);
        gestureDetector = new GestureDetector(this);

        mAlarmHandImageView = (HandImageView)findViewById(R.id.alarm_hand);
        mSecHandImageView = (HandImageView)findViewById(R.id.sec_hand);
        mHourHandImageView = (HandImageView)findViewById(R.id.hour_hand);
        mMinHandImageView = (HandImageView)findViewById(R.id.min_hand);

        mShowAlarmTimeLinearLayout = (LinearLayout)findViewById(R.id.showtime_linearLayout);
        mTimeTextView = (TextView)findViewById(R.id.alarmtime);

        mSettingButton = (ImageButton)findViewById(R.id.setting);
        mAlarmButton = (ImageButton)findViewById(R.id.alarm_buttom);
        mLockButton = (ImageButton)findViewById(R.id.lock);
        mStopWatchButton = (ImageButton)findViewById(R.id.stopwatch_button);
        mSettingButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(AnalogClockActivity.this, SettingsPreferenceActivity.class));
            }
        });
        mAlarmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isSlideLocked) {
                    mLockButton.performClick();
                }
                if (mAlarmHandImageView.isShown()) {
                    mAlarmHandImageView.setVisibility(View.INVISIBLE);
                    mLockButton.setVisibility(View.INVISIBLE);
                    mAlarmHandImageView.setOnTouchListener(null);
                    mAlarmButton.setImageResource(R.drawable.alarm_off);
                } else {
                    mAlarmHandImageView.setVisibility(View.VISIBLE);
                    mLockButton.setVisibility(View.VISIBLE);
                    mAlarmButton.setImageResource(R.drawable.alarm_on);
                    // mAlarmHandImageView.setOnLongClickListener(mHandImageViewOnLongClickListener);
                    // mAlarmHandImageView.setOnClickListener(mRelativeLayoutOnClickListener);
                }
                mAlarm.enabled = mAlarmHandImageView.isShown();
                AlarmsMethod.setAlarm(getApplicationContext(), mAlarm);
                showAlarmTimeView();
            }
        });

        mLockButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isSlideLocked) {
                    isSlideLocked = !isSlideLocked;
                    // mLockButton.setText("set alarm");
                    mLockButton.setImageResource(R.drawable.lock_on);
                    mAlarmHandImageView.SetHandImageSrc(mContext, alarmHandPressedImg);
                    showAlarmTimeView();
                    mAlarmHandImageView.setOnTouchListener(mHandImageViewTouchListener);
                    mLockAlarmHandler.postDelayed(mLockAlarmRunnable, AUTO_LOCK_VALUE);
                } else {
                    isSlideLocked = !isSlideLocked;
                    // mLockButton.setText("locked");
                    mLockButton.setImageResource(R.drawable.lock_off);
                    mAlarmHandImageView.setOnTouchListener(null);
                    mAlarmHandImageView.SetHandImageAsDefault();
                    showAlarmTimeView();
                    mLockAlarmHandler.removeCallbacks(mLockAlarmRunnable);
                }
            }
        };

        mHandImageViewTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                // float x = event.getXPrecision()*event.getX()+event.getX();
                // float y = event.getYPrecision()*event.getY()+event.getY();
                float x = event.getX();
                float y = event.getY();
                float relativeX = x - mRotateCenterX;
                float relativeY = mRotateCenterY - y;
                // if
                // ((relativeX*relativeX+relativeY*relativeY)>mRotateCenterX*mRotateCenterX)
                // {
                // return false;
                // }
                Log.v("X + Y : " + x + " + " + y);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mAlarmHandImageView.SetHandImageSrc(mContext, alarmHandPressedImg);
                        // mAlarmHandImageView.SetHandImageSrc(mContext,
                        // R.drawable.sec_hand_jk1);
                        mLockAlarmHandler.removeCallbacks(mLockAlarmRunnable);
                        Log.e("jk start," + System.currentTimeMillis());
                        mAlarmHandImageView.count = 0;
                        count2 = 0;
                        count3 = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        count3++;
                        double Angle = Math.atan2(relativeX, relativeY);
                        AlarmHandAngle = (int)Math.toDegrees(Angle);
                        if (AlarmHandAngle >= 179.9) {
                            AlarmHandAngle = 180.0f;
                        }
                        if (AlarmHandAngle <= -179.9) {
                            AlarmHandAngle = -180.0f;
                        }

                        AlarmHandAngle = (float)(Math.round(AlarmHandAngle / 2.5f)) * 2.5f;
                        // -180
                        AlarmHandAngle = AlarmHandAngle > 0.0 ? AlarmHandAngle
                                : AlarmHandAngle + 360.0f;
                        if (AlarmHandAngle > 359.6) {
                            AlarmHandAngle = 0.0f;
                        }

                        if (isChangeAmPm(LastAlarmHandAngle, AlarmHandAngle)) {
                            am_pm = !am_pm;
                        }
                        if (Math.abs(AlarmHandAngle - LastAlarmHandAngle) > 0.5) {
                            count2++;
                            mAlarmHandImageView.RotateHanderWithAngle(AlarmHandAngle);
                            LastAlarmHandAngle = AlarmHandAngle;
                        } else {
                            break;
                        }
                        Alarm_Hour = (int)(AlarmHandAngle * 2) / 60;// +(JikeAnalogClockPreference.AlarmAtAmPm?0:12);
                        Alarm_Min = (int)(AlarmHandAngle * 2) % 60;
                        hourTimeString = Alarm_Hour < 10 ? "" + Alarm_Hour : "" + Alarm_Hour;
                        minTimeString = Alarm_Min < 10 ? "0" + Alarm_Min : "" + Alarm_Min;
                        ampmString = (am_pm ? " am" : " pm");
                        if ((am_pm == false) && hourTimeString.equals("0")) {
                            hourTimeString = "12";
                        }
                        showalarmtimeString = hourTimeString + ":" + minTimeString + ampmString;
                        showAlarmTimeTextandColor(am_pm, showalarmtimeString);
                        // ??
                        // }
                        break;
                    case MotionEvent.ACTION_UP:
                        mAlarm.hour = Alarm_Hour + (am_pm ? 0 : 12);
                        mAlarm.minutes = Alarm_Min;
                        mLockAlarmHandler.postDelayed(mLockAlarmRunnable, AUTO_LOCK_VALUE);
                        AlarmsMethod.setAlarm(mContext, mAlarm);
                        Log.e("jk stop ," + System.currentTimeMillis());
                        Log.e("jk draw count= " + mAlarmHandImageView.count);
                        Log.e("jk count2= " + count2);
                        Log.e("jk count3= " + count3);
                        break;
                    default:
                        break;
                }
                return true;
            }
        };
        mLockButton.setOnClickListener(mLockButtonClickListener);
        mStopWatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(MainActivity.mIntents[1]);
            }
        });
    }

    final Handler mRotateHandler = new Handler();

    final Runnable mRotateRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mAlarmHandImageView.RotateHanderWithAngle(AlarmHandAngle);
            LastAlarmHandAngle = AlarmHandAngle;
        }
    };

    final Handler mLockAlarmHandler = new Handler();

    final Runnable mLockAlarmRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (!isSlideLocked) {
                mLockButton.performClick();
            }
        }
    };

    final Handler mButtonHideHandler = new Handler();

    final Runnable mButtonHideRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mAlarmButton.isShown()) {
                HideAlarmAndSettingButton();
            }
        }
    };

    private void HideAlarmAndSettingButton() {
        mAlarmButton.setVisibility(View.INVISIBLE);
        mSettingButton.setVisibility(View.INVISIBLE);
        mAlarmHandImageView.setOnLongClickListener(null);
        mAlarmHandImageView.setOnTouchListener(null);
        mShowAlarmTimeLinearLayout.setVisibility(View.INVISIBLE);
        Log.v("unshow button");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        JikeAnalogClockPreference.loadStart(mContext);
        Init();
        ClockRunning = true;
        mGetRunHandTask = new GetRunHandTask();
        mGetRunHandTask.execute("");
    }

    private int count2 = 0;

    private int count3 = 0;

    public boolean ClockRunning;

    private void Init() {
        // TODO Auto-generated method stub
        JikeAnalogClockPreference.loadBaseSettingPreference(mContext);
        WinManParams.screenBrightness = JikeAnalogClockPreference.Brightness;
        getWindow().setAttributes(WinManParams);
        isSlideLocked = true;
        mAlarmHandImageView.SetHandImageAsDefault();

        mAlarm = AlarmsMethod.getAlarm(getContentResolver(), 1);
        Log.v("mAlarm.id " + mAlarm.id);
        AlarmHandAngle = (mAlarm.hour * 60 + mAlarm.minutes) * 0.5f;
        am_pm = mAlarm.hour < 12 ? true : false;
        Log.v("AlarmHandAngle" + AlarmHandAngle);

        mAlarmHandImageView.setVisibility(View.INVISIBLE);

        if (mAlarm.enabled) {
            mAlarmButton.setImageResource(R.drawable.alarm_on);
        } else {
            mAlarmButton.setImageResource(R.drawable.alarm_off);
        }

        if (mAlarm.enabled) {
            mAlarmHandImageView.setVisibility(View.VISIBLE);
        }
        showAlarmTimeView();
        mAlarmHandImageView.RotateHanderWithAngle(AlarmHandAngle);
        if (mAlarm.enabled) {
            mLockButton.setVisibility(View.VISIBLE);
            mLockButton.setImageResource(R.drawable.lock_off);
            mAlarmHandImageView.setVisibility(View.VISIBLE);
        } else {
            mLockButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * @param LastAlarmHandAngle
     * @param AlarmHandAngle
     * @return
     */
    private boolean isChangeAmPm(float LastAlarmHandAngle, float AlarmHandAngle) {
        if (LastAlarmHandAngle == AlarmHandAngle) {
            Log.i("equal angel");
            return false;
        }
        if (((LastAlarmHandAngle >= 0 && LastAlarmHandAngle < 90) && (AlarmHandAngle < 360 && AlarmHandAngle > 270))
                || ((AlarmHandAngle >= 0 && AlarmHandAngle < 90) && (LastAlarmHandAngle < 360 && LastAlarmHandAngle > 270))) {
            Log.i("changed");
            return true;
        }
        Log.i("no changed");
        return false;
    }

    /**
	 */
    private void showAlarmTimeView() {
        if (!isSlideLocked && mAlarmHandImageView.isShown()) {
            String ampm = mAlarm.hour < 12 ? " am" : " pm";
            String hour = mAlarm.hour == 12 ? "" + 12 : (mAlarm.hour % 12) + "";
            String min = mAlarm.minutes < 10 ? "0" + mAlarm.minutes : mAlarm.minutes + "";

            String showalarmtimeString = hour + ":" + min + ampm;
            showAlarmTimeTextandColor(mAlarm.hour < 12, showalarmtimeString);
            mShowAlarmTimeLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mShowAlarmTimeLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void showAlarmTimeTextandColor(Boolean am_pm, String showalarmtimeString) {
        if (am_pm) {
            mTimeTextView.setTextColor(Color.GREEN);
        } else {
            mTimeTextView.setTextColor(Color.YELLOW);
        }
        mTimeTextView.setText(" " + showalarmtimeString);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        ClockRunning = false;
        if (!isSlideLocked) {
            mLockButton.performClick();
        }
        JikeAnalogClockPreference.saveStart(mContext);
        // Debug.stopMethodTracing();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        // // if(isSlide == false || mflashlightView.isShown()){
        // // return false;
        // // }
        distanceY = distanceY > 0 ? distanceY / 2 : distanceY;
        WinManParams.screenBrightness = (float)(WinManParams.screenBrightness + rateBrightness
                * distanceY / mScreenHeight);
        if (WinManParams.screenBrightness >= 1.0)
            WinManParams.screenBrightness = 1.0f;
        else if (WinManParams.screenBrightness <= 0.01)
            WinManParams.screenBrightness = 0.01f;
        getWindow().setAttributes(WinManParams);
        // displayClock();
        JikeAnalogClockPreference.Brightness = WinManParams.screenBrightness;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        Log.e("touch");
        return gestureDetector.onTouchEvent(event);
    }

    class GetRunHandTask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            while (ClockRunning) {
                long time = System.currentTimeMillis();
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(time);
                // if (!on_off) {
                // return;
                // }
                secAngle = mCalendar.get(Calendar.SECOND) * 6;
                minAngle = mCalendar.get(Calendar.MINUTE) * 6;
                hourAngle = mCalendar.get(Calendar.HOUR) * 30 + minAngle / 12;
                // secAngle = (secAngle > 348 ? 0 : secAngle + 6);
                if (secAngle != lastSecAngle) {
                    mSecHandImageView.PostRotateHanderWithAngle(secAngle);
                    lastSecAngle = secAngle;
                    // Log.i("jike@ secAngle " + secAngle);
                }
                if (lastMinAngle != minAngle) {
                    mHourHandImageView.PostRotateHanderWithAngle(hourAngle);
                    mMinHandImageView.PostRotateHanderWithAngle(minAngle);
                    lastMinAngle = minAngle;
                }
                try {
                    Thread.sleep(SEC_HAND_REFRESH_VALUE);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return ClockRunning;
        }
    }

}
