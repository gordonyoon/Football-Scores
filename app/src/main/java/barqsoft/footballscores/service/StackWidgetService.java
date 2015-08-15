package barqsoft.footballscores.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

public class StackWidgetService extends RemoteViewsService {

    public static final String ACTION_SELECT_WIDGET_ITEM = "selectItem";
    public static final String KEY_ITEM_POS = "itemPosition";

    public StackWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    class StackRemoteViewsFactory implements RemoteViewsFactory {
        private Context mContext;
        private int mAppWidgetId;
        private Cursor mCursor;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            // get today's matches
            String[] projection = {DatabaseContract.scores_table.HOME_COL,
                    DatabaseContract.scores_table.AWAY_COL,
                    DatabaseContract.scores_table.HOME_GOALS_COL,
                    DatabaseContract.scores_table.AWAY_GOALS_COL,
                    DatabaseContract.scores_table.TIME_COL};
            String[] selectionArgs = {Utilities.getDate(0)};
            mCursor = getApplicationContext().getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    projection,
                    null,
                    selectionArgs,
                    null);
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            mCursor.moveToPosition(position);
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
            rv.setTextViewText(R.id.home_name, mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL)));
            rv.setTextViewText(R.id.away_name, mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL)));
            rv.setTextViewText(R.id.date_textview, mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL)));
            rv.setTextViewText(R.id.score_textview, Utilities.getScores(
                    mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL)),
                    mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL))));

            // fillIntent for each item in the collection
            Intent fillIntent = new Intent();
            fillIntent.setAction(ACTION_SELECT_WIDGET_ITEM);
            fillIntent.putExtra(KEY_ITEM_POS, position);
            rv.setOnClickFillInIntent(R.id.widget_list_item, fillIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
