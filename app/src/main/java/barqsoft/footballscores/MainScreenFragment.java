package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.StackView;

import barqsoft.footballscores.service.FetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int SCORES_LOADER = 0;
    public ScoresAdapter mAdapter;
    private String[] fragmentdate = new String[1];

    private static final String ARG_POS = "positionArgument";
    private int mWidgetPosition = StackView.INVALID_POSITION;


    public static MainScreenFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt(ARG_POS, position);

        MainScreenFragment fragment = new MainScreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MainScreenFragment() {
    }

    public void setFragmentDate(String date) {
        fragmentdate[0] = date;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWidgetPosition = getArguments().getInt(ARG_POS, StackView.INVALID_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        score_list.setItemsCanFocus(true);
        score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                showDetails(selected.match_id);
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        if (mWidgetPosition != StackView.INVALID_POSITION) {
            cursor.moveToPosition(mWidgetPosition);
            double matchId = cursor.getDouble(cursor.getColumnIndex(DatabaseContract.scores_table.MATCH_ID));
            showDetails(matchId);

            final ListView listView = (ListView) getView().findViewById(R.id.scores_list);
            listView.setSelection(mWidgetPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private void showDetails(double matchId) {
        mAdapter.detail_match_id = matchId;
        MainActivity.selected_match_id = (int) matchId;
        mAdapter.notifyDataSetChanged();
    }
}
