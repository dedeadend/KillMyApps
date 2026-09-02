package dedeadend.killmyapps.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import dedeadend.killmyapps.IKillAppService;

public class ShizukuUserService extends IKillAppService.Stub {

    @Override
    public boolean killApp(String pkgName, int level) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"sh"});
            OutputStream os = process.getOutputStream();

            appendKillCommands(os, pkgName, level);

            os.write("exit\n".getBytes());
            os.flush();
            os.close();
            process.getInputStream().close();
            process.getErrorStream().close();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    @Override
    public int killListOfApps(List<String> packages, int level) {
        if (packages == null || packages.isEmpty())
            return 0;

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"sh"});
            OutputStream os = process.getOutputStream();
            boolean killMyApps = false;

            for (String pkg : packages) {
                if ("dedeadend.killmyapps".equals(pkg)) {
                    killMyApps = true;
                    continue;
                }
                appendKillCommands(os, pkg, level);
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
            }
        }
    }

    private static void appendKillCommands(OutputStream os, String pkgName, int level) throws IOException {
        switch (level) {
            case 0:
                os.write(("am kill " + pkgName + "\n").getBytes());
                os.write(("am set-inactive " + pkgName + " true\n").getBytes());
                break;

            case 2:
                os.write(("am force-stop " + pkgName + "\n").getBytes());
                os.write(("cmd package suspend " + pkgName + " 2>/dev/null && cmd package unsuspend " + pkgName + " 2>/dev/null\n").getBytes());
                break;

            case 1:
            default:
                os.write(("am force-stop " + pkgName + "\n").getBytes());
                break;
        }
    }
}