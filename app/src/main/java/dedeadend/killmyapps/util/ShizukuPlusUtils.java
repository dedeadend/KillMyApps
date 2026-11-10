package dedeadend.killmyapps.util;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dedeadend.killmyapps.IKillAppService;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.service.ShizukuUserService;
import rikka.shizuku.Shizuku;

public class ShizukuPlusUtils {

    private static final long TIMEOUT_SECONDS = 10;
    private static final int SHIZUKU_REQ_CODE = 1001;
    private static volatile IKillAppService killAppService = null;
    private static volatile CountDownLatch bindLatch = null;

    private static final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            killAppService = IKillAppService.Stub.asInterface(service);
            if (bindLatch != null) {
                bindLatch.countDown();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            killAppService = null;
        }
    };

    private static final Shizuku.UserServiceArgs userServiceArgs =
            new Shizuku.UserServiceArgs(new ComponentName("dedeadend.killmyapps", ShizukuUserService.class.getName()))
                    .daemon(false)
                    .processNameSuffix("shizuku")
                    .debuggable(false)
                    .version(1);

    public static boolean checkShizuku() {
        try {
            if (!Shizuku.pingBinder())
                return false;

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                return true;

            CountDownLatch permissionLatch = new CountDownLatch(1);
            AtomicInteger grantResult = new AtomicInteger(PackageManager.PERMISSION_DENIED);

            Shizuku.OnRequestPermissionResultListener listener = (requestCode, result) -> {
                if (requestCode == SHIZUKU_REQ_CODE) {
                    grantResult.set(result);
                    permissionLatch.countDown();
                }
            };

            Shizuku.addRequestPermissionResultListener(listener);
            try {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                    return true;

                Shizuku.requestPermission(SHIZUKU_REQ_CODE);
                if (!permissionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    return false;

                return grantResult.get() == PackageManager.PERMISSION_GRANTED
                        && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            } finally {
                Shizuku.removeRequestPermissionResultListener(listener);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static synchronized boolean ensureServiceBound() {
        if (killAppService != null)
            return true;
        if (!checkShizuku())
            return false;

        CountDownLatch latch = new CountDownLatch(1);
        bindLatch = latch;
        try {
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
            boolean connected = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    && killAppService != null;
            if (!connected) {
                bindLatch = null;
                Shizuku.unbindUserService(userServiceArgs, userServiceConnection, false);
            }
            return connected;
        } catch (Exception e) {
            bindLatch = null;
            try {
                Shizuku.unbindUserService(userServiceArgs, userServiceConnection, false);
            } catch (Exception ignored) {
                return false;
            }
            return false;
        }
    }

    public static boolean killApp(String pkgName, int level) {
        if (!ensureServiceBound())
            return false;
        try {
            return killAppService.killApp(pkgName, level);
        } catch (Exception e) {
            return false;
        }
    }

    public static int killListOfApps(List<AppInfo> appList, int level) {
        if (!ensureServiceBound())
            return -1;
        try {
            List<String> pkgList = new ArrayList<>(appList.size());
            for (AppInfo app : appList) {
                pkgList.add(app.getPkgName());
            }
            return killAppService.killListOfApps(pkgList, level);
        } catch (Exception e) {
            return -1;
        }
    }
}