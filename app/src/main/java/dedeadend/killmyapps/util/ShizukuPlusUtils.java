package dedeadend.killmyapps.util;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dedeadend.killmyapps.IKillAppService;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.service.ShizukuUserService;
import rikka.shizuku.Shizuku;

public class ShizukuPlusUtils {

    private static final int SHIZUKU_REQ_CODE = 1001;
    private static IKillAppService killAppService = null;

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

    private static CountDownLatch bindLatch = null;

    private static final Shizuku.UserServiceArgs userServiceArgs =
            new Shizuku.UserServiceArgs(new ComponentName("dedeadend.killmyapps", ShizukuUserService.class.getName()))
                    .daemon(false)
                    .processNameSuffix("shizuku")
                    .debuggable(false)
                    .version(1);

    private static synchronized boolean ensureServiceBound() {
        if (killAppService != null)
            return true;
        if (!checkShizuku())
            return false;

        bindLatch = new CountDownLatch(1);
        try {
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
            return bindLatch.await(2, TimeUnit.SECONDS) && killAppService != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static void requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.requestPermission(SHIZUKU_REQ_CODE);
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean checkShizuku() {
        try {
            if (!Shizuku.pingBinder())
                return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable e) {
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