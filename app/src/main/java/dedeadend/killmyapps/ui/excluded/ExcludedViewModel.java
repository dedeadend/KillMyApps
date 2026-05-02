package dedeadend.killmyapps.ui.excluded;

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

public class ExcludedViewModel extends ViewModel {

    private final MutableLiveData<List<AppInfo>> appsList;
    private final MutableLiveData<List<AppInfo>> excludedList;

    public ExcludedViewModel() {
        appsList = new MutableLiveData<>();
        excludedList = new MutableLiveData<>();
        appsList.setValue(new ArrayList<>());
    }

    public void refreshList() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int listMode = App.settings.getInt(App.LIST_MODE, 1);
                if (listMode == 0)
                    getUserAppsList(App.context);
                else if (listMode == 1)
                    getLauncherAppsList(App.context);
                else if (listMode == 2)
                    getSystemAppsList(App.context);
                getExcludedAppsList();
            }
        });
    }

    public void clearList() {
        appsList.setValue(new ArrayList<>());
        excludedList.setValue(new ArrayList<>());
    }

    public void addExcluded(AppInfo appInfo) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                PKGName newExcludedItem = new PKGName();
                newExcludedItem.name = appInfo.getPkgName();
                App.database.excludedPkgDao().insert(newExcludedItem);
            }
        });
    }

    public void removeExcluded(AppInfo appInfo) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                PKGName newExcludedItem = new PKGName();
                newExcludedItem.name = appInfo.getPkgName();
                App.database.excludedPkgDao().delete(newExcludedItem);
            }
        });
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
        return false;
    }

    private void getSystemAppsList(Context context) {
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < applications.size(); i++) {
            if (filterList(applications, i))
                i--;
        }
        List<AppInfo> temp = AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
        temp.sort(AppInfo::compareTo);
        appsList.getValue().clear();
        appsList.getValue().addAll(temp);
    }

    private void getUserAppsList(Context context) {
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < applications.size(); i++) {
            if ((applications.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                applications.remove(i);
                i--;
                continue;
            }
            if (filterList(applications, i))
                i--;
        }
        List<AppInfo> temp = AppInfo.utils.applicationInfoList2AppInfoList(context, applications);
        temp.sort(AppInfo::compareTo);
        appsList.getValue().clear();
        appsList.getValue().addAll(temp);
    }

    private void getLauncherAppsList(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<AppInfo> temp = AppInfo.utils.resolveInfoList2AppInfoList(context, pm.queryIntentActivities(intent, 0));
        temp.sort(AppInfo::compareTo);
        appsList.getValue().clear();
        appsList.getValue().addAll(temp);
    }

    private void getExcludedAppsList() {
        List<AppInfo> temp = new ArrayList<>();
        List<PKGName> pkgs = App.database.excludedPkgDao().getAll();
        for (int i = 0; i < pkgs.size(); i++) {
            for (int j = 0; j < appsList.getValue().size(); j++) {
                if (pkgs.get(i).name.equals(appsList.getValue().get(j).getPkgName())) {
                    temp.add(appsList.getValue().get(j));
                    appsList.getValue().remove(j);
                    break;
                }
            }
        }
        temp.sort(AppInfo::compareTo);
        App.handler.post(new Runnable() {
            @Override
            public void run() {
                excludedList.setValue(temp);
            }
        });
    }

    public MutableLiveData<List<AppInfo>> getAppsList() {
        return appsList;
    }

    public MutableLiveData<List<AppInfo>> getExcludedList() {
        return excludedList;
    }
}
