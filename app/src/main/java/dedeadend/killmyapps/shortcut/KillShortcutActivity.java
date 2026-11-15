package dedeadend.killmyapps.shortcut;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.AppListHelper;

public class KillShortcutActivity extends Activity {

    public static final String ACTION_KILL_APPS = "dedeadend.killmyapps.action.SHORTCUT_KILL_APPS";
    public static final String ACTION_KILL_APPS_AND_SLEEP = "dedeadend.killmyapps.action.SHORTCUT_KILL_APPS_AND_SLEEP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisible(false);

        String action = getIntent() != null ? getIntent().getAction() : null;
        if (!ACTION_KILL_APPS.equals(action) && !ACTION_KILL_APPS_AND_SLEEP.equals(action)) {
            finish();
            return;
        }

        App.executorService.execute(() -> {
            try {
                List<AppInfo> apps = AppListHelper.getFilteredAppsList(App.context, true);
                if (apps.isEmpty()) {
                    App.handler.post(() -> {
                        Toast.makeText(App.context, "All Dead", Toast.LENGTH_SHORT).show();
                    });
                    if (action.equals(ACTION_KILL_APPS_AND_SLEEP))
                        Killer.sleep();
                } else if (Killer.killListOfApps(apps)) {
                    App.handler.post(() -> {
                        String message = apps.size() == 1 ? "1 app is dead" : apps.size() + " apps are dead";
                        Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show();
                    });
                    if (action.equals(ACTION_KILL_APPS_AND_SLEEP))
                        Killer.sleep();
                } else
                    App.handler.post(() -> {
                        Toast.makeText(App.context, "Failed", Toast.LENGTH_SHORT).show();
                    });
            } catch (Exception ignored) {
            } finally {
                App.handler.post(this::finish);
            }
        });
    }
}
