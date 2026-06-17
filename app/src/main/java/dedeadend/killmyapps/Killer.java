package dedeadend.killmyapps;

import java.util.List;

import dedeadend.killmyapps.model.AppInfo;

public class Killer {

    private static int killerMode = 1;

    private static boolean checkAccess() {
        killerMode = App.settings.getInt(App.KILLER_MODE, 1);
        if (killerMode == 1)
            return SuUtils.checkSU();
        else if (killerMode == 2) {
            if (!ShizukuUtils.checkShizuku()) {
                ShizukuUtils.requestPermission();
                return false;
            }
            return true;
        } else
            return false;
    }

    public static boolean killListOfApps(List<AppInfo> appList) {
        if (!checkAccess())
            return false;
        if (killerMode == 1) {
            int result = SuUtils.killListOfApps(appList);
            if (result == 0)
                return true;
            else if (result == 1) {
                SuUtils.killMyApps();
                return true;
            } else
                return false;
        } else if (killerMode == 2) {
            int result = ShizukuUtils.killListOfApps(appList);
            if (result == 0)
                return true;
            else if (result == 1) {
                ShizukuUtils.killMyApps();
                return true;
            } else
                return false;
        } else
            return false;
    }

    public static boolean killApp(String pkgName) {
        if (!checkAccess())
            return false;
        if (killerMode == 1)
            return SuUtils.killApp(pkgName);
        else if (killerMode == 2)
            return ShizukuUtils.killApp(pkgName);
        else
            return false;
    }
}
