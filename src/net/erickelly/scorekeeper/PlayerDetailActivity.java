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

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mPlayersCollectionPagerAdapter = new PlayersPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.player_detail_container);
		mViewPager.setAdapter(mPlayersCollectionPagerAdapter);
		final int index = getIntent().getIntExtra(ARG_PLAYER_INDEX, 0);
		mViewPager.setCurrentItem(index);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			private int mPreviousPage = index;
			private boolean mTriggered = false;

			@Override
			public void onPageScrollStateChanged(int scrollState) {
				switch (scrollState) {
				case ViewPager.SCROLL_STATE_DRAGGING:
					Log.d(TAG, "clearing the state of: " + mPreviousPage);
					if (!mTriggered) {
						clearState(mPreviousPage);
						mTriggered = true;
					}
					break;
				case ViewPager.SCROLL_STATE_IDLE:
					mTriggered = false;
					break;
				case ViewPager.SCROLL_STATE_SETTLING:
					break;
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				Log.d(TAG, "onPageSelected: " + position);
				mPreviousPage = position;
			}
		});

		mSign = Sign.valueOf(getIntent().getBooleanExtra(
				NumpadFragment.ARG_POS_NEG, true));
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
			overridePendingTransition(R.anim.slide_in_from_left,
					R.anim.slide_out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
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
			args.putBoolean(PlayerDetailFragment.ARG_START_IN_NOTES,
					mStartInNotes);
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
			return PagerAdapter.POSITION_UNCHANGED;
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

		// Reset the old fragment
		PlayerDetailFragment fragment = ((PlayerDetailFragment) mViewPager
				.getAdapter().instantiateItem(mViewPager, previousPagePosition));
		fragment.reset();
	}

	/**
	 * The fragment argument representing the index in the viewpager
	 */
	public static final String ARG_PLAYER_INDEX = "player_index";

	// When requested, this adapter returns a PageDetailFragment,
	// representing an object in the collection.
	private PlayersPagerAdapter mPlayersCollectionPagerAdapter;
	private ViewPager mViewPager;
	private boolean mReturnToList = false;
	private boolean mStartInNotes = false;
	private Sign mSign = Sign.POSITIVE;

	private static final String TAG = "PlayerDetailActivity";
}
