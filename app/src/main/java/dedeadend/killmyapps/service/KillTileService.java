package dedeadend.killmyapps.service;

import dedeadend.killmyapps.util.QuickActionsHelper;

public class KillTileService extends BaseTileService {

    @Override
    protected int getAction() {
        return QuickActionsHelper.ACTION_KILL;
    }

    @Override
    protected String getIdleLabel() {
        return "Kill Apps";
    }

    @Override
    protected String getWorkingLabel() {
        return QuickActionsHelper.STATUS_KILLING;
    }
}
