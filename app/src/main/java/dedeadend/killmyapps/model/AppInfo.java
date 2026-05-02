package dedeadend.killmyapps.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {
    final private String name, pkgName;
    final private Drawable icon;

    AppInfo(Context context, ApplicationInfo applicationInfo) {
        name = applicationInfo.loadLabel(context.getPackageManager()).toString();
        pkgName = applicationInfo.packageName;
        icon = applicationInfo.loadIcon(context.getPackageManager());
    }

    AppInfo(Context context, ResolveInfo resolveInfo) {
        name = resolveInfo.loadLabel(context.getPackageManager()).toString();
        pkgName = resolveInfo.activityInfo.packageName;
        icon = resolveInfo.loadIcon(context.getPackageManager());
    }

    public String getName() {
        return name;
    }

    public String getPkgName() {
        return pkgName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int compareTo(AppInfo b) {
        return name.toLowerCase().compareTo(b.getName().toLowerCase());
    }

    public static class utils {
        public static List<AppInfo> applicationInfoList2AppInfoList(Context context, List<ApplicationInfo> applicationsInfo) {
            List<AppInfo> appsList = new ArrayList<>();
            for (ApplicationInfo applicationInfo : applicationsInfo) {
                appsList.add(new AppInfo(context, applicationInfo));
            }
            return appsList;
        }

        public static List<AppInfo> resolveInfoList2AppInfoList(Context context, List<ResolveInfo> resolvesInfo) {
            List<AppInfo> appsList = new ArrayList<>();
            for (ResolveInfo resolveInfo : resolvesInfo) {
                appsList.add(new AppInfo(context, resolveInfo));
            }
            return appsList;
        }
    }
}
