package dedeadend.killmyapps.service;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.Killer;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.AppListUtils;

public class KillTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();

        final Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("Killing...");
            tile.updateTile();
        }

        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                final List<AppInfo> targets = AppListUtils.getFilteredAppsList(App.context, true);

                if (targets.isEmpty()) {
                    App.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tile != null) {
                                tile.setLabel("All Dead");
                                tile.updateTile();
                            }
                        }
                    });
                } else {
                    final boolean success = Killer.killListOfApps(targets);
                    App.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tile != null) {
                                if (success)
                                    tile.setLabel(targets.size() + " Dead");
                                else
                                    tile.setLabel("Failed");
                                tile.updateTile();
                            }
                        }
                    });
                }
                resetTileState(tile);
            }
        });
    }

    private void resetTileState(Tile tile) {
        if (tile != null) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    tile.setState(Tile.STATE_INACTIVE);
                    tile.setLabel("Kill Apps");
                    tile.updateTile();
                }
            }, 3000);
        }
    }
}