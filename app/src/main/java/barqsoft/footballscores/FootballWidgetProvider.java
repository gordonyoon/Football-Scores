package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.StackView;

import barqsoft.footballscores.service.StackWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class FootballWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(StackWidgetService.ACTION_SELECT_WIDGET_ITEM)) {
            int position = intent.getIntExtra(StackWidgetService.KEY_ITEM_POS, StackView.INVALID_POSITION);
            openDetailedMatch(context, position);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Here we setup the intent which points to the StackViewService which will
        // provide the views for this collection.
        Intent intent = new Intent(context, StackWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // When intents are compared, the extras are ignored, so we need to embed the extras
        // into the data so that the extras will not be ignored.
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        // Construct the RemoteViews object
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.football_app_widget);
        rv.setRemoteAdapter(R.id.stackView, intent);

        // pending intent template
        Intent onItemClick = new Intent(context, FootballWidgetProvider.class);
        onItemClick.setAction(StackWidgetService.ACTION_SELECT_WIDGET_ITEM);
        PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.stackView, onClickPendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    private void openDetailedMatch(Context context, int position) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(StackWidgetService.ACTION_SELECT_WIDGET_ITEM);
        intent.putExtra(StackWidgetService.KEY_ITEM_POS, position);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

