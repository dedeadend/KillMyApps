package dedeadend.killmyapps;

interface IKillAppService {

    boolean sleep() = 1;

    boolean killApp(String pkgName, int level) = 2;

    int killListOfApps(in List<String> packages, int level) = 3;

    void destroy() = 16777114;
}