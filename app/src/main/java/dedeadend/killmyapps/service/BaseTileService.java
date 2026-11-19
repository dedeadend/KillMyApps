package dedeadend.killmyapps.service;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.util.QuickActionsHelper;

public abstract class BaseTileService extends TileService {

    protected abstract int getAction();

    protected abstract String getIdleLabel();

    protected abstract String getWorkingLabel();

    private final Runnable resetTile = () -> updateTile(getIdleLabel(), Tile.STATE_INACTIVE);

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile(getIdleLabel(), Tile.STATE_INACTIVE);
    }

    @Override
    public void onClick() {
        super.onClick();
        if (getQsTile().getState() != Tile.STATE_INACTIVE)
            return;
        updateTile(getWorkingLabel(), Tile.STATE_ACTIVE);

        QuickActionsHelper.runAsync(getAction(), result -> {
            updateTile(result.status, Tile.STATE_ACTIVE);
            App.handler.postDelayed(resetTile, 3000);
        });
    }

    private void updateTile(String label, int state) {
        Tile tile = getQsTile();
        if (tile == null)
            return;
        tile.setLabel(label);
        tile.setState(state);
        tile.updateTile();
    }
}
