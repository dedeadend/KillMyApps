package dedeadend.killmyapps.service;

import dedeadend.killmyapps.util.QuickActionsHelper;

public class SleepTileService extends BaseTileService {
    @Override
    protected int getAction() {
        return QuickActionsHelper.ACTION_SLEEP;
    }

    @Override
    protected String getIdleLabel() {
        return "Sleep";
    }

    @Override
    protected String getWorkingLabel() {
        return QuickActionsHelper.STATUS_SLEEPING;
    }
}
