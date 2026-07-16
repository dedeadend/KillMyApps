package dedeadend.killmyapps.ui.Settings;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dedeadend.killmyapps.App;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<Integer> themeMode, killerMode, listMode, selectionMode;
    //themeMode = 0 -> auto , 1 -> light , 2 -> dark
    //listMode = 0 -> user , 1 -> launcher , 2 -> system
    //selectionMode = 0 -> exclude , 1 -> include
    //killerMode = (0 -> auto) , 1 -> root , 2 -> shizuku
    private final MutableLiveData<Boolean> hideKillMyApps, hideDefaultLauncher, hideDefaultAlarm,
            hideDefaultKeyboard, hideDefaultDialer, hideDefaultSMS, hideCriticalSystemApps,
            showAppsPkgName, clickToAppInfo, longClickToMenu, showScrollAnimation;

    public SettingsViewModel() {
        themeMode = new MutableLiveData<>();
        killerMode = new MutableLiveData<>();
        listMode = new MutableLiveData<>();
        selectionMode = new MutableLiveData<>();
        hideKillMyApps = new MutableLiveData<>();
        hideDefaultLauncher = new MutableLiveData<>();
        hideDefaultAlarm = new MutableLiveData<>();
        hideDefaultKeyboard = new MutableLiveData<>();
        hideDefaultDialer = new MutableLiveData<>();
        hideDefaultSMS = new MutableLiveData<>();
        hideCriticalSystemApps = new MutableLiveData<>();
        showAppsPkgName = new MutableLiveData<>();
        clickToAppInfo = new MutableLiveData<>();
        longClickToMenu = new MutableLiveData<>();
        showScrollAnimation = new MutableLiveData<>();
        loadSettings();
    }

    private void loadSettings() {
        themeMode.setValue(App.settings.getInt(App.THEME_MODE, 0));
        killerMode.setValue(App.settings.getInt(App.KILLER_MODE, 1));
        listMode.setValue(App.settings.getInt(App.LIST_MODE, 1));
        selectionMode.setValue(App.settings.getInt(App.SELECTION_MODE, 0));
        hideKillMyApps.setValue(App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true));
        hideDefaultLauncher.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true));
        hideDefaultAlarm.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true));
        hideDefaultKeyboard.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true));
        hideDefaultDialer.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true));
        hideDefaultSMS.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true));
        hideCriticalSystemApps.setValue(App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true));
        showAppsPkgName.setValue(App.settings.getBoolean(App.SHOW_PKGNAME, true));
        clickToAppInfo.setValue(App.settings.getBoolean(App.CLICK_TO_APP_INFO, true));
        longClickToMenu.setValue(App.settings.getBoolean(App.LONG_CLICK_TO_MENU, true));
        showScrollAnimation.setValue(App.settings.getBoolean(App.SHOW_SCROLL_ANIMATION, true));
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

    public void setSelectionMode(int mode) {
        selectionMode.setValue(mode);
        App.settings.edit().putInt(App.SELECTION_MODE, mode).apply();
    }

    public void setHideKillMyApps(boolean hide) {
        hideKillMyApps.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_KILL_MY_APPS, hide).apply();
    }

    public void setHideDefaultLauncher(boolean hide) {
        hideDefaultLauncher.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_LAUNCHER, hide).apply();
    }

    public void setHideDefaultAlarm(boolean hide) {
        hideDefaultAlarm.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_ALARM, hide).apply();
    }

    public void setHideDefaultKeyboard(boolean hide) {
        hideDefaultKeyboard.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_KEYBOARD, hide).apply();
    }

    public void setHideDefaultDialer(boolean hide) {
        hideDefaultDialer.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_DIALER, hide).apply();
    }

    public void setHideDefaultSMS(boolean hide) {
        hideDefaultSMS.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_SMS, hide).apply();
    }

    public void setHideCriticalSystemApps(boolean hide) {
        hideCriticalSystemApps.setValue(hide);
        App.settings.edit().putBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, hide).apply();
    }

    public void setShowAppsPkgName(boolean show) {
        showAppsPkgName.setValue(show);
        App.settings.edit().putBoolean(App.SHOW_PKGNAME, show).apply();
    }

    public void setClickToAppInfo(boolean click) {
        clickToAppInfo.setValue(click);
        App.settings.edit().putBoolean(App.CLICK_TO_APP_INFO, click).apply();
    }

    public void setLongClickToMenu(boolean longClick) {
        longClickToMenu.setValue(longClick);
        App.settings.edit().putBoolean(App.LONG_CLICK_TO_MENU, longClick).apply();
    }

    public void setShowScrollAnimation(boolean show) {
        showScrollAnimation.setValue(show);
        App.settings.edit().putBoolean(App.SHOW_SCROLL_ANIMATION, show).apply();
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

    public MutableLiveData<Integer> getSelectionMode() {
        return selectionMode;
    }

    public MutableLiveData<Boolean> getHideKillMyApps() {
        return hideKillMyApps;
    }

    public MutableLiveData<Boolean> getHideDefaultLauncher() {
        return hideDefaultLauncher;
    }

    public MutableLiveData<Boolean> getHideDefaultAlarm() {
        return hideDefaultAlarm;
    }

    public MutableLiveData<Boolean> getHideDefaultKeyboard() {
        return hideDefaultKeyboard;
    }

    public MutableLiveData<Boolean> getHideDefaultDialer() {
        return hideDefaultDialer;
    }

    public MutableLiveData<Boolean> getHideDefaultSMS() {
        return hideDefaultSMS;
    }

    public MutableLiveData<Boolean> getHideCriticalSystemApps() {
        return hideCriticalSystemApps;
    }

    public MutableLiveData<Boolean> getShowAppsPkgName() {
        return showAppsPkgName;
    }

    public MutableLiveData<Boolean> getClickToAppInfo() {
        return clickToAppInfo;
    }

    public MutableLiveData<Boolean> getLongClickToMenu() {
        return longClickToMenu;
    }

    public MutableLiveData<Boolean> getShowScrollAnimation() {
        return showScrollAnimation;
    }
}