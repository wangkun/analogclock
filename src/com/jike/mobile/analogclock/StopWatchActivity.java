
package com.jike.mobile.analogclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jike.mobile.analogclock.settingwidget.JikeAnalogClockPreference;
import com.jike.mobile.analogclock.widget.HandImageView;
import com.jike.mobile.analogclock.widget.Log;

/**
 * StopWatch and CountDown Timer
 * 
 * @author wangkun 2012-3-30 13:50:20
 */
public class StopWatchActivity extends Activity {

    public static int mRotateCenterX;

    public static int mRotateCenterY;

    static int timer_stopwatch;

    final int STOPWATCH = 1;

    final int TIMER = 2;

    /**
     * keep screen on
     */
    RelativeLayout mAllRelativeLayout;

    ImageButton mStartStopButton;

    ImageButton mResetButton;

    ImageButton Timer_StopwatchButton;

    TextView mAlarmTimeTextView;

    TextView mTitleTextView;

    HandImageView mSecHandImageView;

    HandImageView mMinHandImageView;

    // HandImageView mHourHandImageView;

    long startTime;

    long countTime;

    int countSec;

    int countMs;

    int countMin;

    // int countHour;

    String countMsString;

    String countSecString;

    String countMinString;

    // String countHourString;
    Calendar mCalendar;

    String mAlarmTimeString;

    int secAngle;

    int lastSecAngle = -1;

    int minAngle;

    int lastMinAngle = -1;

    boolean isRunning;

    boolean isZeroTime;

    final long AnimationFrequency = 10L;

    int SecAnimationLevel = 1;

    int MinAnimationLevel = 1;

    final int AnimationLevelRate = 4;

    Alarm mTimerAlarm;

    Context mContext;

    Resources res;

    private OnTouchListener mMinHandTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("StopWatchActivity onCreate");
        setContentView(R.layout.stopwatch);
        mContext = this;
        res = mContext.getResources();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        MainActivity.mRotateCenterX = dm.widthPixels / 2;
        MainActivity.mRotateCenterY = dm.heightPixels / 2;
        mRotateCenterX = MainActivity.mRotateCenterX;
        mRotateCenterY = MainActivity.mRotateCenterY;

        mTimerAlarm = AlarmsMethod.getAlarm(getContentResolver(), 2);
        Log.v("mTimerAlarm " + mTimerAlarm.label);

        timer_stopwatch = STOPWATCH;
        isRunning = false;
        isZeroTime = true;
        mCalendar = Calendar.getInstance();

        mAllRelativeLayout = (RelativeLayout)findViewById(R.id.stopwatch_relativelayout);
        mAllRelativeLayout.setKeepScreenOn(true);

        mAlarmTimeTextView = (TextView)findViewById(R.id.stopwatch_time_text);
        mTitleTextView = (TextView)findViewById(R.id.stopwatch_title);
        mStartStopButton = (ImageButton)findViewById(R.id.start_stop_button);
        mResetButton = (ImageButton)findViewById(R.id.reset_button);
        mSecHandImageView = (HandImageView)findViewById(R.id.sec_hand_view);
        mMinHandImageView = (HandImageView)findViewById(R.id.min_hand_view);
        Timer_StopwatchButton = (ImageButton)findViewById(R.id.timer_stopwatch_button);
        // mHourHandImageView =
        // (HandImageView)findViewById(R.id.hour_hand_view);

        mSecHandImageView.setVisibility(View.VISIBLE);

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mResetAnimationHandler.removeCallbacks(mResetAnimationRunnable);
                if (timer_stopwatch == TIMER) {
                    if (countMin == 0 && countMs == 0) {
                        return;
                    }
                    if (isRunning) {
                        isRunning = !isRunning;
                        mSecHandReverseRunHandler.removeCallbacks(mSecHandReverseRunnable);
                        mStartStopButton.setImageResource(R.drawable.start_btn);
                        mResetButton.setClickable(true);

                        mTimerAlarm.time = 0L;
                        mTimerAlarm.enabled = false;
                        AlarmsMethod.updateAlarmTime(mContext, mTimerAlarm);
                        AlarmsMethod.disableAlert(mContext, mTimerAlarm.id);
                    } else {
                        isRunning = !isRunning;
                        startTime = isZeroTime ? System.currentTimeMillis() + countMin * 60 * 1000
                                : System.currentTimeMillis() + countMin * 60 * 1000 + countSec
                                        * 1000 + countMs;

                        mTimerAlarm.time = startTime;
                        AlarmsMethod.updateAlarmTime(mContext, mTimerAlarm);

                        AlarmsMethod.enableAlert(mContext, mTimerAlarm, startTime);
                        if (isZeroTime) {
                            isZeroTime = false;
                        }
                        mStartStopButton.setImageResource(R.drawable.stop_btn);
                        mSecHandReverseRunHandler.post(mSecHandReverseRunnable);
                        mResetButton.setClickable(false);
                    }
                    return;
                }
                if (isRunning) {
                    isRunning = !isRunning;
                    mSecHandRunHandler.removeCallbacks(mSecHandRunnable);
                    mStartStopButton.setImageResource(R.drawable.start_btn);
                    mResetButton.setClickable(true);
                } else {
                    isRunning = !isRunning;
                    startTime = isZeroTime ? System.currentTimeMillis() : System
                            .currentTimeMillis() - countTime;
                    if (isZeroTime) {
                        isZeroTime = false;
                        minAngle = 0;
                        secAngle = 0;
                    }
                    mSecHandRunHandler.post(mSecHandRunnable);
                    mStartStopButton.setImageResource(R.drawable.stop_btn);
                    mResetButton.setClickable(false);
                }
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isRunning) {
                    return;
                }
                startTime = System.currentTimeMillis() + 1000L;
                countTime = 0L;
                isZeroTime = true;
                isRunning = false;
                // mSecHandRunHandler.post(mSecHandRunnable);
                SecAnimationLevel = getAnimationLevel(secAngle);
                MinAnimationLevel = getAnimationLevel(minAngle);
                updateTimerText(0, 0, 0);
                mResetAnimationHandler.post(mResetAnimationRunnable);
            }
        });
        Timer_StopwatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v("Timer_StopwatchButton.setOnClickListener");
                if (isRunning) {
                    return;
                }
                Log.v("timer_stopwatch" + timer_stopwatch);
                switch (timer_stopwatch) {
                    case TIMER:
                        timer_stopwatch = STOPWATCH;
                        Timer_StopwatchButton.setImageResource(R.drawable.stopwatch_menu_icon);
                        mMinHandImageView.setOnTouchListener(mMinHandTouchListener);
                        mTitleTextView.setText(getString(R.string.stopwatch_title));
                        break;

                    case STOPWATCH:
                        timer_stopwatch = TIMER;
                        Timer_StopwatchButton.setImageResource(R.drawable.timer_menu_icon);
                        mMinHandImageView.setOnTouchListener(null);
                        mTitleTextView.setText(getString(R.string.timer_title));
                        break;

                    default:
                        break;
                }
                // updateTimerText(0, 0, 0);
                // resetHand();
                init();
            }
        });

        mMinHandTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (isRunning) {
                    return false;
                }
                float x = event.getX();
                float y = event.getY();
                float relativeX = x - mRotateCenterX;
                float relativeY = mRotateCenterY - y;
                // if
                // ((relativeX*relativeX+relativeY*relativeY)>mRotateCenterX*mRotateCenterX)
                // {
                // return false;
                // }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        double Angle = Math.atan2(relativeX, relativeY);
                        minAngle = (int)Math.toDegrees(Angle);
                        if (minAngle > 175) {
                            minAngle = 180;
                        }
                        if (minAngle < -175) {
                            minAngle = -180;
                        }
                        minAngle = (minAngle / 6) * 6;
                        // Ƕ Ϊ-180
                        minAngle = minAngle > 0 ? minAngle : minAngle + 360;
                        if (minAngle == 360) {
                            minAngle = 0;
                        }
                        if (lastMinAngle != minAngle) {
                            mMinHandImageView.RotateHanderWithAngle(minAngle);
                            lastMinAngle = minAngle;
                        }
                        countMin = minAngle / 6;
                        updateTimerText(countMin, 0, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }
                return true;
            }
        };
        Log.v("onCreat over");

    }

    private void init() {
        // TODO Auto-generated method stub
        switch (timer_stopwatch) {
            case TIMER:
                Timer_StopwatchButton.setImageResource(R.drawable.stopwatch_menu_icon);
                mMinHandImageView.setOnTouchListener(mMinHandTouchListener);
                mTitleTextView.setText(R.string.timer_title);
                break;
            case STOPWATCH:
                Timer_StopwatchButton.setImageResource(R.drawable.timer_menu_icon);
                mTitleTextView.setText(R.string.stopwatch_title);
                mMinHandImageView.setOnTouchListener(null);
                break;
            default:
                break;
        }
        isZeroTime = true;
        isRunning = false;
        updateTimerText(0, 0, 0);
        resetHand();
        AlarmsMethod.disableAlert(mContext, mTimerAlarm.id);
        // mResetAnimationHandler.post(mResetAnimationRunnable);
    }

    /**
     * Sec Hand running handler
     */
    final Handler mSecHandRunHandler = new Handler();

    final Runnable mSecHandRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            countTime = System.currentTimeMillis() - startTime;
            if (countTime < 0L)
                countTime = 0L;
            mCalendar.setTimeInMillis(countTime);
            // countMs = mCalendar.get(Calendar.MILLISECOND);
            // countSec = mCalendar.get(Calendar.SECOND);
            // countMin = mCalendar.get(Calendar.MINUTE);
            // countHour = mCalendar.get(Calendar.HOUR_OF_DAY)-8;

            updateTimerText(mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND),
                    mCalendar.get(Calendar.MILLISECOND));

            secAngle = (int)(6 * (countSec + (((float)countMs / 100)) / 10f));
            // SecAngel = (float) (Math.round(SecAngel * 10))/10;
            secAngle = secAngle > 0 ? secAngle : secAngle + 360;
            if (lastSecAngle != secAngle) {
                mSecHandImageView.RotateHanderWithAngle(secAngle);
                lastSecAngle = secAngle;
                Log.v("jike " + secAngle);
                minAngle = countMin * 6;
                if (lastMinAngle != minAngle) {
                    // mHourHandImageView.PostRotateHanderWithAngle(hourAngle);
                    mMinHandImageView.PostRotateHanderWithAngle(minAngle);
                    lastMinAngle = countMin;
                }
            }

            if (isRunning) {
                mSecHandRunHandler.postDelayed(mSecHandRunnable, 10L);
            }
        }
    };

    /**
     * sec hand Reverse running handler
     */
    final Handler mSecHandReverseRunHandler = new Handler();

    final Runnable mSecHandReverseRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            countTime = startTime - System.currentTimeMillis();
            if (countTime < 0L) {
                countTime = 0L;
                updateTimerText(0, 0, 0);
                resetHand();
                isRunning = false;
                mStartStopButton.setImageResource(R.drawable.start_btn);
                mResetButton.setClickable(true);
                return;
            }
            mCalendar.setTimeInMillis(countTime);
            countMs = mCalendar.get(Calendar.MILLISECOND);
            countSec = mCalendar.get(Calendar.SECOND);
            countMin = mCalendar.get(Calendar.MINUTE);
            updateTimerText(countMin, countSec, countMs);

            secAngle = (int)(6 * (countSec + (((float)countMs / 100)) / 10f));
            // SecAngel = (float) (Math.round(SecAngel * 10))/10;
            secAngle = secAngle > 0 ? secAngle : secAngle + 360;
            if (lastSecAngle != secAngle) {
                mSecHandImageView.RotateHanderWithAngle(secAngle);
                lastSecAngle = secAngle;
                Log.v("jike " + secAngle);
                minAngle = countMin * 6;
                if (lastMinAngle != minAngle) {
                    // mHourHandImageView.PostRotateHanderWithAngle(hourAngle);
                    mMinHandImageView.PostRotateHanderWithAngle(minAngle);
                    lastMinAngle = countMin;
                }
            }

            if (isRunning && countTime >= 0L) {
                mSecHandReverseRunHandler.postDelayed(mSecHandReverseRunnable, 10L);
            }
        }
    };

    /**
     * update time_text
     * 
     * @param countMin
     * @param countSec
     * @param countMs
     */
    private void updateTimerText(int countMin, int countSec, int countMs) {
        this.countMin = countMin;
        this.countSec = countSec;
        this.countMs = countMs;
        countMsString = countMs < 10 ? "00" + countMs : countMs < 100 ? "0" + countMs : ""
                + countMs;
        countSecString = countSec < 10 ? "0" + countSec : "" + countSec;
        countMinString = countMin < 10 ? "0" + countMin : "" + countMin;
        // countHourString = countHour<10?"0"+countHour:""+countHour;

        mAlarmTimeString = " " + countMinString + ":" + countSecString + ":" + countMsString;
        mAlarmTimeTextView.setText(mAlarmTimeString);
    }

    private int getAnimationLevel(int Angle) {
        if (Angle > 270) {
            return 4;
        }
        if (Angle > 180) {
            return 3;
        }
        if (Angle > 90) {
            return 2;
        } else {
            return 1;
        }
    }

    final Handler mResetAnimationHandler = new Handler();

    final Runnable mResetAnimationRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            if (secAngle > 0 && minAngle > 0) {
                secAngle = secAngle - SecAnimationLevel * AnimationLevelRate;
                minAngle = minAngle - MinAnimationLevel * AnimationLevelRate;
                if (secAngle < 0) {
                    secAngle = 0;
                }
                if (minAngle < 0) {
                    minAngle = 0;
                }
                mSecHandImageView.PostRotateHanderWithAngle(secAngle);
                mMinHandImageView.PostRotateHanderWithAngle(minAngle);
                mResetAnimationHandler.postDelayed(mResetAnimationRunnable, AnimationFrequency);
            } else if (secAngle > 0) {
                secAngle = secAngle - SecAnimationLevel * AnimationLevelRate;
                if (secAngle < 0) {
                    secAngle = 0;
                }
                mSecHandImageView.PostRotateHanderWithAngle(secAngle);
                mResetAnimationHandler.postDelayed(mResetAnimationRunnable, AnimationFrequency);
            } else if (minAngle > 0) {
                minAngle = minAngle - MinAnimationLevel * AnimationLevelRate;
                if (minAngle < 0) {
                    minAngle = 0;
                }
                mMinHandImageView.PostRotateHanderWithAngle(minAngle);
                mResetAnimationHandler.postDelayed(mResetAnimationRunnable, AnimationFrequency);
            } else {
                resetHand();
                return;
            }

        }
    };

    private void resetHand() {
        minAngle = 0;
        secAngle = 0;
        mSecHandImageView.PostRotateHanderWithAngle(0);
        mMinHandImageView.PostRotateHanderWithAngle(0);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        JikeAnalogClockPreference.loadStart(mContext);
        timer_stopwatch = JikeAnalogClockPreference.firstLauched;
        if (!isRunning) {
            init();
        }
        Log.v("onResume finish");
        super.onResume();
    }

    protected void dialog() {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(getString(R.string.dialog_stop_title));
        builder.setPositiveButton(getString(R.string.dialog_ok_title), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                init();
                dialog.dismiss();
                StopWatchActivity.this.onBackPressed();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel_title), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && isRunning) {
            dialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        JikeAnalogClockPreference.firstLauched = timer_stopwatch;
        JikeAnalogClockPreference.saveStart(mContext);
        super.onPause();
    }

}
