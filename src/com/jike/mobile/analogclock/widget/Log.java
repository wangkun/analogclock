
package com.jike.mobile.analogclock.widget;

public class Log {

    public final static String LOGTAG = "JiKe Mobile";

    // public static final boolean LOGV = AlarmClock.DEBUG ? Config.LOGD :
    // Config.LOGV;
    public static final boolean LOGV = true;

    public static void v(String logMe) {
        android.util.Log.v(LOGTAG, /* SystemClock.uptimeMillis() + " " + */logMe);
    }

    public static void i(String logMe) {
        android.util.Log.i(LOGTAG, logMe);
    }

    public static void e(String logMe) {
        android.util.Log.e(LOGTAG, logMe);
    }

    public static void e(String logMe, Exception ex) {
        android.util.Log.e(LOGTAG, logMe, ex);
    }
}
