package dedeadend.killmyapps.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.model.PKGName;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<AppInfo>> appsList;
    private List<PKGName> excludedlist;

    public HomeViewModel() {
        appsList = new MutableLiveData<>();
    }

    public void refreshList() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                excludedlist = App.database.excludedPkgDao().getAll();
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
        int size = appsList.getValue().size();
        appsList.setValue(new ArrayList<>());
        return size;
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

    private boolean filterList(List<ApplicationInfo> applications, int i) {
        boolean hideKillMyApps = App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true);
        boolean hideDefaultLauncher = App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true);
        boolean hideSystemUI = App.settings.getBoolean(App.HIDE_SYSTEM_UI, true);
        if (hideKillMyApps) {
            if (applications.get(i).packageName.equals("dedeadend.killmyapps")) {
                applications.remove(i);
                return true;
            }
        }
        if (hideDefaultLauncher) {
            if (applications.get(i).packageName.equals(getLauncherPkgName())) {
                applications.remove(i);
                return true;
            }
        }
        if (hideSystemUI) {
            if ((applications.get(i).packageName.equals("com.android.systemui"))) {
                applications.remove(i);
                return true;
            }
        }
        for (int j = 0; j < excludedlist.size(); j++) {
            if (applications.get(i).packageName.equals(excludedlist.get(j).name)) {
                applications.remove(i);
                return true;
            }
        }
        return false;
    }

    private void getSystemAppsList(Context context) {
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < applications.size(); i++) {
            if ((applications.get(i).flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                applications.remove(i);
                i--;
                continue;
            }
            if (filterList(applications, i))
                i--;
        }
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
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < applications.size(); i++) {
            if (((applications.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) ||
                    (applications.get(i).flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                applications.remove(i);
                i--;
                continue;
            }
            if (filterList(applications, i))
                i--;
        }
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
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> launcherApps = pm.queryIntentActivities(intent, 0);
        List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < applications.size(); i++) {
            if ((applications.get(i).flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                applications.remove(i);
                i--;
                continue;
            }
            boolean isLauncherApp = false;
            for (int j = 0; j < launcherApps.size(); j++) {
                if (applications.get(i).packageName.equals(launcherApps.get(j).activityInfo.packageName)) {
                    isLauncherApp = true;
                    launcherApps.remove(j);
                    break;
                }
            }
            if (!isLauncherApp) {
                applications.remove(i);
                i--;
                continue;
            }
            if (filterList(applications, i))
                i--;
        }
        List<AppInfo> temp = AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
        temp.sort(AppInfo::compareTo);
        App.handler.post(new Runnable() {
            @Override
            public void run() {
                appsList.setValue(temp);
            }
        });
    }

    public MutableLiveData<List<AppInfo>> getAppsList() {
        return appsList;
    }
}