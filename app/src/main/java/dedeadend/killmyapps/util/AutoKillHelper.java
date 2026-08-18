package dedeadend.killmyapps.util;

import android.content.Context;
import android.content.Intent;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.receiver.AutoKillReceiver;
import dedeadend.killmyapps.service.AutoKillService;

public class AutoKillHelper {

    public static void enableScreenOffKill(Context context, int delayMinutes) {
        App.settings.edit()
                .putBoolean(App.SCREEN_OFF_AUTO_KILL, true)
                .putInt(App.SCREEN_OFF_AUTO_KILL_DELAY, delayMinutes)
                .apply();

        Intent intent = new Intent(context, AutoKillService.class);
        context.startForegroundService(intent);
    }

    public static void disableScreenOffKill(Context context) {
        App.settings.edit().putBoolean(App.SCREEN_OFF_AUTO_KILL, false).apply();

        Intent intent = new Intent(context, AutoKillService.class);
        context.stopService(intent);
    }

    public static void enableFixedTimeKill(Context context, int hour, int minute) {
        App.settings.edit()
                .putBoolean(App.FIXED_TIME_AUTO_KILL, true)
                .putInt(App.FIXED_TIME_AUTO_KILL_HOUR, hour)
                .putInt(App.FIXED_TIME_AUTO_KILL_MINUTE, minute)
                .apply();

        AutoKillReceiver.scheduleDailyAlarm(context, hour, minute);
    }

    public static void disableFixedTimeKill(Context context) {
        App.settings.edit().putBoolean(App.FIXED_TIME_AUTO_KILL, false).apply();

        AutoKillReceiver.cancelDailyAlarm(context);
    }
}