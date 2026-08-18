package dedeadend.killmyapps;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dedeadend.killmyapps.data.Database;
import dedeadend.killmyapps.util.AutoKillHelper;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class App extends Application {

    public static final String THEME_MODE = "themeMode";
    public static final String KILLER_MODE = "killerMode";
    public static final String LIST_MODE = "listMode";
    public static final String SELECTION_MODE = "selectionMode";
    public static final String HIDE_KILL_MY_APPS = "hideKillMyApps";
    public static final String HIDE_DEFAULT_LAUNCHER = "hideDefaultLauncher";
    public static final String HIDE_DEFAULT_ALARM = "hideDefaultAlarm";
    public static final String HIDE_DEFAULT_KEYBOARD = "hideDefaultKeyboard";
    public static final String HIDE_DEFAULT_DIALER = "hideDefaultDialer";
    public static final String HIDE_DEFAULT_SMS = "hideDefaultSMS";
    public static final String HIDE_CRITICAL_SYSTEM_APPS = "hideCriticalSystemApps";
    public static final String SHOW_PKGNAME = "showPkgName";
    public static final String CLICK_TO_APP_INFO = "clickToAppInfo";
    public static final String LONG_CLICK_TO_MENU = "longClickToMenu";
    public static final String SHOW_SCROLL_ANIMATION = "showScrollAnimation";
    public static final String SCREEN_OFF_AUTO_KILL = "screenOffAutoKill";
    public static final String SCREEN_OFF_AUTO_KILL_DELAY = "screenOffAutoKillDelay";
    public static final String FIXED_TIME_AUTO_KILL = "fixedTimeAutoKill";
    public static final String FIXED_TIME_AUTO_KILL_HOUR = "fixedTimeAutoKillHour";
    public static final String FIXED_TIME_AUTO_KILL_MINUTE = "fixedTimeAutoKillMinute";

    public static Database database;
    public static SharedPreferences settings;
    public static Context context;
    public static ExecutorService executorService;
    public static Handler handler;
    public static boolean isFirstRun;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this, Database.class, "database").build();
        settings = getSharedPreferences("settings", MODE_PRIVATE);
        context = getApplicationContext();
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        isFirstRun = settings.getBoolean("isFirstRun", true);
        if (isFirstRun)
            settings.edit().putBoolean("isFirstRun", false).apply();
        initAutoKillServices();
    }

    public static void setAppThemeMode() {
        int themeMode = App.settings.getInt(App.THEME_MODE, 0);
        if (themeMode == 0)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else if (themeMode == 1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (themeMode == 2)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static void toast(Activity activity, String title, String message) {
        MotionToast.Companion.darkToast(activity, title, message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.helvetica_regular));
    }

    public static void notificationWarningToast(Activity activity) {
        MotionToast.Companion.darkToast(
                activity,
                "Permission Required",
                "Notification permission is required to keep the auto-kill service running",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.helvetica_regular));
    }

    public static void liteToast(String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initAutoKillServices() {
        if (settings.getBoolean(SCREEN_OFF_AUTO_KILL, false)) {
            boolean hasNotificationPermission = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;
            if (hasNotificationPermission)
                AutoKillHelper.enableScreenOffKill(
                        context,
                        settings.getInt(SCREEN_OFF_AUTO_KILL_DELAY, 5)
                );
        }
        if (settings.getBoolean(FIXED_TIME_AUTO_KILL, false))
            AutoKillHelper.enableFixedTimeKill(
                    context,
                    settings.getInt(FIXED_TIME_AUTO_KILL_HOUR, 3),
                    settings.getInt(FIXED_TIME_AUTO_KILL_MINUTE, 30)
            );
    }
}
