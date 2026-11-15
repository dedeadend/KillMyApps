package dedeadend.killmyapps;

interface IKillAppService {

    boolean sleep();

    boolean killApp(String pkgName, int level);

    int killListOfApps(in List<String> packages, int level);
}