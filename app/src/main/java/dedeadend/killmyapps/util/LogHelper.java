package dedeadend.killmyapps.util;

import android.util.Log;

public class LogHelper {

    private static final boolean ENABLED = false;

    private static final String BASE_TAG = "LogHelper/";
    public static final String TAG_SU = BASE_TAG + "SU";
    public static final String TAG_SHIZUKU = BASE_TAG + "Shizuku";
    public static final String TAG_SHIZUKU_PLUS = BASE_TAG + "ShizukuPlus";
    public static final String TAG_AUTO_KILL = BASE_TAG + "AutoKill";
    public static final String TAG_QUICK_ACTIONS = BASE_TAG + "QuickActions";
    public static final String TAG_WIDGET_PROVIDER = BASE_TAG + "WidgetProvider";

    public static void create(String tag, String message) {
        if (ENABLED)
            Log.w(tag, message);
    }
}
