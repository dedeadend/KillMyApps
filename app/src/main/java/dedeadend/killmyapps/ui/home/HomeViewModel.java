package dedeadend.killmyapps.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.AppListHelper;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<AppInfo>> appsList;

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
                List<AppInfo> apps = AppListHelper.getFilteredAppsList(App.context, false);
                apps.sort(AppInfo::compareTo);

                App.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        appsList.setValue(apps);
                    }
                });
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
}