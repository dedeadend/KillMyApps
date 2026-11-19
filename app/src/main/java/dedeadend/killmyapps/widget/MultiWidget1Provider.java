package dedeadend.killmyapps.widget;

import dedeadend.killmyapps.R;
import dedeadend.killmyapps.util.QuickActionsHelper;

public class MultiWidget1Provider extends BaseWidgetProvider {

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_multi_1;
    }

    @Override
    protected String getIdleStatus() {
        return "TARGETS";
    }

    @Override
    protected int getStatusViewId() {
        return R.id.widget_status;
    }

    @Override
    protected int[] getActionViewIds() {
        return new int[]{R.id.widget_action_kill, R.id.widget_action_sleep, R.id.widget_action_combo};
    }

    @Override
    protected int[] getActions() {
        return new int[]{QuickActionsHelper.ACTION_KILL, QuickActionsHelper.ACTION_SLEEP, QuickActionsHelper.ACTION_COMBO};
    }

    @Override
    protected int getTargetCountViewId() {
        return R.id.widget_target_count;
    }

    @Override
    protected int getRefreshViewId() {
        return R.id.widget_refresh;
    }

    @Override
    protected int getEngineViewId() {
        return R.id.widget_engine;
    }

    @Override
    protected int getOpenAppViewId() {
        return R.id.widget_container;
    }

    @Override
    protected int getProgressViewId() {
        return R.id.widget_progress;
    }
}