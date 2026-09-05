package dedeadend.killmyapps.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.receiver.AutoKillReceiver;
import dedeadend.killmyapps.service.AutoKillService;

public class AutoKillHelper {

    public static void syncServiceState() {
        boolean hasNotificationPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                    App.context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        Intent intent = new Intent(App.context, AutoKillService.class);
        if ((App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false) ||
                App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)
        ) && hasNotificationPermission)
            ContextCompat.startForegroundService(App.context, intent);
        else
            App.context.stopService(intent);
    }

    public static void executeKill(Runnable onComplete) {
        App.executorService.execute(() -> {
            try {
                List<AppInfo> appsList = AppListHelper.getFilteredAppsList(App.context, true);
                if (!appsList.isEmpty())
                    Killer.killListOfApps(appsList);
            } finally {
                if (onComplete != null)
                    onComplete.run();
            }
        });
    }

    private static void syncScheduledKillDate() {
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3));
        targetTime.set(Calendar.MINUTE, App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30));
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);
        if (Calendar.getInstance().after(targetTime))
            markScheduledKillExecutedToday();
        else
            resetScheduledKillDate();
    }

    public static boolean isScheduledKillMissedToday() {
        if (!App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false) || isScheduledKillExecutedToday())
            return false;
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3));
        targetTime.set(Calendar.MINUTE, App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30));
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);
        return Calendar.getInstance().after(targetTime);
    }

    public static boolean isScheduledKillExecutedToday() {
        return App.settings.getInt(App.SCHEDULED_AUTO_KILL_DATE, 0) == getTodayDateKey();
    }

    public static void markScheduledKillExecutedToday() {
        App.settings.edit().putInt(App.SCHEDULED_AUTO_KILL_DATE, getTodayDateKey()).apply();
    }

    public static void resetScheduledKillDate() {
        App.settings.edit().remove(App.SCHEDULED_AUTO_KILL_DATE).apply();
    }

    private static int getTodayDateKey() {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) * 10000
                + (today.get(Calendar.MONTH) + 1) * 100
                + today.get(Calendar.DAY_OF_MONTH);
    }

    public static void enableScreenOffKill() {
        syncServiceState();
    }

    public static void disableScreenOffKill() {
        syncServiceState();
    }

    public static void enableScheduledKill() {
        syncScheduledKillDate();
        AutoKillReceiver.scheduleDailyAlarm(App.context);
        syncServiceState();
    }

    public static void disableScheduledKill() {
        AutoKillReceiver.cancelDailyAlarm(App.context);
        syncServiceState();
    }
}