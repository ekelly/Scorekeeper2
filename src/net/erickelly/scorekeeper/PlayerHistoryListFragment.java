package net.erickelly.scorekeeper;

import static net.erickelly.scorekeeper.data.Players.*;
import net.erickelly.scorekeeper.data.Sign;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class PlayerHistoryListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PlayerHistoryListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_PLAYER_ID)) {
			mId = getArguments().getLong(ARG_PLAYER_ID);
		}

		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.player_history_item, null, new String[] { ADJUST_AMT,
						ADJUST_AMT, SCORE }, new int[] { R.id.adjust_amt,
						R.id.plus_minus, R.id.total_score }, 0);
		mAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (view.getId() == R.id.plus_minus) {
					boolean isPositive = cursor.getInt(columnIndex) >= 0;
					TextView sign = ((TextView) view
							.findViewById(R.id.plus_minus));
					sign.setText(isPositive ? Sign.POSITIVE.toString()
							: Sign.NEGATIVE.toString());
					return true;
				}
				return false;
			}
		});

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
		lv.addHeaderView(getActivity().getLayoutInflater().inflate(
				R.layout.player_history_list_header, null));
		setListAdapter(mAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), Uri.withAppendedPath(SCORES_URI,
				"/" + mId), new String[] { _ID, ADJUST_AMT, SCORE }, null,
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		Log.d(TAG, "onLoadFinished.  Data size: " + data.getCount());
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}

	/**
	 * This is the Adapter being used to display the list's data.
	 */
	SimpleCursorAdapter mAdapter;

	/**
	 * This is the id of the player that we're viewing history information about
	 */
	private long mId;

	public static final String ARG_PLAYER_ID = "player_id";

	private static final String TAG = "PlayerHistoryListFragment";
}
