package dedeadend.killmyapps.ui.excluded;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.model.PKGName;
import dedeadend.killmyapps.util.AppListUtils;

public class ExcludedViewModel extends ViewModel {

    private final MutableLiveData<List<AppInfo>> appsList;
    private final MutableLiveData<List<AppInfo>> excludedList;

    public ExcludedViewModel() {
        appsList = new MutableLiveData<>();
        excludedList = new MutableLiveData<>();
        appsList.setValue(new ArrayList<>());
    }

    public MutableLiveData<List<AppInfo>> getAppsList() {
        return appsList;
    }

    public MutableLiveData<List<AppInfo>> getExcludedList() {
        return excludedList;
    }

    public void refreshList() {
        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<AppInfo> apps = AppListUtils.getAppsList(App.context, false);
                apps.sort(AppInfo::compareTo);

                List<AppInfo> exApps = new ArrayList<>();
                List<PKGName> pkgs = App.database.excludedPkgDao().getAll();
                Map<String, AppInfo> appsListMap = new HashMap<>();
                for (AppInfo appInfo : apps)
                    appsListMap.put(appInfo.getPkgName(), appInfo);
                for (int i = 0; i < pkgs.size(); i++) {
                    if (appsListMap.containsKey(pkgs.get(i).name)) {
                        exApps.add(appsListMap.get(pkgs.get(i).name));
                        apps.remove(appsListMap.get(pkgs.get(i).name));
                    }
                }
                exApps.sort(AppInfo::compareTo);

                App.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        appsList.setValue(apps);
                        excludedList.setValue(exApps);
                    }
                });
            }
        });
    }

    public void clearList() {
        appsList.setValue(new ArrayList<>());
        excludedList.setValue(new ArrayList<>());
    }

    public void addExcluded(AppInfo appInfo) {
        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                PKGName newExcludedItem = new PKGName();
                newExcludedItem.name = appInfo.getPkgName();
                App.database.excludedPkgDao().insert(newExcludedItem);
            }
        });
    }

    public void removeExcluded(AppInfo appInfo) {
        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                PKGName newExcludedItem = new PKGName();
                newExcludedItem.name = appInfo.getPkgName();
                App.database.excludedPkgDao().delete(newExcludedItem);
            }
        });
    }
}
