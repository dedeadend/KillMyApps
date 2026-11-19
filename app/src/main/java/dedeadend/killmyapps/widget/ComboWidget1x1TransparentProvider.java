package dedeadend.killmyapps.widget;

import dedeadend.killmyapps.R;
import dedeadend.killmyapps.util.QuickActionsHelper;

public class ComboWidget1x1TransparentProvider extends BaseWidgetProvider {

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_combo_1x1_transparent;
    }

    @Override
    protected String getIdleStatus() {
        return "Kill + Sleep";
    }

    @Override
    protected int getStatusViewId() {
        return R.id.widget_status;
    }

    @Override
    protected int[] getActionViewIds() {
        return new int[]{R.id.widget_kill_sleep_button};
    }

    @Override
    protected int[] getActions() {
        return new int[]{QuickActionsHelper.ACTION_COMBO};
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
