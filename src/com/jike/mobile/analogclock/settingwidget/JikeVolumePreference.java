
package com.jike.mobile.analogclock.settingwidget;

import com.jike.mobile.analogclock.R;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * Preference
 * 
 * @author kunwang
 */
public class JikeVolumePreference extends DialogPreference {

    private Context context;

    private SeekBar sensitivityLevel = null;

    private LinearLayout layout = null;

    private int volume;

    private Resources res;

    public JikeVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        res = context.getResources();
        // persistInt(10);
    }

    public int setVolume(int mVolume) {
        volume = mVolume;
        this.setSummary(mVolume == 0 ? res.getString(R.string.slient) : mVolume + "%");
        return volume;

    }

    @Override
    public void setDialogMessage(CharSequence dialogMessage) {
        // TODO Auto-generated method stub
        super.setDialogMessage(dialogMessage);
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)); //
        layout.setMinimumWidth(400);
        layout.setPadding(20, 20, 20, 20); // Padding
        // SeekBar
        sensitivityLevel = new SeekBar(context);
        sensitivityLevel.setMax(100);
        sensitivityLevel.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)); // SeekBar
        sensitivityLevel.setProgress(volume);
        layout.addView(sensitivityLevel); // SeekBar layout
        builder.setView(layout);
    }

    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(sensitivityLevel.getProgress());
            this.getOnPreferenceChangeListener().onPreferenceChange(this,
                    sensitivityLevel.getProgress());

            Log.d("sensitivityLevel.getProgress()", " " + sensitivityLevel.getProgress());
        }

        super.onDialogClosed(positiveResult);
    }

}
