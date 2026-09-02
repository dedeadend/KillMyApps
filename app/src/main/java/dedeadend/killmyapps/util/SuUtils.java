package dedeadend.killmyapps.util;

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

    public static boolean killApp(String pkgName, int level) {
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            appendKillCommands(os, pkgName, level);

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

    public static int killListOfApps(List<AppInfo> appList, int level) {
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            boolean killMyApps = false;

            for (AppInfo app : appList) {
                String pkgName = app.getPkgName();
                if (pkgName.equals("dedeadend.killmyapps")) {
                    killMyApps = true;
                    continue;
                }
                appendKillCommands(os, pkgName, level);
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

    private static void appendKillCommands(DataOutputStream os, String pkgName, int level) throws IOException {
        switch (level) {
            case 0:
                os.writeBytes("am kill " + pkgName + "\n");
                os.writeBytes("am set-inactive " + pkgName + " true\n");
                break;

            case 2:
                os.writeBytes("am force-stop " + pkgName + "\n");
                os.writeBytes("cmd package suspend " + pkgName + " 2>/dev/null && cmd package unsuspend " + pkgName + " 2>/dev/null\n");
                break;

            case 1:
            default:
                os.writeBytes("am force-stop " + pkgName + "\n");
                break;
        }
    }
}