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
    private static final int ALARM_REQUEST_CODE = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false))
                scheduleDailyAlarm(context);
            else
                cancelDailyAlarm(context);
            AutoKillHelper.syncServiceState();
            return;
        }

        if (!ACTION_SCHEDULED_KILL.equals(action))
            return;

        if (!App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)) {
            cancelDailyAlarm(context);
            return;
        }

        scheduleDailyAlarm(context);
        PendingResult pendingResult = goAsync();
        AutoKillHelper.executeScheduledKill(pendingResult::finish);
    }

    public static void scheduleDailyAlarm(Context context) {
        if (!App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)) {
            cancelDailyAlarm(context);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.set(Calendar.HOUR_OF_DAY, App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3));
        scheduledTime.set(Calendar.MINUTE, App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30));
        scheduledTime.set(Calendar.SECOND, 0);
        scheduledTime.set(Calendar.MILLISECOND, 0);
        if (!scheduledTime.after(Calendar.getInstance()))
            scheduledTime.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime.getTimeInMillis(),
                getAlarmPendingIntent(context)
        );
    }

    public static void cancelDailyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.cancel(getAlarmPendingIntent(context));
    }

    private static PendingIntent getAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, AutoKillReceiver.class);
        intent.setAction(ACTION_SCHEDULED_KILL);
        return PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}