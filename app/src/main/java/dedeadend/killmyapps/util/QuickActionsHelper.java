package dedeadend.killmyapps.util;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;

public class QuickActionsHelper {

    public static final int ACTION_KILL = 1;
    public static final int ACTION_SLEEP = 2;
    public static final int ACTION_COMBO = 3;

    public static final String STATUS_ALL_DEAD = "All Dead";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_SLEPT = "Slept";
    public static final String STATUS_KILLING = "Killing...";
    public static final String STATUS_SLEEPING = "Sleeping...";

    public interface Callback {
        void onComplete(Result result);
    }

    public static class Result {
        public boolean success;
        public String status;

        public Result(boolean success, String status) {
            this.success = success;
            this.status = status;
        }
    }

    public static void runAsync(int action, Callback callback) {
        App.executorService.execute(() -> {
            Result result = null;
            try {
                result = execute(action);
            } catch (Exception exception) {
                result = new Result(false, STATUS_FAILED);
            }
            finally {
                if (callback != null) {
                    final Result finalResult = result != null ? result : new Result(false, STATUS_FAILED);
                    App.handler.post(() -> callback.onComplete(finalResult));
                }
            }
        });
    }

    private static Result execute(int action) {
        if (action != ACTION_KILL && action != ACTION_SLEEP && action != ACTION_COMBO)
            return new Result(false, STATUS_FAILED);
        if (action == ACTION_KILL || action == ACTION_COMBO) {
            List<AppInfo> apps = AppListHelper.getFilteredAppsList(App.context, true);
            if (!Killer.killListOfApps(apps))
                return new Result(false, STATUS_FAILED);
            if (action == ACTION_KILL)
                return new Result(true, getDeadMessage(apps.size()));
        }
        return Killer.sleep() ? new Result(true, STATUS_SLEPT) : new Result(false, STATUS_FAILED);
    }

    private static String getDeadMessage(int count) {
        if (count == 0)
            return STATUS_ALL_DEAD;
        return count + " Dead";
    }
}
