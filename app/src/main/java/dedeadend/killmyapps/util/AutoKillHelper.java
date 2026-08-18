package dedeadend.killmyapps.util;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import dedeadend.killmyapps.receiver.AutoKillReceiver;
import dedeadend.killmyapps.service.AutoKillService;

public class AutoKillHelper {

    public static void enableScreenOffKill(Context context) {
        Intent intent = new Intent(context, AutoKillService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void disableScreenOffKill(Context context) {
        Intent intent = new Intent(context, AutoKillService.class);
        context.stopService(intent);
    }

    public static void enableFixedTimeKill(Context context) {
        AutoKillReceiver.scheduleDailyAlarm(context);
    }

    public static void disableFixedTimeKill(Context context) {
        AutoKillReceiver.cancelDailyAlarm(context);
    }
}