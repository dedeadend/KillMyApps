package dedeadend.killmyapps.ui.Settings;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.util.AutoKillHelper;

public class SettingsViewModel extends ViewModel {

    //themeMode = 0 -> auto , 1 -> light , 2 -> dark
    //listMode = 0 -> user , 1 -> launcher , 2 -> system
    //selectionMode = 0 -> exclude , 1 -> include
    //killerMode = (0 -> auto) , 1 -> root , 2 -> shizuku , 3 -> shizuku+
    //killLevel = 0 -> soft , 1 -> deep , 2 -> killer

    private final MutableLiveData<Integer> themeMode, killerMode, listMode, selectionMode,
            screenOffAutoKillDelay, scheduledAutoKillTime, killLevel;
    private final MutableLiveData<Boolean> hideKillMyApps, hideDefaultLauncher, hideDefaultAlarm,
            hideDefaultKeyboard, hideDefaultDialer, hideDefaultSMS, hideCriticalSystemApps,
            showAppsPkgName, clickToAppInfo, longClickToMenu, showScrollAnimation,
            screenOffAutoKill, scheduledAutoKill;

    public SettingsViewModel() {
        themeMode = new MutableLiveData<>();
        killerMode = new MutableLiveData<>();
        killLevel = new MutableLiveData<>();
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
        screenOffAutoKill = new MutableLiveData<>();
        screenOffAutoKillDelay = new MutableLiveData<>();
        scheduledAutoKill = new MutableLiveData<>();
        scheduledAutoKillTime = new MutableLiveData<>();
        loadSettings();
    }

    private void loadSettings() {
        themeMode.setValue(App.settings.getInt(App.THEME_MODE, 0));
        killerMode.setValue(App.settings.getInt(App.KILLER_MODE, 1));
        killLevel.setValue(App.settings.getInt(App.KILLER_LEVEL, 1));
        listMode.setValue(App.settings.getInt(App.LIST_MODE, 1));
        selectionMode.setValue(App.settings.getInt(App.SELECTION_MODE, 0));
        hideKillMyApps.setValue(App.settings.getBoolean(App.HIDE_KILL_MY_APPS, true));
        hideDefaultLauncher.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true));
        hideDefaultAlarm.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_ALARM, true));
        hideDefaultKeyboard.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true));
        hideDefaultDialer.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_DIALER, true));
        hideDefaultSMS.setValue(App.settings.getBoolean(App.HIDE_DEFAULT_SMS, true));
        hideCriticalSystemApps.setValue(App.settings.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true));
        showAppsPkgName.setValue(App.settings.getBoolean(App.SHOW_PKG_NAME, true));
        clickToAppInfo.setValue(App.settings.getBoolean(App.CLICK_TO_APP_INFO, true));
        longClickToMenu.setValue(App.settings.getBoolean(App.LONG_CLICK_TO_MENU, true));
        showScrollAnimation.setValue(App.settings.getBoolean(App.SHOW_SCROLL_ANIMATION, true));
        screenOffAutoKill.setValue(App.settings.getBoolean(App.SCREEN_OFF_AUTO_KILL, false));
        screenOffAutoKillDelay.setValue(App.settings.getInt(App.SCREEN_OFF_AUTO_KILL_DELAY, 0));
        scheduledAutoKill.setValue(App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false));

        int hour = App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3);
        int minute = App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30);
        scheduledAutoKillTime.setValue(hour * 2 + (minute >= 30 ? 1 : 0));
    }

    public void reloadSettings() {
        int restoredHour = App.settings.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3);
        int restoredMinute = App.settings.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30);
        int restoredTime = restoredHour * 2 + (restoredMinute >= 30 ? 1 : 0);
        boolean restoredEnabled = App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false);

        Integer previousTime = scheduledAutoKillTime.getValue();
        Boolean previousEnabled = scheduledAutoKill.getValue();
        boolean scheduleChanged = (previousTime != null && previousTime != restoredTime)
                || (previousEnabled != null && previousEnabled != restoredEnabled);
        loadSettings();
        if (scheduleChanged)
            AutoKillHelper.onScheduledSettingsChanged(restoredEnabled);
    }

    public void setThemeMode(int mode) {
        App.settings.edit().putInt(App.THEME_MODE, mode).apply();
        themeMode.setValue(mode);
        App.setAppThemeMode();
    }

    public void setKillerMode(int mode) {
        App.settings.edit().putInt(App.KILLER_MODE, mode).apply();
        killerMode.setValue(mode);
    }

    public void setKillLevel(int level) {
        App.settings.edit().putInt(App.KILLER_LEVEL, level).apply();
        killLevel.setValue(level);
    }

    public void setListMode(int mode) {
        App.settings.edit().putInt(App.LIST_MODE, mode).apply();
        listMode.setValue(mode);
    }

    public void setSelectionMode(int mode) {
        App.settings.edit().putInt(App.SELECTION_MODE, mode).apply();
        selectionMode.setValue(mode);
    }

    public void setHideKillMyApps(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_KILL_MY_APPS, hide).apply();
        hideKillMyApps.setValue(hide);
    }

    public void setHideDefaultLauncher(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_LAUNCHER, hide).apply();
        hideDefaultLauncher.setValue(hide);
    }

    public void setHideDefaultAlarm(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_ALARM, hide).apply();
        hideDefaultAlarm.setValue(hide);
    }

    public void setHideDefaultKeyboard(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_KEYBOARD, hide).apply();
        hideDefaultKeyboard.setValue(hide);
    }

    public void setHideDefaultDialer(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_DIALER, hide).apply();
        hideDefaultDialer.setValue(hide);
    }

    public void setHideDefaultSMS(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_DEFAULT_SMS, hide).apply();
        hideDefaultSMS.setValue(hide);
    }

    public void setHideCriticalSystemApps(boolean hide) {
        App.settings.edit().putBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, hide).apply();
        hideCriticalSystemApps.setValue(hide);
    }

    public void setShowAppsPkgName(boolean show) {
        App.settings.edit().putBoolean(App.SHOW_PKG_NAME, show).apply();
        showAppsPkgName.setValue(show);
    }

    public void setClickToAppInfo(boolean click) {
        App.settings.edit().putBoolean(App.CLICK_TO_APP_INFO, click).apply();
        clickToAppInfo.setValue(click);
    }

    public void setLongClickToMenu(boolean longClick) {
        App.settings.edit().putBoolean(App.LONG_CLICK_TO_MENU, longClick).apply();
        longClickToMenu.setValue(longClick);
    }

    public void setShowScrollAnimation(boolean show) {
        App.settings.edit().putBoolean(App.SHOW_SCROLL_ANIMATION, show).apply();
        showScrollAnimation.setValue(show);
    }

    public void setScreenOffAutoKill(boolean screenOff) {
        App.settings.edit().putBoolean(App.SCREEN_OFF_AUTO_KILL, screenOff).apply();
        screenOffAutoKill.setValue(screenOff);
    }

    public void setScreenOffAutoKillDelay(int delay) {
        App.settings.edit().putInt(App.SCREEN_OFF_AUTO_KILL_DELAY, delay).apply();
        screenOffAutoKillDelay.setValue(delay);
    }

    public void setScheduledAutoKill(boolean isScheduled) {
        App.settings.edit().putBoolean(App.SCHEDULED_AUTO_KILL, isScheduled).apply();
        AutoKillHelper.onScheduledSettingsChanged(isScheduled);
        scheduledAutoKill.setValue(isScheduled);
    }

    public void setScheduledAutoKillTime(int time) {
        int hour = time / 2;
        int minute = (time % 2 == 0) ? 0 : 30;
        App.settings.edit()
                .putInt(App.SCHEDULED_AUTO_KILL_HOUR, hour)
                .putInt(App.SCHEDULED_AUTO_KILL_MINUTE, minute)
                .apply();
        if (App.settings.getBoolean(App.SCHEDULED_AUTO_KILL, false))
            AutoKillHelper.onScheduledSettingsChanged(true);
        scheduledAutoKillTime.setValue(time);
    }

    public MutableLiveData<Integer> getThemeMode() {
        return themeMode;
    }

    public MutableLiveData<Integer> getKillerMode() {
        return killerMode;
    }

    public MutableLiveData<Integer> getKillLevel() {
        return killLevel;
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

    public MutableLiveData<Boolean> getScreenOffAutoKill() {
        return screenOffAutoKill;
    }

    public MutableLiveData<Integer> getScreenOffAutoKillDelay() {
        return screenOffAutoKillDelay;
    }

    public MutableLiveData<Boolean> getScheduledAutoKill() {
        return scheduledAutoKill;
    }

    public MutableLiveData<Integer> getScheduledAutoKillTime() {
        return scheduledAutoKillTime;
    }
}