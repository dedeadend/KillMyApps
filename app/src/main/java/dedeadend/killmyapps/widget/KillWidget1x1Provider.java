package dedeadend.killmyapps.widget;

import dedeadend.killmyapps.R;
import dedeadend.killmyapps.util.QuickActionsHelper;

public class KillWidget1x1Provider extends BaseWidgetProvider {

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_kill_1x1;
    }

    @Override
    protected String getIdleStatus() {
        return "KILL";
    }

    @Override
    protected int getStatusViewId() {
        return R.id.widget_status;
    }

    @Override
    protected int[] getActionViewIds() {
        return new int[]{R.id.widget_container};
    }

    @Override
    protected int[] getActions() {
        return new int[]{QuickActionsHelper.ACTION_KILL};
    }

    @Override
    protected int getProgressViewId() {
        return R.id.widget_progress;
    }

    @Override
    protected int getIdleIconViewId() {
        return R.id.widget_icon_idle;
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
