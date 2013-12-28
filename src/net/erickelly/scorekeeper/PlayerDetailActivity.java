package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.NumpadFragment.NumpadListener;
import net.erickelly.scorekeeper.data.ActionFocus;
import net.erickelly.scorekeeper.data.Player;
import net.erickelly.scorekeeper.data.PlayerManager;
import net.erickelly.scorekeeper.data.Sign;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MenuItem;

/**
 * An activity representing a single Player detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link PlayerListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PlayerDetailFragment}.
 */
public class PlayerDetailActivity extends FragmentActivity implements
		NumpadListener, PlayerDetailFragment.Callbacks {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Are we using the notes area or not?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mUsingNotes = prefs.getBoolean(SettingsFragment.PREF_NOTES, false);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(PlayerDetailFragment.ARG_PLAYER_ID, getIntent()
					.getLongExtra(PlayerDetailFragment.ARG_PLAYER_ID, 0));
			arguments.putBoolean(PlayerDetailFragment.ARG_NOTES, 
					mUsingNotes);
			PlayerDetailFragment fragment = new PlayerDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.player_detail_container, fragment).commit();
		}

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mPlayersCollectionPagerAdapter = new PlayersPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.player_detail_container);
		mViewPager.setAdapter(mPlayersCollectionPagerAdapter);
		mViewPager.setCurrentItem(getIntent().getIntExtra(ARG_PLAYER_INDEX, 0));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			private int previousPage = 0;

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// Log.d(TAG, "onPageScrolled: " + previousPage);
				previousPage = position;
			}

			@Override
			public void onPageSelected(int position) {
				Log.d(TAG, "onPageSelected: " + position);
				clearState(previousPage);
			}
		});

		mSign = Sign.valueOf(getIntent().getExtras().getBoolean(
				NumpadFragment.ARG_POS_NEG, true));

		mReturnToList = getIntent().getExtras().getBoolean(ARG_RETURN_TO_LIST,
				false);

		// Set the keypad to be positive or negative
		setOperationSign(mSign);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					PlayerListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Since this is an object collection, use a FragmentStatePagerAdapter,
	// and NOT a FragmentPagerAdapter.
	public class PlayersPagerAdapter extends FragmentStatePagerAdapter {
		public PlayersPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Log.d(TAG, "getItem: " + position);
			Player p = PlayerManager.getPlayerByIndex(
					PlayerDetailActivity.this, position);
			Fragment fragment = new PlayerDetailFragment();
			Bundle args = new Bundle();
			args.putLong(PlayerDetailFragment.ARG_PLAYER_ID, p.getId());
			args.putBoolean(PlayerDetailFragment.ARG_NOTES, mUsingNotes);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Log.d(TAG, "getCount");
			// TODO: Why is getCount() called so many times?
			return PlayerManager.getInstance().getPlayerCount(
					PlayerDetailActivity.this);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Log.d(TAG, "getPageTitle: " + position);
			Player p = PlayerManager.getPlayerByIndex(
					PlayerDetailActivity.this, position);
			return p.getName();
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		private static final String TAG = "PlayerDetailActivity.PlayersPagerAdapter";
	}

	@Override
	public void onNumberClicked(String number) {
		Log.d(TAG, "onNumberClicked: " + number);
		if (mFocus.equals(ActionFocus.SCORE)) {
			mAdjustAmount += number;
			Integer amt = getCurrentAdjustAmount();
			getCurrentPlayerFragment().adjustScore(amt);
		} else {
			mNotes += number;
			getCurrentPlayerFragment().setNotes(mNotes);
		}
	}

	@Override
	public void onDeleteClicked() {
		Log.d(TAG, "onDeleteClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			if (mAdjustAmount.length() > 0) {
				mAdjustAmount = mAdjustAmount.substring(0,
						mAdjustAmount.length() - 1);
				Integer amt = getCurrentAdjustAmount();
				getCurrentPlayerFragment().adjustScore(amt);
			}
		} else {
			if (mNotes.length() > 0) {
				mNotes = mNotes.substring(0, mNotes.length() - 1);
				getCurrentPlayerFragment().setNotes(mNotes);
				// updateNote(mNotes);
			}
		}
	}

	@Override
	public void onEnterClicked() {
		Log.d(TAG, "onEnterClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			if (adjustScore()) {
				clearState(mViewPager.getCurrentItem());
				resetNotes();
				mUpdate = false;
			}
		} else {
			updateNote(mNotes);
			reset();
		}
		if (mReturnToList) {
			this.finish();
		}
	}

	@Override
	public void onSignClicked(Sign sign) {
		Log.d(TAG, "onSignClicked: " + sign.toString());
		if (mFocus.equals(ActionFocus.SCORE)) {
			mSign = sign;
			getCurrentPlayerFragment().adjustScore(getCurrentAdjustAmount());
		}
	}

	@Override
	public void onHistoryClicked() {
		Log.d(TAG, "onHistoryClicked");
		long id = getCurrentPlayerFragment().getPlayer().getId();
		Intent i = new Intent(this, PlayerHistoryListActivity.class);
		i.putExtra(PlayerHistoryListFragment.ARG_PLAYER_ID, id);
		startActivity(i);
	}

	@Override
	public void onUndoClicked() {
		Log.d(TAG, "onUndoClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			PlayerManager.undoLastAdjustment(this, getCurrentPlayerFragment()
					.getPlayer().getId());
			getCurrentPlayerFragment().clear();
		} else {
			mNotes = "";
			getCurrentPlayerFragment().setNotes(mNotes);
		}
	}

	/**
	 * Update the current player's note
	 * 
	 * @param note
	 */
	private void updateNote(String note) {
		updateNote(getCurrentPlayerFragment().getPlayer().getId(), note);
	}

	/**
	 * Update the note associated with the given player id
	 * 
	 * @param playerId
	 * @param note
	 */
	private void updateNote(long playerId, String note) {
		Log.d(TAG, "updateNote: " + playerId + ", " + note);
		PlayerManager.updateScoreNotes(this, playerId, note, mUpdate);
		mUpdate = true;
	}

	/**
	 * Adjust the score to the "current" adjust amount
	 */
	private boolean adjustScore() {
		Log.d(TAG, "adjustScore");
		Integer adjustAmt = getCurrentAdjustAmount();
		if (adjustAmt != null) {
			Player p = PlayerManager.getPlayerByIndex(this,
					mViewPager.getCurrentItem());
			PlayerManager.adjustScore(this, p.getId(), adjustAmt, mNotes,
					mUpdate);
			mUpdate = true;
			return true;
		}
		return false;
	}

	@Override
	public void onSwitchFocus(ActionFocus focus) {
		Log.d(TAG, "onSwitchFocus: " + focus);
		this.mFocus = focus;
		getNumpadFragment().setUndoText(mFocus);
		// TODO: +/- sign?
	}

	/**
	 * Resets the state of the given fragment position. When switching between
	 * players using the view flipper, the player you are exiting must have its
	 * unsaved state cleared. This resets the view, temporary adjust amount, and
	 * default operation sign.
	 * 
	 * @param previousPagePosition
	 */
	private void clearState(int previousPagePosition) {
		Log.d(TAG, "clearState");
		// TODO: Do this in the background

		// Reset the old fragment
		PlayerDetailFragment fragment = ((PlayerDetailFragment) mViewPager
				.getAdapter().instantiateItem(mViewPager, previousPagePosition));
		fragment.clear();

		// Save the notes field into the database
		// updateNote(getPlayerIdByPosition(previousPagePosition), mNotes);

		// Reset the Activity to the defaults
		reset();

	}

	/**
	 * Resets the state of the Activity
	 */
	private void reset() {
		Log.d(TAG, "reset");
		// Notes
		PlayerDetailFragment fragment = getCurrentPlayerFragment();
		fragment.refreshPlayer();
		mNotes = fragment.getPlayer().getLastNotesField();
		if (mNotes == null)
			mNotes = "";
		fragment.setNotes(mNotes); // TODO: Do I really need this line?

		// Adjust amt
		mAdjustAmount = "";
		setOperationSign(Sign.POSITIVE);

		// Focus resets
		setFocus(mFocus);
	}

	/**
	 * Set the notes field to an empty string and show it
	 */
	private void resetNotes() {
		mNotes = "";
		getCurrentPlayerFragment().setNotes(mNotes);
	}

	/**
	 * Returns the current temporary adjust amount
	 * 
	 * @return The adjust amount as an Integer, or null if there is no current
	 *         adjust amount
	 */
	private Integer getCurrentAdjustAmount() {
		if (!mAdjustAmount.isEmpty()) {
			return Integer.parseInt((mSign.isPositive() ? "" : "-")
					+ mAdjustAmount);
		} else {
			return null;
		}
	}

	/**
	 * Set the sign of the numpad to the given Sign
	 * 
	 * @param sign
	 */
	private void setOperationSign(Sign sign) {
		mSign = sign;
		getNumpadFragment().setOperationSign(sign);
	}

	/**
	 * Set the focus of the PlayerDetail view to the given ActionFocus
	 * 
	 * @param sign
	 */
	private void setFocus(ActionFocus focus) {
		getCurrentPlayerFragment().setFocus(focus);
		onSwitchFocus(focus);
	}

	/**
	 * Return the id associated with the position in the viewpager
	 * 
	 * @param pos
	 * @return
	 */
	@SuppressWarnings("unused")
	private long getPlayerIdByPosition(int pos) {
		return ((PlayerDetailFragment) mViewPager.getAdapter().instantiateItem(
				mViewPager, pos)).getPlayer().getId();
	}

	/**
	 * Return the PlayerFragment representing the current player that is being
	 * viewed.
	 * 
	 * @return
	 */
	private PlayerDetailFragment getCurrentPlayerFragment() {
		return (PlayerDetailFragment) mViewPager.getAdapter().instantiateItem(
				mViewPager, mViewPager.getCurrentItem());
	}

	/**
	 * Return the numpad fragment
	 * 
	 * @return
	 */
	private NumpadFragment getNumpadFragment() {
		return (NumpadFragment) getSupportFragmentManager().findFragmentById(
				R.id.numpad);
	}

	/**
	 * Should the activity return to the player screen when enter is clicked?
	 */
	public static final String ARG_RETURN_TO_LIST = "return";

	/**
	 * The fragment argument representing the state of the +/- button
	 */
	public static final String ARG_POS_NEG = "pos_neg";

	/**
	 * The fragment argument representing the index in the viewpager
	 */
	public static final String ARG_PLAYER_INDEX = "player_index";

	// When requested, this adapter returns a PageDetailFragment,
	// representing an object in the collection.
	PlayersPagerAdapter mPlayersCollectionPagerAdapter;

	ViewPager mViewPager;

	private String mNotes = "";
	private String mAdjustAmount = "";
	private Sign mSign = Sign.POSITIVE;
	private boolean mReturnToList = false;
	private boolean mUpdate = false; // Insert/Update a new record
	private ActionFocus mFocus = ActionFocus.SCORE;
	private boolean mUsingNotes = false;

	private static final String TAG = "PlayerDetailActivity";
}
