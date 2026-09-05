package dedeadend.killmyapps.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.MainActivity;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.util.AutoKillHelper;

public class AutoKillService extends Service {

    private static final String CHANNEL_ID = "auto_kill_channel";
    private static final int NOTIF_ID = 1001;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable killRunnable;
    private PowerManager.WakeLock currentWakeLock;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if (App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false))
                    scheduleScreenOffKill();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                cancelScreenOffKill();
                runScheduledKillFallbackIfNeeded();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            startForeground(NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else
            startForeground(NOTIF_ID, buildNotification());
        if (!App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false) &&
                !App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void scheduleScreenOffKill() {
        cancelScreenOffKill();
        long delayMillis = App.settings.getInt(App.SCREEN_OFF_AUTO_KILL_DELAY, 0) * 60 * 1000L;
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KillMyApps:ScreenOffWakeLock");
            wakeLock.acquire(delayMillis + 10000L);
        }
        this.currentWakeLock = wakeLock;
        final PowerManager.WakeLock activeLock = wakeLock;
        killRunnable = () -> AutoKillHelper.executeKill(() -> releaseWakeLock(activeLock));
        handler.postDelayed(killRunnable, delayMillis);
    }

    private void cancelScreenOffKill() {
        if (killRunnable != null) {
            handler.removeCallbacks(killRunnable);
            killRunnable = null;
        }
        releaseWakeLock(currentWakeLock);
        currentWakeLock = null;
    }

    private void runScheduledKillFallbackIfNeeded() {
        if (AutoKillHelper.isScheduledKillMissedToday())
            AutoKillHelper.executeKill(AutoKillHelper::markScheduledKillExecutedToday);
    }

    private void releaseWakeLock(PowerManager.WakeLock lock) {
        if (lock != null && lock.isHeld()) {
            try {
                lock.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                "Auto Kill Service",
                NotificationManager.IMPORTANCE_MIN
        );
        notificationChannel.setDescription("Keeps the auto kill service active to detect screen lock events.");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null)
            notificationManager.createNotificationChannel(notificationChannel);
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto Kill Service")
                .setContentText("KillMyApps will close apps automatically based on your settings")
                .setSmallIcon(R.drawable.ic_snow)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(screenReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}