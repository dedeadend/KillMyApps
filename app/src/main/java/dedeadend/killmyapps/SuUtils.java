package dedeadend.killmyapps;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import dedeadend.killmyapps.model.AppInfo;

public class SuUtils {

    private static Process process = null;

    public static boolean checkSU() {
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.getInputStream().close();
            process.getErrorStream().close();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        } finally {
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }

    public static boolean killApp(String pkgName) {
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("am force-stop " + pkgName + "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.getInputStream().close();
            process.getErrorStream().close();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        } finally {
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }

    public static int killListOfApps(List<AppInfo> appList) {
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            boolean killMyApps = false;
            for (AppInfo app : appList) {
                if (app.getPkgName().equals("dedeadend.killmyapps")) {
                    killMyApps = true;
                    continue;
                }
                os.writeBytes("am force-stop " + app.getPkgName() + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.getInputStream().close();
            process.getErrorStream().close();
            process.waitFor();
            return killMyApps ? 1 : 0;
        } catch (Exception e) {
            return -1;
        } finally {
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }
}
