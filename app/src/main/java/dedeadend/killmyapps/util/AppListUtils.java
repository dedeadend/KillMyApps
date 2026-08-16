package dedeadend.killmyapps.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.provider.Telephony;
import android.telecom.TelecomManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.model.PKGName;

public class AppListUtils {
    public static List<AppInfo> getAppsList(Context context, boolean forceHideKillMyApps) {
        int listMode = App.settings.getInt(App.LIST_MODE, 1);
        boolean hideKillMyApps = App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true) || forceHideKillMyApps;
        boolean hideDefaultLauncher = App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true);
        boolean hideDefaultAlarm = App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true);
        boolean hideDefaultKeyboard = App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true);
        boolean hideDefaultDialer = App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true);
        boolean hideDefaultSms = App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true);
        boolean hideCriticalPackages = App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true);

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        String launcherPkg = hideDefaultLauncher ? getLauncherPkgName(context) : "";
        String alarmPkg = hideDefaultAlarm ? getAlarmPkgName(context) : "";
        String keyboardPkg = hideDefaultKeyboard ? getDefaultKeyboardPkgName(context) : "";
        String dialerPkg = hideDefaultDialer ? getDefaultDialerPkgName(context) : "";
        String smsPkg = hideDefaultSms ? getDefaultSmsPkgName(context) : "";

        Set<String> launcherPkgNames = new HashSet<>();
        if (listMode == 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launcherApps = pm.queryIntentActivities(intent, 0);
            for (ResolveInfo resolveInfo : launcherApps) {
                launcherPkgNames.add(resolveInfo.activityInfo.packageName);
            }
        }

        applications.removeIf(app -> {
            String pkgName = app.packageName;
            boolean shouldRemove = false;
            if (listMode == 0)
                shouldRemove = (app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
            else if (listMode == 1)
                shouldRemove = !launcherPkgNames.contains(pkgName);

            return shouldRemove
                    || (hideCriticalPackages && CRITICAL_PACKAGES.contains(pkgName))
                    || (hideDefaultLauncher && pkgName.equals(launcherPkg))
                    || (hideDefaultAlarm && pkgName.equals(alarmPkg))
                    || (hideDefaultKeyboard && pkgName.equals(keyboardPkg))
                    || (hideDefaultDialer && pkgName.equals(dialerPkg))
                    || (hideDefaultSms && pkgName.equals(smsPkg))
                    || (hideKillMyApps && pkgName.equals("dedeadend.killmyapps"));
        });

        return AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
    }

    public static List<AppInfo> getFilteredAppsList(Context context, boolean forceHideKillMyApps) {
        List<PKGName> excludedList = App.database.excludedPkgDao().getAll();
        Set<String> selectionListHash = new HashSet<>();
        for (PKGName pkgName : excludedList)
            selectionListHash.add(pkgName.name);

        int listMode = App.settings.getInt(App.LIST_MODE, 1);
        int selectionMode = App.settings.getInt(App.SELECTION_MODE, 0);
        boolean hideKillMyApps = App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true) || forceHideKillMyApps;
        boolean hideDefaultLauncher = App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true);
        boolean hideDefaultAlarm = App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true);
        boolean hideDefaultKeyboard = App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true);
        boolean hideDefaultDialer = App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true);
        boolean hideDefaultSms = App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true);
        boolean hideCriticalPackages = App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true);

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        String launcherPkg = hideDefaultLauncher ? getLauncherPkgName(context) : "";
        String alarmPkg = hideDefaultAlarm ? getAlarmPkgName(context) : "";
        String keyboardPkg = hideDefaultKeyboard ? getDefaultKeyboardPkgName(context) : "";
        String dialerPkg = hideDefaultDialer ? getDefaultDialerPkgName(context) : "";
        String smsPkg = hideDefaultSms ? getDefaultSmsPkgName(context) : "";

        Set<String> launcherPkgNames = new HashSet<>();
        if (listMode == 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launcherApps = pm.queryIntentActivities(intent, 0);
            for (ResolveInfo resolveInfo : launcherApps) {
                launcherPkgNames.add(resolveInfo.activityInfo.packageName);
            }
        }

        applications.removeIf(app -> {
            String pkgName = app.packageName;
            boolean shouldRemove = (app.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED;
            if (listMode == 0)
                shouldRemove = shouldRemove || (app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
            else if (listMode == 1)
                shouldRemove = shouldRemove || !launcherPkgNames.contains(pkgName);

            return shouldRemove
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

        return AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
    }

    public static String getLauncherPkgName(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null ? resolveInfo.activityInfo.packageName : "";
    }

    public static String getAlarmPkgName(Context context) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (resolveInfo != null && resolveInfo.activityInfo != null) ? resolveInfo.activityInfo.packageName : "";
    }

    public static String getDefaultKeyboardPkgName(Context context) {
        String defaultIME = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD
        );
        if (defaultIME != null && defaultIME.contains("/")) {
            return defaultIME.split("/")[0];
        }
        return "";
    }

    public static String getDefaultDialerPkgName(Context context) {
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        return telecomManager != null ? telecomManager.getDefaultDialerPackage() : "";
    }

    public static String getDefaultSmsPkgName(Context context) {
        return Telephony.Sms.getDefaultSmsPackage(context);
    }

    public static final Set<String> CRITICAL_PACKAGES = new HashSet<>(Arrays.asList(
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
