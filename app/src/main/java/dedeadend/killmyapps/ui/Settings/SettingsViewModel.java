package dedeadend.killmyapps.ui.Settings;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dedeadend.killmyapps.App;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<Integer> themeMode, killerMode, listMode;
    //themeMode = 0 -> auto , 1 -> light , 2 -> dark
    //listMode = 0 -> user , 1 -> launcher , 2 -> system
    //killerMode = (0 -> auto) , 1 -> root , 2 -> shizuku
    private final MutableLiveData<Boolean> hideKillMyApps, hideDefaultLauncher, hideSystemUI,
            showAppsPkgName, clickToAppInfo, longClickToCopy;

    public SettingsViewModel() {
        themeMode = new MutableLiveData<>();
        killerMode = new MutableLiveData<>();
        listMode = new MutableLiveData<>();
        hideKillMyApps = new MutableLiveData<>();
        hideDefaultLauncher = new MutableLiveData<>();
        hideSystemUI = new MutableLiveData<>();
        showAppsPkgName = new MutableLiveData<>();
        clickToAppInfo = new MutableLiveData<>();
        longClickToCopy = new MutableLiveData<>();
        loadSettings();
    }

    private void loadSettings() {
        themeMode.setValue(App.settings.getInt(App.THEME_MODE, 0));
        killerMode.setValue(App.settings.getInt(App.KILLER_MODE, 1));
        listMode.setValue(App.settings.getInt(App.LIST_MODE, 1));
        hideKillMyApps.setValue(App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true));
        hideDefaultLauncher.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true));
        hideSystemUI.setValue(App.settings.getBoolean(App.HIDE_SYSTEM_UI, true));
        showAppsPkgName.setValue(App.settings.getBoolean(App.SHOW_PKGNAME, true));
        clickToAppInfo.setValue(App.settings.getBoolean(App.CLICK_TO_APP_INFO, true));
        longClickToCopy.setValue(App.settings.getBoolean(App.LONG_CLICK_TO_COPY, true));
    }

    public void setThemeMode(int mode) {
        themeMode.setValue(mode);
        App.settings.edit().putInt(App.THEME_MODE, mode).apply();
        App.setAppThemeMode();
    }

    public void setKillerMode(int mode) {
        killerMode.setValue(mode);
        App.settings.edit().putInt(App.KILLER_MODE, mode).apply();
    }

    public void setListMode(int mode) {
        listMode.setValue(mode);
        App.settings.edit().putInt(App.LIST_MODE, mode).apply();
    }

    public void setHideKillMyApps(boolean hide) {
        hideKillMyApps.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_KILL_MY_APPS, hide).apply();
    }

    public void setHideDefaultLauncher(boolean hide) {
        hideDefaultLauncher.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_LAUNCHER, hide).apply();
    }

    public void setHideSystemUI(boolean hide) {
        hideSystemUI.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_SYSTEM_UI, hide).apply();
    }

    public void setShowAppsPkgName(boolean show) {
        showAppsPkgName.setValue(show);
        App.settings.edit().putBoolean(App.SHOW_PKGNAME, show).apply();
    }

    public void setClickToAppInfo(boolean click) {
        clickToAppInfo.setValue(click);
        App.settings.edit().putBoolean(App.CLICK_TO_APP_INFO, click).apply();
    }

    public void setLongClickToCopy(boolean longClick) {
        longClickToCopy.setValue(longClick);
        App.settings.edit().putBoolean(App.LONG_CLICK_TO_COPY, longClick).apply();
    }

    public MutableLiveData<Integer> getThemeMode() {
        return themeMode;
    }

    public MutableLiveData<Integer> getKillerMode() {
        return killerMode;
    }

    public MutableLiveData<Integer> getListMode() {
        return listMode;
    }

    public MutableLiveData<Boolean> getHideKillMyApps() {
        return hideKillMyApps;
    }

    public MutableLiveData<Boolean> getHideDefaultLauncher() {
        return hideDefaultLauncher;
    }

    public MutableLiveData<Boolean> getHideSystemUI() {
        return hideSystemUI;
    }

    public MutableLiveData<Boolean> getShowAppsPkgName() {
        return showAppsPkgName;
    }

    public MutableLiveData<Boolean> getClickToAppInfo() {
        return clickToAppInfo;
    }

    public MutableLiveData<Boolean> getLongClickToCopy() {
        return longClickToCopy;
    }
}