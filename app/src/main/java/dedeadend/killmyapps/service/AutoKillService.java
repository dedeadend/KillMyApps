package dedeadend.killmyapps.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.AppListHelper;

public class AutoKillService extends Service {

    private static final String CHANNEL_ID = "auto_kill_channel";
    private static final int NOTIF_ID = 1001;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable killRunnable;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action))
                scheduleKillTask();
            else if (Intent.ACTION_SCREEN_ON.equals(action))
                cancelKillTask();
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
        return START_STICKY;
    }

    private void scheduleKillTask() {
        cancelKillTask();
        long delayMillis = App.settings.getInt(App.SCREEN_OFF_AUTO_KILL_DELAY, 5) * 60 * 1000L;

        killRunnable = () -> App.executorService.execute(() -> {
            List<AppInfo> targets = AppListHelper.getFilteredAppsList(getApplicationContext(), true);
            if (!targets.isEmpty()) {
                Killer.killListOfApps(targets);
            }
        });

        handler.postDelayed(killRunnable, delayMillis);
    }

    private void cancelKillTask() {
        if (killRunnable != null) {
            handler.removeCallbacks(killRunnable);
            killRunnable = null;
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Screen Lock Auto Kill",
                NotificationManager.IMPORTANCE_MIN
        );
        channel.setDescription("Keeps the auto kill service active to detect screen lock events.");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto kill on screen lock")
                .setContentText("Ready to kill background apps when screen locks")
                .setSmallIcon(R.drawable.ic_snow)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelKillTask();
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