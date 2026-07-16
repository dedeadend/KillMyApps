package dedeadend.killmyapps;

import android.content.pm.PackageManager;

import java.io.OutputStream;
import java.util.List;

import dedeadend.killmyapps.model.AppInfo;
import rikka.shizuku.Shizuku;

public class ShizukuUtils {

    private static Process process = null;

    private static final int SHIZUKU_REQ_CODE = 1001;

    public static void requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.requestPermission(SHIZUKU_REQ_CODE);
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean checkShizuku() {
        try {
            if (!Shizuku.pingBinder())
                return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean killApp(String pkgName) {
        try {
            process = Shizuku.newProcess(new String[]{"am", "force-stop", pkgName}, null, null);
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
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
            process = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream os = process.getOutputStream();
            boolean killMyApps = false;
            for (AppInfo app : appList) {
                String pkg = app.getPkgName();
                if (pkg.equals("dedeadend.killmyapps")) {
                    killMyApps = true;
                    continue;
                }
                String command = "am force-stop " + pkg + "\n";
                os.write(command.getBytes());
            }
            os.write("exit\n".getBytes());
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