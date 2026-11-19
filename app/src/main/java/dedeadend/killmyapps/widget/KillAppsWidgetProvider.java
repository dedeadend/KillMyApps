package dedeadend.killmyapps.widget;

import dedeadend.killmyapps.R;
import dedeadend.killmyapps.util.QuickActionsHelper;

public class KillAppsWidgetProvider extends BaseWidgetProvider {

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_kill_apps;
    }

    @Override
    protected String getIdleStatus() {
        return "Ready to Kill";
    }

    @Override
    protected int[] getActionViewIds() {
        return new int[]{R.id.widget_action_kill};
    }

    @Override
    protected int[] getActions() {
        return new int[]{QuickActionsHelper.ACTION_KILL};
    }

    @Override
    protected int getStatusViewId() {
        return R.id.widget_status;
    }

    @Override
    protected int getOpenAppViewId() {
        return R.id.widget_container;
    }

    @Override
    protected int getIdleIconViewId() {
        return R.id.widget_icon_run;
    }

    @Override
    protected int getSuccessIconViewId() {
        return R.id.widget_icon_success;
    }

    @Override
    protected int getFailureIconViewId() {
        return R.id.widget_icon_fail;
    }
}
