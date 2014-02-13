package net.erickelly.scorekeeper;

import static net.erickelly.scorekeeper.data.Players.NAME;
import static net.erickelly.scorekeeper.data.Players.PLAYERS_TABLE_NAME;
import static net.erickelly.scorekeeper.data.Players.PLAYERS_WITH_SCORE_URI;
import static net.erickelly.scorekeeper.data.Players.SCORE;
import static net.erickelly.scorekeeper.data.Players._ID;
import net.erickelly.scorekeeper.data.CursorWithDelete;
import net.erickelly.scorekeeper.data.PlayerManager;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.DeleteItemCallback;

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
		LoaderManager.LoaderCallbacks<Cursor>, DeleteItemCallback {

	private static final long TIMEOUT_SECONDS = 3;
	private static final long TIMEOUT = TIMEOUT_SECONDS * 1000;
	private int adjustAmt = 0;
	private int progressStatus = 100;

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
				R.layout.player_list_item, null, new String[] { NAME, SCORE,
						_ID }, new int[] { R.id.player_name, R.id.score }, 0);

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

		final ListView listView = getListView();

		// Set the listview padding correctly
		// (need padding above/below listview so that the first item isn't
		// squashed into the actionbar)
		float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		int sizeInPx = (int) (4 * scale + 0.5f);
		listView.setPadding(0, sizeInPx, 0, sizeInPx);
		listView.setClipToPadding(false);

		listView.setBackgroundColor(getResources().getColor(
				R.color.list_background));
		listView.setDividerHeight(0);
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

		// Somewhere in your adapter creation code
		ContextualUndoAdapter adapter = new ContextualUndoAdapter(mAdapter,
				R.layout.deleted_player_list_item, R.id.undo);
		adapter.setAbsListView(listView);
		setListAdapter(adapter);
		adapter.setDeleteItemCallback(this);
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

		flushDeletedPlayers();

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		Log.d(TAG, "Position: " + position + ", ID: " + id);
		mCallbacks.onItemSelected(id, position);
	}

	@Override
	public void deleteItem(int position) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		long id = cursor.getLong(cursor.getColumnIndex(_ID));
		CursorWithDelete cursorWrapper = new CursorWithDelete(
				mAdapter.getCursor(), position);
		mAdapter.swapCursor(cursorWrapper);
		deletePlayer(id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	private void flushDeletedPlayers() {
		if (getListView() != null) {
			ListAdapter adapter = getListView().getAdapter();
			if (adapter instanceof ContextualUndoAdapter) {
				ContextualUndoAdapter undoAdapter = (ContextualUndoAdapter) adapter;
				undoAdapter.onListScrolled();
				undoAdapter.notifyDataSetChanged();
			}
		}
	}

	public void addNewPlayer() {
		PlayerManager.getInstance().addPlayer(getActivity(),
				getDefaultPlayerName());
		mAdapter.notifyDataSetChanged();
		getListView().postDelayed(new Runnable() {
			@Override
			public void run() {
				getListView().setSelection(mAdapter.getCount() - 1);
				getListView().smoothScrollToPosition(mAdapter.getCount() - 1);
			}
		}, 150L);
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

	public String getDefaultPlayerName() {
		return "Player "
				+ (PlayerManager.getInstance().getPlayerCount(getActivity()) + 1);
	}

	public void switchEditName(View v) {
		final RelativeLayout parent = (RelativeLayout) v.getParent();
		final EditText editName = (EditText) parent
				.findViewById(R.id.edit_player_name);
		final Button name = (Button) parent.findViewById(R.id.player_name);
		final ImageButton confirmButton = (ImageButton) parent
				.findViewById(R.id.confirm_edit_button);
		if (editName.getVisibility() == View.VISIBLE) {
			// Persist the new player name
			// The extra space on the end is to prevent the italic text
			// from being slightly cut off
			String newName = editName.getText().toString().trim() + " ";
			int position = getListView().getPositionForView(parent);
			long id = mAdapter.getItemId(position);
			PlayerManager.editPlayerName(getActivity(), id, newName);

			editName.setFocusableInTouchMode(false);
			name.setText(newName);
			name.setVisibility(View.VISIBLE);
			editName.setVisibility(View.GONE);
			confirmButton.setVisibility(View.GONE);

			getListView().setDescendantFocusability(
					ListView.FOCUS_BLOCK_DESCENDANTS);
			getListView().setItemsCanFocus(false);
			parent.setDescendantFocusability(RelativeLayout.FOCUS_BLOCK_DESCENDANTS);

			InputMethodManager keyboard = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			keyboard.hideSoftInputFromWindow(getActivity().getCurrentFocus()
					.getWindowToken(), 0);
		} else {
			getListView().setDescendantFocusability(
					ListView.FOCUS_AFTER_DESCENDANTS);
			getListView().setItemsCanFocus(true);
			parent.setDescendantFocusability(RelativeLayout.FOCUS_AFTER_DESCENDANTS);

			String text = "" + name.getText();
			editName.setText(text.trim());
			editName.setVisibility(View.VISIBLE);
			name.setVisibility(View.GONE);
			confirmButton.setVisibility(View.VISIBLE);

			editName.setFocusableInTouchMode(true);
			// editName.setSelectAllOnFocus(true);
			Log.d(TAG, "Did edit text take focus? " + editName.requestFocus());

			// bring up keyboard
			editName.postDelayed(new Runnable() {
				@Override
				public void run() {
					editName.selectAll();
					InputMethodManager keyboard = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					keyboard.showSoftInput(editName, 0);
				}
			}, 100L);

		}
	}

	public void adjustPlayerScore(final ProgressBar progressBar) {
		progressBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (progressStatus <= 0) {
					progressStatus -= 1;
					// Update the progress bar and display the
					// current value in the text view
					progressBar.post(new Runnable() {
						public void run() {
							progressBar.setProgress(progressStatus);
						}
					});
					try {
						// Sleep for 200 milliseconds.
						// Just to display the progress slowly
						Thread.sleep(TIMEOUT / 100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				progressBar.setVisibility(View.INVISIBLE);
			}
		}).start();
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
