
package com.jike.mobile.analogclock.settingwidget;

import com.jike.mobile.analogclock.R;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

/**
 * AlertSoundPreference
 */
public class AlertSoundPreference extends ListPreference {

    private static MediaPlayer myMediaPlayer;

    private Context mContext;

    Resources res;

    // private String[] soundNameEntries =
    // res.getStringArray(R.array.sound_name_entries);
    private String[] soundNameEntries = {
            "Ascending", "Birds", "Classic", "Cuckoo", "Digital", "Electronic", "High Tone",
            "Mbira", "Old Clock", "Rooster", "School Bell",
    };

    private String[] soundEntryValues = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
    };

    private static int RSID;

    public AlertSoundPreference(Context context) {
        super(context);
        mContext = context;
        res = mContext.getResources();
        soundNameEntries = res.getStringArray(R.array.sound_name_entries);
        setEntries(soundNameEntries);
        setEntryValues(soundEntryValues);
    }

    public AlertSoundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        res = mContext.getResources();
        soundNameEntries = res.getStringArray(R.array.sound_name_entries);
        setEntries(soundNameEntries);
        setEntryValues(soundEntryValues);
    }

    @Override
    protected View onCreateDialogView() {
        // TODO Auto-generated method stub
        myMediaPlayer = new MediaPlayer();
        return super.onCreateDialogView();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            setSoundPrefSummary(RSID);
            callChangeListener(RSID);
        }
        myMediaPlayer.release();
    }

    static Thread mSoundThread;

    public int getSoundId() {
        return RSID;
    }

    public void setSoundPrefSummary(int id) {
        setSummary(soundNameEntries[id]);
        RSID = id;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        final CharSequence[] entries = getEntries();
        // final CharSequence[] entryValues = getEntryValues();

        builder.setSingleChoiceItems(entries, RSID, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    // runable
                    myMediaPlayer.reset();
                    RSID = item;
                    soundHandler.removeCallbacks(SoundsPreviewTask);
                    soundHandler.postDelayed(SoundsPreviewTask, 500);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    myMediaPlayer = new MediaPlayer();
                }
                // Toast.makeText(mContext, entries[item],
                // Toast.LENGTH_SHORT).show();
            }
        });

    }

    Handler soundHandler = new Handler();

    private Runnable SoundsPreviewTask = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                // myMediaPlayer.reset();
                myMediaPlayer = new MediaPlayer();
                // if (RSID==0) {
                //
                // }else {
                // myMediaPlayer.setVolume(0.9f, 0.9f);
                myMediaPlayer = MediaPlayer.create(mContext, getSoundRsId(RSID));
                myMediaPlayer.start();
                // }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                myMediaPlayer = new MediaPlayer();
            }

        }
    };

    private int getSoundRsId(int i) {
        int RsId = 0;
        switch (i + 1) {
            case 1:
                RsId = R.raw.ascending_preview;
                break;
            case 2:
                RsId = R.raw.birds_preview;
                break;
            case 3:
                RsId = R.raw.classic_preview;
                break;
            case 4:
                RsId = R.raw.cuckoo_preview;
                break;
            case 5:
                RsId = R.raw.digital_preview;
                break;
            case 6:
                RsId = R.raw.electronic_preview;
                break;
            case 7:
                RsId = R.raw.high_tone_preview;
                break;
            case 8:
                RsId = R.raw.mbira_preview;
                break;
            case 9:
                RsId = R.raw.old_clock_preview;
                break;
            case 10:
                RsId = R.raw.rooster_preview;
                break;
            case 11:
                RsId = R.raw.school_bell_preview;
                break;
            default:
                break;
        }
        return RsId;
    }
}
