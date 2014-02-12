package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.data.PlayerManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * An activity representing a list of Players. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link PlayerDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PlayerListFragment} and the item details (if present) is a
 * {@link PlayerDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PlayerListFragment.Callbacks} interface to listen for item selections.
 */
public class PlayerListActivity extends FragmentActivity implements
		PlayerListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_list);

		// Make sure the settings are set to their default values on initial
		// launch
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		if (findViewById(R.id.player_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((PlayerListFragment) getSupportFragmentManager().findFragmentById(
					R.id.player_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Create the Actionbar menu for the PlayerList
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle Action bar clicks
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_add_player:
			addPlayer();
			return true;
		case R.id.menu_reset_players:
			resetPlayers();
			return true;
		case R.id.menu_settings:
			launchSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Callback method from {@link PlayerListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(long id, int position) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(PlayerDetailFragment.ARG_PLAYER_ID, id);
			arguments.putInt(PlayerDetailActivity.ARG_PLAYER_INDEX, position);
			arguments.putBoolean(PlayerDetailFragment.ARG_START_IN_NOTES, true);
			PlayerDetailFragment fragment = new PlayerDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.player_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			gotoPlayerDetail(position, id, true, false, true);
		}
	}

	/**
	 * Callback when adjusting score
	 * 
	 * @param v
	 */
	public void onAdjustScore(View v) {
		boolean isPositive = (v.getId() == R.id.plus);
		RelativeLayout row = ((RelativeLayout) v.getParent());
		PlayerListFragment listFragment = ((PlayerListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.player_list));
		int index = listFragment.getListView().getPositionForView(row);
		Long id = listFragment.getListAdapter().getItemId(index);
		gotoPlayerDetail(index, id, isPositive, true, false);
	}

	public void editName(View v) {
		PlayerListFragment listFragment = ((PlayerListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.player_list));
		listFragment.switchEditName(v);
	}

	/**
	 * Goto the player detail at the given index, specified by the id
	 * 
	 * @param index
	 *            Index in the viewflipper to navigate to
	 * @param id
	 *            ID of the player
	 * @param isPositive
	 *            Should the numpad be positive?
	 * @param returnToList
	 *            Should hitting enter cause the screen to return to this list
	 * @param startInNotes
	 *            Should you start with notes selected?
	 */
	private void gotoPlayerDetail(int index, long id, boolean isPositive,
			boolean returnToList, boolean startInNotes) {
		Log.d(TAG, "gotoPlayerDetail: " + index + ", " + id + ", " + isPositive);
		Intent i = new Intent(this, PlayerDetailActivity.class);
		i.putExtra(PlayerDetailActivity.ARG_PLAYER_INDEX, index);
		i.putExtra(PlayerDetailFragment.ARG_PLAYER_ID, id);
		i.putExtra(NumpadFragment.ARG_POS_NEG, isPositive);
		i.putExtra(PlayerDetailActivity.ARG_RETURN_TO_LIST, returnToList);
		i.putExtra(PlayerDetailActivity.ARG_START_IN_NOTES, startInNotes);
		startActivity(i);
	}

	/**
	 * Reset the scores of all the players
	 */
	private void resetPlayers() {
		PlayerManager.resetAllPlayers(this);
	}

	/**
	 * Add a player to the list
	 */
	private void addPlayer() {
		((PlayerListFragment) getSupportFragmentManager().findFragmentById(
				R.id.player_list)).addNewPlayer();
	}

	/**
	 * Launch the settings activity
	 */
	private void launchSettings() {
		Log.d(TAG, "launchSettings");
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}

	private static final String TAG = "PlayerListActivity";
}
