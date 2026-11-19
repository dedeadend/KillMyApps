package dedeadend.killmyapps.service;

import dedeadend.killmyapps.util.QuickActionsHelper;

public class ComboTileService extends BaseTileService {
    @Override
    protected int getAction() {
        return QuickActionsHelper.ACTION_COMBO;
    }

    @Override
    protected String getIdleLabel() {
        return "Kill + Sleep";
    }

    @Override
    protected String getWorkingLabel() {
        return QuickActionsHelper.STATUS_KILLING;
    }
}
