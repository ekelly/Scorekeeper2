package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.NumpadFragment.NumpadListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * An activity representing a single Player detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PlayerListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link PlayerDetailFragment}.
 */
public class PlayerDetailActivity extends FragmentActivity implements NumpadListener {
	
	/**
     * The fragment argument representing the state of the +/- button
     */
    public static final String ARG_POS_NEG = "pos_neg";
	
	// When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    PlayersCollectionPagerAdapter mPlayersCollectionPagerAdapter;
    ViewPager mViewPager;
    
    int currentPage;

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
            arguments.putString(PlayerDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(PlayerDetailFragment.ARG_ITEM_ID));
            PlayerDetailFragment fragment = new PlayerDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_detail_container, fragment)
                    .commit();
        }
        
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mPlayersCollectionPagerAdapter =
                new PlayersCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.player_detail_container);
        mViewPager.setAdapter(mPlayersCollectionPagerAdapter);
        mViewPager.setCurrentItem(Integer.parseInt(getIntent()
                .getStringExtra(PlayerDetailFragment.ARG_ITEM_ID)));
        
        boolean positive = getIntent().getExtras()
                .getBoolean(NumpadFragment.ARG_POS_NEG, true);
        
        // Set the keypad to be positive or negative
        ((NumpadFragment) getSupportFragmentManager().findFragmentById(R.id.numpad))
                .setOperationSign(positive);
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
                NavUtils.navigateUpTo(this, new Intent(this, PlayerListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	 // Since this is an object collection, use a FragmentStatePagerAdapter,
	 // and NOT a FragmentPagerAdapter.
	 public class PlayersCollectionPagerAdapter extends FragmentStatePagerAdapter {
	     public PlayersCollectionPagerAdapter(FragmentManager fm) {
	         super(fm);
	     }
	
	     @Override
	     public Fragment getItem(int i) {
	         Fragment fragment = new PlayerDetailFragment();
	         Bundle args = new Bundle();
	         args.putString(PlayerDetailFragment.ARG_ITEM_ID, Integer.valueOf(i).toString());
	         fragment.setArguments(args);
	         return fragment;
	     }
	
	     @Override
	     public int getCount() {
	         return 5;
	     }
	
	     @Override
	     public CharSequence getPageTitle(int position) {
	         return "Player " + (position + 1);
	     }
	 }

	@Override
	public void onNumberClicked(String number) {
		Log.d(TAG, "onNumberClicked: " + number);
		TextView v = (TextView) mViewPager
		        .findViewWithTag(String.valueOf(mViewPager.getCurrentItem()));
		v.setText(number);
	}

	@Override
	public void onDeleteClicked() {
		Log.d(TAG, "onDeleteClicked");
	}

	@Override
	public void onEnterClicked() {
		Log.d(TAG, "onEnterClicked");
	}
	
	@Override
	public void onSignClicked(boolean positive) {
		Log.d(TAG, "onSignClicked: " + (positive ? "+" : "-"));
	}
	
	private static final String TAG = "PlayerDetailActivity";
}
