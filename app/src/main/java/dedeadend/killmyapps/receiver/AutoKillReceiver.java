package dedeadend.killmyapps.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.AppListHelper;
import dedeadend.killmyapps.util.AutoKillHelper;

public class AutoKillReceiver extends BroadcastReceiver {

    public static final String ACTION_FIXED_TIME_KILL = "dedeadend.killmyapps.action.FIXED_TIME_KILL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (App.settings.getBoolean(App.FIXED_TIME_AUTO_KILL, false)) {
                scheduleDailyAlarm(context);
            }

            if (App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false)) {
                boolean hasPermission = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasPermission = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED;
                }
                if (hasPermission)
                    AutoKillHelper.enableScreenOffKill(context);
            }
            return;
        }

        if (ACTION_FIXED_TIME_KILL.equals(action)) {
            App.executorService.execute(() -> {
                List<AppInfo> targets = AppListHelper.getFilteredAppsList(context, true);
                if (!targets.isEmpty())
                    Killer.killListOfApps(targets);
            });

            if (App.settings.getBoolean(App.FIXED_TIME_AUTO_KILL, false)) {
                scheduleDailyAlarm(context);
            }
        }
    }

    public static void scheduleDailyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, AutoKillReceiver.class);
        intent.setAction(ACTION_FIXED_TIME_KILL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, App.settings.getInt(App.FIXED_TIME_AUTO_KILL_HOUR, 3));
        calendar.set(Calendar.MINUTE, App.settings.getInt(App.FIXED_TIME_AUTO_KILL_MINUTE, 30));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelDailyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, AutoKillReceiver.class);
        intent.setAction(ACTION_FIXED_TIME_KILL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}