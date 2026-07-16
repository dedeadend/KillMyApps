package dedeadend.killmyapps.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.provider.Telephony;
import android.telecom.TelecomManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.model.PKGName;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<AppInfo>> appsList;
    private Set<String> selectionListHash;
    private boolean hideKillMyApps, hideDefaultLauncher, hideDefaultAlarm, hideDefaultKeyboard,
            hideDefaultDialer, hideDefaultSms, hideCriticalPackages;
    private int selectionMode;

    public HomeViewModel() {
        appsList = new MutableLiveData<>();
    }

    public MutableLiveData<List<AppInfo>> getAppsList() {
        return appsList;
    }

    public void refreshList() {
        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<PKGName> excludedlist = App.database.excludedPkgDao().getAll();
                selectionListHash = new HashSet<>();
                for (PKGName pkgName : excludedlist)
                    selectionListHash.add(pkgName.name);
                int listMode = App.settings.getInt(App.LIST_MODE, 1);
                if (listMode == 0) {
                    getUserAppsList(App.context);
                } else if (listMode == 1) {
                    getLauncherAppsList(App.context);
                } else if (listMode == 2) {
                    getSystemAppsList(App.context);
                }
            }
        });
    }

    public void checkForRefresh() {
        if (appsList.getValue().size() == 1)
            refreshList();
    }

    public int clearList() {
        int size = appsList.getValue() == null ? 0 : appsList.getValue().size();
        appsList.setValue(new ArrayList<>());
        return size;
    }

    private void getSystemAppsList(Context context) {
        selectionMode = App.settings.getInt(App.SELECTION_MODE, 0);
        hideKillMyApps = App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true);
        hideDefaultLauncher = App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true);
        hideDefaultAlarm = App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true);
        hideDefaultKeyboard = App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true);
        hideDefaultDialer = App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true);
        hideDefaultSms = App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true);
        hideCriticalPackages = App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true);
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        String launcherPkg = hideDefaultLauncher ? getLauncherPkgName() : "";
        String alarmPkg = hideDefaultAlarm ? getAlarmPkgName() : "";
        String keyboardPkg = hideDefaultKeyboard ? getDefaultKeyboardPkgName() : "";
        String dialerPkg = hideDefaultDialer ? getDefaultDialerPkgName() : "";
        String smsPkg = hideDefaultSms ? getDefaultSmsPkgName() : "";
        applications.removeIf(app -> {
            String pkgName = app.packageName;
            return (app.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED
                    || (selectionMode == 0 && selectionListHash.contains(pkgName))
                    || (selectionMode == 1 && !selectionListHash.contains(pkgName))
                    || (hideCriticalPackages && CRITICAL_PACKAGES.contains(pkgName))
                    || (hideDefaultLauncher && pkgName.equals(launcherPkg))
                    || (hideDefaultAlarm && pkgName.equals(alarmPkg))
                    || (hideDefaultKeyboard && pkgName.equals(keyboardPkg))
                    || (hideDefaultDialer && pkgName.equals(dialerPkg))
                    || (hideDefaultSms && pkgName.equals(smsPkg))
                    || (hideKillMyApps && pkgName.equals("dedeadend.killmyapps"));
        });
        List<AppInfo> temp = AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
        temp.sort(AppInfo::compareTo);
        App.handler.post(new Runnable() {
            @Override
            public void run() {
                appsList.setValue(temp);
            }
        });
    }

    private void getUserAppsList(Context context) {
        selectionMode = App.settings.getInt(App.SELECTION_MODE, 0);
        hideKillMyApps = App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true);
        hideDefaultLauncher = App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true);
        hideDefaultAlarm = App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true);
        hideDefaultKeyboard = App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true);
        hideDefaultDialer = App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true);
        hideDefaultSms = App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true);
        hideCriticalPackages = App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true);
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        String launcherPkg = hideDefaultLauncher ? getLauncherPkgName() : "";
        String alarmPkg = hideDefaultAlarm ? getAlarmPkgName() : "";
        String keyboardPkg = hideDefaultKeyboard ? getDefaultKeyboardPkgName() : "";
        String dialerPkg = hideDefaultDialer ? getDefaultDialerPkgName() : "";
        String smsPkg = hideDefaultSms ? getDefaultSmsPkgName() : "";
        applications.removeIf(app -> {
            String pkgName = app.packageName;
            return (app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM
                    || (app.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED
                    || (selectionMode == 0 && selectionListHash.contains(pkgName))
                    || (selectionMode == 1 && !selectionListHash.contains(pkgName))
                    || (hideCriticalPackages && CRITICAL_PACKAGES.contains(pkgName))
                    || (hideDefaultLauncher && pkgName.equals(launcherPkg))
                    || (hideDefaultAlarm && pkgName.equals(alarmPkg))
                    || (hideDefaultKeyboard && pkgName.equals(keyboardPkg))
                    || (hideDefaultDialer && pkgName.equals(dialerPkg))
                    || (hideDefaultSms && pkgName.equals(smsPkg))
                    || (hideKillMyApps && pkgName.equals("dedeadend.killmyapps"));
        });
        List<AppInfo> temp = AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
        temp.sort(AppInfo::compareTo);
        App.handler.post(new Runnable() {
            @Override
            public void run() {
                appsList.setValue(temp);
            }
        });
    }

    private void getLauncherAppsList(Context context) {
        selectionMode = App.settings.getInt(App.SELECTION_MODE, 0);
        hideKillMyApps = App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true);
        hideDefaultLauncher = App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true);
        hideDefaultAlarm = App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true);
        hideDefaultKeyboard = App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true);
        hideDefaultDialer = App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true);
        hideDefaultSms = App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true);
        hideCriticalPackages = App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true);
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ResolveInfo> launcherApps = pm.queryIntentActivities(intent, 0);
        Set<String> launcherPkgNames = new HashSet<>();
        for (ResolveInfo resolveInfo : launcherApps) {
            launcherPkgNames.add(resolveInfo.activityInfo.packageName);
        }
        String launcherPkg = hideDefaultLauncher ? getLauncherPkgName() : "";
        String alarmPkg = hideDefaultAlarm ? getAlarmPkgName() : "";
        String keyboardPkg = hideDefaultKeyboard ? getDefaultKeyboardPkgName() : "";
        String dialerPkg = hideDefaultDialer ? getDefaultDialerPkgName() : "";
        String smsPkg = hideDefaultSms ? getDefaultSmsPkgName() : "";
        applications.removeIf(app -> {
            String pkgName = app.packageName;
            return (app.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED
                    || !launcherPkgNames.contains(pkgName)
                    || (selectionMode == 0 && selectionListHash.contains(pkgName))
                    || (selectionMode == 1 && !selectionListHash.contains(pkgName))
                    || (hideCriticalPackages && CRITICAL_PACKAGES.contains(pkgName))
                    || (hideDefaultLauncher && pkgName.equals(launcherPkg))
                    || (hideDefaultAlarm && pkgName.equals(alarmPkg))
                    || (hideDefaultKeyboard && pkgName.equals(keyboardPkg))
                    || (hideDefaultDialer && pkgName.equals(dialerPkg))
                    || (hideDefaultSms && pkgName.equals(smsPkg))
                    || (hideKillMyApps && pkgName.equals("dedeadend.killmyapps"));
        });
        List<AppInfo> temp = AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
        temp.sort(AppInfo::compareTo);
        App.handler.post(new Runnable() {
            @Override
            public void run() {
                appsList.setValue(temp);
            }
        });
    }

    public void onKillAllAppsClicked(OnResultListener onResultListener) {
        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (appsList.getValue() != null) {
                    if (Killer.killListOfApps(appsList.getValue())) {
                        App.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onResultListener.onKillSuccessfully(clearList());
                            }
                        });
                    } else {
                        App.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onResultListener.onKillFailed();
                            }
                        });
                    }
                }
            }
        });
    }

    public void onKillSingleAppClicked(String pkgName, OnResultListener onResultListener) {
        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (Killer.killApp(pkgName)) {
                    App.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onResultListener.onKillSuccessfully(1);
                        }
                    });
                } else {
                    App.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onResultListener.onKillFailed();
                        }
                    });
                }
            }
        });
    }

    public interface OnResultListener {
        void onKillSuccessfully(int count);

        void onKillFailed();
    }

    private String getLauncherPkgName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = App.context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String pkgName = "";
        if (resolveInfo != null) {
            pkgName = resolveInfo.activityInfo.packageName;
        }
        return pkgName;
    }

    private String getAlarmPkgName() {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        ResolveInfo resolveInfo = App.context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String pkgName = "";
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            pkgName = resolveInfo.activityInfo.packageName;
        }
        return pkgName;
    }

    private String getDefaultKeyboardPkgName() {
        String defaultIME = Settings.Secure.getString(
                App.context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD
        );
        if (defaultIME != null && defaultIME.contains("/")) {
            return defaultIME.split("/")[0];
        }
        return "";
    }

    private String getDefaultDialerPkgName() {
        TelecomManager telecomManager = (TelecomManager) App.context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            return telecomManager.getDefaultDialerPackage();
        }
        return "";
    }

    private String getDefaultSmsPkgName() {
        return Telephony.Sms.getDefaultSmsPackage(App.context);
    }

    private static final Set<String> CRITICAL_PACKAGES = new HashSet<>(Arrays.asList(

            // === AOSP Core ===
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.providers.downloads",
            "com.android.providers.media",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.providers.settings",

            // === Google Service / MicroG ===
            "com.google.android.gms",
            "com.google.android.gsf",
            "org.microg.gms.droidguard",
            "com.google.android.packageinstaller",

            // === MIUI / HyperOS ===
            "com.miui.securitycenter",
            "com.miui.powerkeeper",
            "com.miui.guardprovider",
            "com.xiaomi.finddevice",
            "com.miui.core",
            "com.miui.home",

            // === One UI ===
            "com.samsung.android.lool",
            "com.samsung.android.securitylogagent",
            "com.samsung.android.biometrics.app.setting",
            "com.samsung.android.incallui",
            "com.samsung.android.sm_cn",

            // === ColorOS / OxygenOS ===
            "com.coloros.safecenter",
            "com.oplus.battery",
            "com.oplus.safe",

            // === EMUI ===
            "com.huawei.systemmanager"
    ));
}