package dedeadend.killmyapps.service;

import java.io.OutputStream;
import java.util.List;

import dedeadend.killmyapps.IKillAppService;

public class UserService extends IKillAppService.Stub {

    @Override
    public boolean killApp(String pkgName) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"am", "force-stop", pkgName});
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    @Override
    public int killListOfApps(List<String> packages) {
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
                os.write(("am force-stop " + pkg + "\n").getBytes());
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
}