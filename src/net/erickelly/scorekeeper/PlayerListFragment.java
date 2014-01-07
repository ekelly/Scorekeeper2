package net.erickelly.scorekeeper;

import static net.erickelly.scorekeeper.data.Players.NAME;
import static net.erickelly.scorekeeper.data.Players.PLAYERS_TABLE_NAME;
import static net.erickelly.scorekeeper.data.Players.PLAYERS_WITH_SCORE_URI;
import static net.erickelly.scorekeeper.data.Players.SCORE;
import static net.erickelly.scorekeeper.data.Players._ID;
import net.erickelly.scorekeeper.PlayerNameDialogFragment.PlayerNamePromptListener;
import net.erickelly.scorekeeper.data.Player;
import net.erickelly.scorekeeper.data.PlayerManager;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * A list fragment representing a list of Players. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link PlayerDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PlayerListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PlayerListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.player_list_item, null, new String[] { NAME, SCORE },
				new int[] { R.id.player_name, R.id.score }, 0);
		setListAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			private Long mSelectedId;

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				Log.d(TAG, "onItemCheckedStateChanged: " + id + ", " + checked);
				// Here you can do something when items are
				// selected/de-selected,
				// such as update the title in the CAB
				if (checked) {
					mSelectedId = id;
				} else {
					mSelectedId = null;
				}
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {
				case R.id.menu_delete_player:
					deletePlayer(mSelectedId);
					mode.finish(); // Action picked, so close the CAB
					return true;
				case R.id.menu_edit_player:
					editPlayerName(mSelectedId);
					mode.finish();
					return true;
				case R.id.menu_reset_player:
					resetPlayer(mSelectedId);
					mode.finish();
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Inflate the menu for the CAB
				Log.d(TAG, "onCreateActionMode");
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.menu_context, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Here you can make any necessary updates to the activity when
				// the CAB is removed. By default, selected items are
				// deselected/unchecked.
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		Log.d(TAG, "Position: " + position + ", ID: " + id);
		mCallbacks.onItemSelected(id, position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	/**
	 * Reset the score of the given player
	 * 
	 * @param playerId
	 *            ID of the player to reset
	 */
	private void resetPlayer(long playerId) {
		PlayerManager.resetPlayerScore(getActivity(), playerId);
	}

	/**
	 * Delete the player identified by the player id
	 * 
	 * @param playerId
	 */
	private void deletePlayer(long playerId) {
		PlayerManager.getInstance().deletePlayer(getActivity(), playerId);
	}

	/**
	 * Edit the player name associated with the given player id
	 * 
	 * @param playerId
	 */
	private void editPlayerName(final long playerId) {
		Log.d(TAG, "editPlayerName: " + playerId);
		Player p = PlayerManager.getPlayer(getActivity(), playerId);
		PlayerNameDialogFragment.newInstance(new PlayerNamePromptListener() {
			/**
			 * When the new player's name is entered, this method is called. Use
			 * this to actually set the new player name
			 */
			@Override
			public void onPlayerNameEntry(String name) {
				PlayerManager.editPlayerName(getActivity(), playerId, name);
				mAdapter.notifyDataSetChanged();
			}
		}, p.getName()).show(getFragmentManager(), "EditPlayerName");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		Log.d(TAG, "onCreateLoader");
		return new CursorLoader(getActivity(), PLAYERS_WITH_SCORE_URI,
				new String[] { PLAYERS_TABLE_NAME + "." + _ID, NAME,
						"IFNULL(" + SCORE + ", 0) AS " + SCORE }, null, null,
				PLAYERS_TABLE_NAME + "." + _ID);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		Log.d(TAG, "onLoadFinished");
		Log.d("onLoadFinished: ", String.valueOf(data.getCount()));
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
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * This is the Adapter being used to display the list's data.
	 */
	SimpleCursorAdapter mAdapter;

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(long id, int position);

		/**
		 * Callback for when a player's score is being adjusted
		 */
		public void onAdjustScore(View v);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(long id, int position) {
		}

		@Override
		public void onAdjustScore(View v) {
		}
	};

	private static String TAG = "PlayerListFragment";
}
