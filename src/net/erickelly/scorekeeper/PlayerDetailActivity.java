package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.data.Player;
import net.erickelly.scorekeeper.data.PlayerManager;
import net.erickelly.scorekeeper.data.Sign;
import android.content.Intent;
import android.os.Bundle;
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
public class PlayerDetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

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
			private int mPreviousPage = 0;

			@Override
			public void onPageScrollStateChanged(int scrollState) {
				String scroll = "";
				switch (scrollState) {
				case ViewPager.SCROLL_STATE_DRAGGING:
					scroll = "Dragging";
					break;
				case ViewPager.SCROLL_STATE_IDLE:
					scroll = "Idle";
					break;
				case ViewPager.SCROLL_STATE_SETTLING:
					scroll = "Settling";
					break;
				}
				Log.d(TAG, "onPageScrollStateChanged: " + scroll);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				mPreviousPage = position;
			}

			@Override
			public void onPageSelected(int position) {
				Log.d("erickell", "onPageSelected: " + position);
				Log.d("erickell", "clearing the state of: " + mPreviousPage);
				clearState(position);
				// clearState(mPreviousPage);
			}
		});

		mSign = Sign.valueOf(getIntent().getExtras().getBoolean(
				NumpadFragment.ARG_POS_NEG, true));

		mReturnToList = getIntent().getExtras().getBoolean(ARG_RETURN_TO_LIST,
				false);

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
			args.putBoolean(PlayerDetailFragment.ARG_RETURN_TO_LIST,
					mReturnToList);
			args.putBoolean(NumpadFragment.ARG_POS_NEG, mSign.isPositive());
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

	/**
	 * Resets the state of the given fragment position. When switching between
	 * players using the view pager, the player you are exiting must have its
	 * unsaved state cleared. This resets the view, temporary adjust amount, and
	 * default operation sign.
	 * 
	 * @param previousPagePosition
	 */
	private void clearState(int previousPagePosition) {
		Log.d(TAG, "clearState");
		// TODO: Do this in the background?

		// Reset the old fragment
		PlayerDetailFragment fragment = ((PlayerDetailFragment) mViewPager
				.getAdapter().instantiateItem(mViewPager, previousPagePosition));
		fragment.clear();
		fragment.reset();

		// Save the notes field into the database
		// updateNote(getPlayerIdByPosition(previousPagePosition), mNotes);

	}

	/**
	 * The fragment argument representing the state of the +/- button
	 */
	public static final String ARG_POS_NEG = "pos_neg";

	/**
	 * Should the activity return to the player screen when enter is clicked?
	 */
	public static final String ARG_RETURN_TO_LIST = "return";

	/**
	 * The fragment argument representing the index in the viewpager
	 */
	public static final String ARG_PLAYER_INDEX = "player_index";

	// When requested, this adapter returns a PageDetailFragment,
	// representing an object in the collection.
	PlayersPagerAdapter mPlayersCollectionPagerAdapter;

	ViewPager mViewPager;

	private boolean mReturnToList = false;
	private Sign mSign = Sign.POSITIVE;

	private static final String TAG = "PlayerDetailActivity";
}
