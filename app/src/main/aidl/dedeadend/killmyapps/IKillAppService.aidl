package dedeadend.killmyapps;

interface IKillAppService {

    boolean killApp(String pkgName);

    int killListOfApps(in List<String> packages);
}