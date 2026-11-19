package dedeadend.killmyapps.shortcut;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.util.QuickActionsHelper;

public class ShortcutActivity extends Activity {

    public static final String ACTION_KILL = "dedeadend.killmyapps.action.SHORTCUT_KILL";
    public static final String ACTION_SLEEP = "dedeadend.killmyapps.action.SHORTCUT_SLEEP";
    public static final String ACTION_COMBO = "dedeadend.killmyapps.action.SHORTCUT_COMBO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisible(false);

        String intentAction = getIntent() != null ? getIntent().getAction() : null;
        int action;
        if (ACTION_KILL.equals(intentAction))
            action = QuickActionsHelper.ACTION_KILL;
        else if (ACTION_SLEEP.equals(intentAction))
            action = QuickActionsHelper.ACTION_SLEEP;
        else if (ACTION_COMBO.equals(intentAction)) {
            action = QuickActionsHelper.ACTION_COMBO;
        }
        else {
            finish();
            return;
        }

        QuickActionsHelper.runAsync(action, result -> {
            Toast.makeText(App.context, result.status, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
