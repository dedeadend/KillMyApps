package dedeadend.killmyapps.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.receiver.AutoKillReceiver;
import dedeadend.killmyapps.service.AutoKillService;

public class AutoKillHelper {

    private static final AtomicLong SCREEN_OFF_KILL_GENERATION = new AtomicLong();
    private static final AtomicLong SCHEDULED_KILL_GENERATION = new AtomicLong();
    private static final AtomicBoolean SCHEDULED_KILL_IN_PROGRESS = new AtomicBoolean();

    public static void syncServiceState() {
        syncServiceState(null);
    }

    private static void syncServiceState(String serviceAction) {
        boolean hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(
                App.context,
                android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;

        Intent intent = new Intent(App.context, AutoKillService.class);
        boolean shouldRunService = App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false)
                || App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false);

        if (shouldRunService && hasNotificationPermission) {
            if (serviceAction != null)
                intent.setAction(serviceAction);
            ContextCompat.startForegroundService(App.context, intent);
        } else {
            App.context.stopService(intent);
        }
    }

    public static void executeScreenOffKill(Runnable onComplete) {
        long generation = SCREEN_OFF_KILL_GENERATION.get();
        executeKill(
                () -> generation == SCREEN_OFF_KILL_GENERATION.get()
                        && App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false),
                onComplete
        );
    }

    private static void executeKill(BooleanSupplier canStart, Runnable onComplete) {
        App.executorService.execute(() -> {
            try {
                if (canStart.getAsBoolean()) {
                    List<AppInfo> apps = AppListHelper.getFilteredAppsList(App.context, true);
                    if (!apps.isEmpty())
                        Killer.killListOfApps(apps);
                }
            } finally {
                if (onComplete != null)
                    onComplete.run();
            }
        });
    }

    public static void executeScheduledKill(Runnable onFinished) {
        executeScheduledKill(false, onFinished);
    }

    public static void executeScheduledKillIfMissedToday() {
        executeScheduledKill(true, null);
    }

    private static void executeScheduledKill(boolean onlyIfMissed, Runnable onFinished) {
        if (!App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)
                || isScheduledKillExecutedToday()
                || (onlyIfMissed && !hasScheduledTimePassed())
                || !SCHEDULED_KILL_IN_PROGRESS.compareAndSet(false, true)) {
            if (onFinished != null)
                onFinished.run();
            return;
        }

        long generation = SCHEDULED_KILL_GENERATION.get();
        try {
            executeKill(() -> {
                if (generation != SCHEDULED_KILL_GENERATION.get()
                        || !App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)
                        || isScheduledKillExecutedToday())
                    return false;

                markScheduledKillExecutedToday();
                if (generation != SCHEDULED_KILL_GENERATION.get()) {
                    updateScheduledKillDateForCurrentSettings();
                    return false;
                }
                return true;
            }, () -> finishScheduledKill(onFinished));
        } catch (RuntimeException ignored) {
            finishScheduledKill(onFinished);
        }
    }

    private static void finishScheduledKill(Runnable onFinished) {
        SCHEDULED_KILL_IN_PROGRESS.set(false);
        if (onFinished != null)
            onFinished.run();
    }

    private static boolean hasScheduledTimePassed() {
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3));
        targetTime.set(Calendar.MINUTE, App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30));
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);
        return !Calendar.getInstance().before(targetTime);
    }

    private static boolean isScheduledKillExecutedToday() {
        return App.settings.getInt(App.SCHEDULED_AUTO_KILL_DATE, 0) == getTodayDateKey();
    }

    private static void markScheduledKillExecutedToday() {
        App.settings.edit().putInt(App.SCHEDULED_AUTO_KILL_DATE, getTodayDateKey()).apply();
    }

    public static void resetScheduledKillDate() {
        SCHEDULED_KILL_GENERATION.incrementAndGet();
        App.settings.edit().remove(App.SCHEDULED_AUTO_KILL_DATE).apply();
    }

    public static void onScheduledSettingsChanged(boolean enabled) {
        SCHEDULED_KILL_GENERATION.incrementAndGet();
        if (enabled) {
            updateScheduledKillDateForCurrentSettings();
            AutoKillReceiver.scheduleDailyAlarm(App.context);
        } else {
            AutoKillReceiver.cancelDailyAlarm(App.context);
            App.settings.edit().remove(App.SCHEDULED_AUTO_KILL_DATE).apply();
        }
        syncServiceState(AutoKillService.ACTION_SKIP_SCHEDULED_FALLBACK);
    }

    private static void updateScheduledKillDateForCurrentSettings() {
        if (App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)
                && hasScheduledTimePassed())
            markScheduledKillExecutedToday();
        else
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
        SCREEN_OFF_KILL_GENERATION.incrementAndGet();
        App.settings.edit().remove(App.SCREEN_OFF_AUTO_KILL_START_TIME).apply();
        syncServiceState(AutoKillService.ACTION_CANCEL_SCREEN_OFF_KILL);
    }

    public static void enableScheduledKill() {
        AutoKillReceiver.scheduleDailyAlarm(App.context);
        syncServiceState(AutoKillService.ACTION_SKIP_SCHEDULED_FALLBACK);
    }

    public static void initializeScheduledKill() {
        AutoKillReceiver.scheduleDailyAlarm(App.context);
        syncServiceState();
    }

    public static void disableScheduledKill() {
        AutoKillReceiver.cancelDailyAlarm(App.context);
        resetScheduledKillDate();
        syncServiceState();
    }
}