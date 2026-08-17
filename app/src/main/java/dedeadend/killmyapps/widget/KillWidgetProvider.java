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

public class KillWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_KILL_APPS = "dedeadend.killmyapps.action.KILL_APPS";

    private enum State {IDLE, PROCESSING, SUCCESS, FAIL}

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, appWidgetId, "Ready to Kill", State.IDLE);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_KILL_APPS.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, KillWidgetProvider.class);

            updateWidgetUI(context, appWidgetManager, thisWidget, "Killing...", State.PROCESSING);

            App.executorService.execute(() -> {
                List<AppInfo> targets = AppListUtils.getFilteredAppsList(App.context, true);

                String resultText;
                State state;
                if (targets.isEmpty()) {
                    resultText = "All Dead";
                    state = State.SUCCESS;
                } else {
                    boolean success = Killer.killListOfApps(targets);
                    resultText = success ? targets.size() + " Dead" : "Failed";
                    state = success ? State.SUCCESS : State.FAIL;
                }

                App.handler.post(() -> updateWidgetUI(context, appWidgetManager, thisWidget, resultText, state));

                App.handler.postDelayed(() -> updateWidgetUI(context, appWidgetManager, thisWidget, "Ready to Kill", State.IDLE), 3000);
            });
        }
    }

    private void updateWidgetUI(Context context, AppWidgetManager manager, ComponentName componentName, String label, State state) {
        RemoteViews views = buildRemoteViews(context, label, state);
        manager.updateAppWidget(componentName, views);
    }

    private void updateWidgetUI(Context context, AppWidgetManager manager, int appWidgetId, String label, State state) {
        RemoteViews views = buildRemoteViews(context, label, state);
        manager.updateAppWidget(appWidgetId, views);
    }

    private RemoteViews buildRemoteViews(Context context, String label, State state) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_kill_apps);
        views.setTextViewText(R.id.widget_status_text, label);

        switch (state) {
            case PROCESSING:
                views.setViewVisibility(R.id.widget_success_icon, View.GONE);
                views.setViewVisibility(R.id.widget_fail_icon, View.GONE);
                views.setViewVisibility(R.id.widget_run_icon, View.GONE);
                views.setViewVisibility(R.id.widget_progress, View.VISIBLE);
                break;

            case SUCCESS:
                views.setViewVisibility(R.id.widget_progress, View.GONE);
                views.setViewVisibility(R.id.widget_fail_icon, View.GONE);
                views.setViewVisibility(R.id.widget_run_icon, View.GONE);
                views.setViewVisibility(R.id.widget_success_icon, View.VISIBLE);
                break;

            case FAIL:
                views.setViewVisibility(R.id.widget_progress, View.GONE);
                views.setViewVisibility(R.id.widget_success_icon, View.GONE);
                views.setViewVisibility(R.id.widget_run_icon, View.GONE);
                views.setViewVisibility(R.id.widget_fail_icon, View.VISIBLE);
                break;

            case IDLE:
            default:
                views.setViewVisibility(R.id.widget_progress, View.GONE);
                views.setViewVisibility(R.id.widget_success_icon, View.GONE);
                views.setViewVisibility(R.id.widget_fail_icon, View.GONE);
                views.setViewVisibility(R.id.widget_run_icon, View.VISIBLE);
                break;
        }

        Intent killIntent = new Intent(context, KillWidgetProvider.class);
        killIntent.setAction(ACTION_KILL_APPS);
        killIntent.setPackage(context.getPackageName());
        PendingIntent killPendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                killIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_kill_action, killPendingIntent);

        Intent launchAppIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchAppIntent != null) {
            PendingIntent launchPendingIntent = PendingIntent.getActivity(
                    context,
                    200,
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_container, launchPendingIntent);
        }

        return views;
    }
}