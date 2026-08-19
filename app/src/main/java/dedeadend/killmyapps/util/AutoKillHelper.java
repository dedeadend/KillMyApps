package dedeadend.killmyapps.util;

import android.content.Intent;

import androidx.core.content.ContextCompat;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.receiver.AutoKillReceiver;
import dedeadend.killmyapps.service.AutoKillService;

public class AutoKillHelper {

    public static void enableScreenOffKill() {
        Intent intent = new Intent(App.context, AutoKillService.class);
        ContextCompat.startForegroundService(App.context, intent);
    }

    public static void disableScreenOffKill() {
        Intent intent = new Intent(App.context, AutoKillService.class);
        App.context.stopService(intent);
    }

    public static void enableFixedTimeKill() {
        AutoKillReceiver.scheduleDailyAlarm(App.context);
    }

    public static void disableFixedTimeKill() {
        AutoKillReceiver.cancelDailyAlarm(App.context);
    }
}