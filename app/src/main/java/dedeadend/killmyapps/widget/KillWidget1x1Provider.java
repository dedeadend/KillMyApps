package dedeadend.killmyapps.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.Killer;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.AppListUtils;

public class KillWidget1x1Provider extends AppWidgetProvider {

    private static final String ACTION_KILL_APPS_1X1 = "dedeadend.killmyapps.action.KILL_APPS_1X1";

    private enum State {IDLE, PROCESSING, RESULT}

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, appWidgetId, "", State.IDLE);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_KILL_APPS_1X1.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, KillWidget1x1Provider.class);

            updateWidgetUI(context, appWidgetManager, thisWidget, "", State.PROCESSING);

            App.executorService.execute(() -> {
                List<AppInfo> targets = AppListUtils.getFilteredAppsList(App.context, true);

                String resultText;
                if (targets.isEmpty()) {
                    resultText = "All Dead";
                } else {
                    boolean success = Killer.killListOfApps(targets);
                    resultText = success ? targets.size() + " Dead" : "Failed";
                }

                final String finalResult = resultText;
                App.handler.post(() -> updateWidgetUI(context, appWidgetManager, thisWidget, finalResult, State.RESULT));

                App.handler.postDelayed(() -> updateWidgetUI(context, appWidgetManager, thisWidget, "", State.IDLE), 3000);
            });
        }
    }

    private void updateWidgetUI(Context context, AppWidgetManager manager, ComponentName componentName, String text, State state) {
        RemoteViews views = buildRemoteViews(context, text, state);
        manager.updateAppWidget(componentName, views);
    }

    private void updateWidgetUI(Context context, AppWidgetManager manager, int appWidgetId, String text, State state) {
        RemoteViews views = buildRemoteViews(context, text, state);
        manager.updateAppWidget(appWidgetId, views);
    }

    private RemoteViews buildRemoteViews(Context context, String text, State state) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_kill_apps_1x1);

        switch (state) {
            case PROCESSING:
                views.setViewVisibility(R.id.widget_1x1_icon, View.GONE);
                views.setViewVisibility(R.id.widget_1x1_status_text, View.GONE);
                views.setViewVisibility(R.id.widget_1x1_progress, View.VISIBLE);
                break;

            case RESULT:
                views.setViewVisibility(R.id.widget_1x1_icon, View.GONE);
                views.setViewVisibility(R.id.widget_1x1_progress, View.GONE);
                views.setTextViewText(R.id.widget_1x1_status_text, text);
                views.setViewVisibility(R.id.widget_1x1_status_text, View.VISIBLE);
                break;

            case IDLE:
            default:
                views.setViewVisibility(R.id.widget_1x1_progress, View.GONE);
                views.setViewVisibility(R.id.widget_1x1_status_text, View.GONE);
                views.setViewVisibility(R.id.widget_1x1_icon, View.VISIBLE);
                break;
        }

        Intent intent = new Intent(context, KillWidget1x1Provider.class);
        intent.setAction(ACTION_KILL_APPS_1X1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_1x1_container, pendingIntent);

        return views;
    }
}