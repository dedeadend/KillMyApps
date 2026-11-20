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

    private static volatile IKillAppService killAppService;
    private static volatile CountDownLatch bindLatch;

    private static final Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(
            new ComponentName("dedeadend.killmyapps", ShizukuUserService.class.getName())
    )
            .daemon(false)
            .processNameSuffix("shizuku")
            .debuggable(false)
            .version(2);

    private static final ServiceConnection userServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            killAppService = IKillAppService.Stub.asInterface(binder);
            CountDownLatch latch = bindLatch;
            if (latch != null)
                latch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            killAppService = null;
            CountDownLatch latch = bindLatch;
            bindLatch = null;
            if (latch != null)
                latch.countDown();
        }
    };

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
            LogHelper.create(LogHelper.TAG_SHIZUKU_PLUS, e.getMessage());
            return false;
        }
    }

    private static boolean isServiceAlive() {
        IKillAppService service = killAppService;
        if (service == null)
            return false;

        IBinder binder = service.asBinder();
        return binder != null && binder.isBinderAlive() && binder.pingBinder();
    }

    private static boolean ensureServiceBound() {
        if (isServiceAlive())
            return true;

        CountDownLatch latch = bindLatch;
        if (latch == null || latch.getCount() == 0) {
            if (!checkShizuku())
                return false;

            latch = new CountDownLatch(1);
            bindLatch = latch;
            try {
                Shizuku.bindUserService(userServiceArgs, userServiceConnection);
            } catch (Exception e) {
                LogHelper.create(LogHelper.TAG_SHIZUKU_PLUS, e.getMessage());
                if (bindLatch == latch) bindLatch = null;
                return isServiceAlive();
            }
        }
        try {
            return latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS) && isServiceAlive();
        } catch (Exception e) {
            LogHelper.create(LogHelper.TAG_SHIZUKU_PLUS, e.getMessage());
            return false;
        }
    }

    private static void invalidateService(IKillAppService service) {
        if (killAppService == service) {
            killAppService = null;
            bindLatch = null;
        }
    }

    public static boolean killApp(String pkgName, int level) {
        if (!ensureServiceBound())
            return false;

        IKillAppService service = killAppService;
        try {
            return service.killApp(pkgName, level);
        } catch (Exception e) {
            LogHelper.create(LogHelper.TAG_SHIZUKU_PLUS, e.getMessage());
            invalidateService(service);
            return false;
        }
    }

    public static int killListOfApps(List<AppInfo> appList, int level) {
        if (appList == null)
            return -1;

        if (appList.isEmpty())
            return 0;

        if (!ensureServiceBound())
            return -1;

        IKillAppService service = killAppService;
        try {
            List<String> packages = new ArrayList<>(appList.size());
            for (AppInfo app : appList)
                packages.add(app.getPkgName());
            return service.killListOfApps(packages, level);
        } catch (Exception e) {
            LogHelper.create(LogHelper.TAG_SHIZUKU_PLUS, e.getMessage());
            invalidateService(service);
            return -1;
        }
    }

    public static boolean sleep() {
        if (!ensureServiceBound())
            return false;

        IKillAppService service = killAppService;
        try {
            return service.sleep();
        } catch (Exception e) {
            LogHelper.create(LogHelper.TAG_SHIZUKU_PLUS, e.getMessage());
            invalidateService(service);
            return false;
        }
    }
}