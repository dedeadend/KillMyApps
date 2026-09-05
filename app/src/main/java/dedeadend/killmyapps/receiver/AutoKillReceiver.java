package dedeadend.killmyapps.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.util.AutoKillHelper;

public class AutoKillReceiver extends BroadcastReceiver {

    public static final String ACTION_SCHEDULED_KILL = "dedeadend.killmyapps.action.SCHEDULED_KILL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false))
                scheduleDailyAlarm(context);
            AutoKillHelper.syncServiceState();
            return;
        }
        if (ACTION_SCHEDULED_KILL.equals(action)) {
            PendingResult pendingResult = goAsync();
            AutoKillHelper.executeKill(() -> {
                AutoKillHelper.markScheduledKillExecutedToday();
                pendingResult.finish();
            });
            if (App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false))
                scheduleDailyAlarm(context);
        }
    }

    public static void scheduleDailyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;
        PendingIntent pendingIntent = getAlarmPendingIntent(context);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3));
        calendar.set(Calendar.MINUTE, App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelDailyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;
        alarmManager.cancel(getAlarmPendingIntent(context));
    }

    private static PendingIntent getAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, AutoKillReceiver.class);
        intent.setAction(ACTION_SCHEDULED_KILL);
        return PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}