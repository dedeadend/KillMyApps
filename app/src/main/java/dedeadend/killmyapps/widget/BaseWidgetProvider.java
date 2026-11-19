package dedeadend.killmyapps.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import java.util.concurrent.atomic.AtomicBoolean;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.util.AppListHelper;
import dedeadend.killmyapps.util.QuickActionsHelper;

public abstract class BaseWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_WIDGET = "dedeadend.killmyapps.action.WIDGET_ACTION";

    private static final String ACTION_REFRESH = "dedeadend.killmyapps.action.WIDGET_REFRESH_COUNT";

    private static final String EXTRA_ACTION = "widget_action";

    private static final String EXTRA_WIDGET_ID = AppWidgetManager.EXTRA_APPWIDGET_ID;

    private static final long RESET_DELAY_MS = 3000L;

    private static final AtomicBoolean isWidgetInIdleState = new AtomicBoolean(true);
    protected enum WidgetState {
        IDLE, PROCESSING, SUCCESS, FAILED
    }

    protected abstract int getLayoutResource();

    protected abstract String getIdleStatus();

    protected abstract int[] getActionViewIds();

    protected abstract int[] getActions();

    protected int getTargetCountViewId() {
        return 0;
    }

    protected int getRefreshViewId() {
        return 0;
    }

    protected int getEngineViewId() {
        return 0;
    }

    protected int getOpenAppViewId() {
        return 0;
    }

    protected int getStatusViewId() {
        return 0;
    }

    protected int getProgressViewId() {
        return 0;
    }

    protected int getIdleIconViewId() {
        return 0;
    }

    protected int getSuccessIconViewId() {
        return 0;
    }

    protected int getFailureIconViewId() {
        return 0;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds)
            updateWidget(context, manager, widgetId, getIdleStatus(), WidgetState.IDLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isWidgetInIdleState.get())
            return;

        if (intent != null && ACTION_REFRESH.equals(intent.getAction()) && getRefreshViewId() != 0) {
            int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
                refreshCount(context, widgetId);
            return;
        }

        if (intent != null && ACTION_WIDGET.equals(intent.getAction())) {
            int action = intent.getIntExtra(EXTRA_ACTION, -1);
            if (!supports(action))
                return;

            isWidgetInIdleState.set(false);
            final AppWidgetManager manager = AppWidgetManager.getInstance(context);
            final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            final PendingResult pendingResult = goAsync();
            try {
                updateWidget(context, manager, widgetId, action == QuickActionsHelper.ACTION_SLEEP ?
                        QuickActionsHelper.STATUS_SLEEPING
                        : QuickActionsHelper.STATUS_KILLING, WidgetState.PROCESSING);
                QuickActionsHelper.runAsync(action, result -> {
                    updateWidget(context, manager, widgetId, result.status, result.success ?
                            WidgetState.SUCCESS : WidgetState.FAILED);
                    App.handler.postDelayed( () ->
                            updateWidget(context, manager, widgetId, getIdleStatus(), WidgetState.IDLE),
                            RESET_DELAY_MS);
                    refreshCount(context, widgetId);
                    isWidgetInIdleState.set(true);
                    pendingResult.finish();
                });
            } catch (RuntimeException exception) {
                updateWidget(context, manager, widgetId, QuickActionsHelper.STATUS_FAILED, WidgetState.FAILED);
                App.handler.postDelayed(() ->
                                updateWidget(context, manager, widgetId, getIdleStatus(), WidgetState.IDLE),
                        RESET_DELAY_MS);
                refreshCount(context, widgetId);
                isWidgetInIdleState.set(true);
                pendingResult.finish();
            }
            return;
        }

        super.onReceive(context, intent);
    }

    private boolean supports(int action) {
        int[] actions = getActions();
        if (actions == null)
            return false;
        for (int supported : actions)
            if (supported == action)
                return true;
        return false;
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId, String status, WidgetState state) {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            return;
        if (manager.getAppWidgetInfo(widgetId) == null)
            return;
        RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource());

        int statusId = getStatusViewId();
        if (statusId != 0)
            views.setTextViewText(statusId, status);

        int engineId = getEngineViewId();
        if (engineId != 0)
            views.setTextViewText(engineId, getEngineLabel());

        setVisibility(views, getProgressViewId(), state == WidgetState.PROCESSING);
        setVisibility(views, getIdleIconViewId(), state == WidgetState.IDLE);
        setVisibility(views, getSuccessIconViewId(), state == WidgetState.SUCCESS);
        setVisibility(views, getFailureIconViewId(), state == WidgetState.FAILED);

        int[] viewIds = getActionViewIds();
        int[] actions = getActions();
        if (viewIds != null && actions != null) {
            for (int i = 0; i < viewIds.length && i < actions.length; i++) {
                Intent actionIntent = new Intent(context, getClass());
                actionIntent.setAction(ACTION_WIDGET);
                actionIntent.putExtra(EXTRA_ACTION, actions[i]);
                actionIntent.putExtra(EXTRA_WIDGET_ID, widgetId);
                actionIntent.setData(Uri.parse("killmyapps://widget/" +
                        getClass().getSimpleName() + "/" + widgetId + "/" + actions[i]));
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        getClass().getName().hashCode() * 100 + widgetId * 10 + actions[i],
                        actionIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(viewIds[i], pendingIntent);
            }

            int refreshId = getRefreshViewId();
            if (refreshId != 0) {
                Intent refreshIntent = new Intent(context, getClass());
                refreshIntent.setAction(ACTION_REFRESH);
                refreshIntent.putExtra(EXTRA_WIDGET_ID, widgetId);
                refreshIntent.setData(Uri.parse("killmyapps://widget/refresh/" +
                        getClass().getSimpleName() + "/" + widgetId));
                PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context,
                        getClass().getName().hashCode() * 31 + widgetId * 7,
                        refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(refreshId, refreshPendingIntent);
            }

            int openAppId = getOpenAppViewId();
            if (openAppId != 0) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (launchIntent != null) {
                    PendingIntent openPendingIntent = PendingIntent.getActivity(context,
                            getClass().getName().hashCode() * 11,
                            launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(openAppId, openPendingIntent);
                }
            }
        }

        manager.updateAppWidget(widgetId, views);
    }

    private void refreshCount(Context context, int widgetId) {

        final int countId = getTargetCountViewId();
        if (countId == 0)
            return;

        final AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (manager.getAppWidgetInfo(widgetId) == null)
            return;

        final RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource());
        views.setTextViewText(countId, "—");
        setVisibility(views, getRefreshViewId(), false);
        manager.partiallyUpdateAppWidget(widgetId, views);
        final Context appContext = context.getApplicationContext();
        try {
            App.executorService.execute(() -> {
                String result;
                try {
                    result = String.valueOf(AppListHelper.getFilteredAppsList(appContext, true).size());
                } catch (RuntimeException exception) {
                    result = "—";
                }
                if (manager.getAppWidgetInfo(widgetId) != null) {
                    views.setTextViewText(countId, result);
                    setVisibility(views, getRefreshViewId(), true);
                    manager.partiallyUpdateAppWidget(widgetId, views);
                }
            });
        } catch (RuntimeException exception) {
            if (manager.getAppWidgetInfo(widgetId) != null) {
                setVisibility(views, getRefreshViewId(), true);
                manager.partiallyUpdateAppWidget(widgetId, views);
            }
        }
    }

    private void setVisibility(RemoteViews views, int viewId, boolean visible) {
        if (viewId != 0)
            views.setViewVisibility(viewId, visible ? View.VISIBLE : View.GONE);
    }

    private String getEngineLabel() {
        if (App.settings == null)
            return "ENGINE NOT SET";

        switch (App.settings.getInt(App.KILLER_MODE, 1)) {
            case 1:
                return "ROOT ENGINE";
            case 2:
                return "SHIZUKU ENGINE";
            case 3:
                return "SHIZUKU+ ENGINE";
            default:
                return "ENGINE NOT SET";
        }
    }
}
