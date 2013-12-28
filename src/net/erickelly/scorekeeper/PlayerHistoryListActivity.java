package net.erickelly.scorekeeper;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PlayerHistoryListActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_history);
		
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.player_history_fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			PlayerHistoryListFragment fragment = new PlayerHistoryListFragment();

			long id = getIntent().getExtras().getLong(
					PlayerHistoryListFragment.ARG_PLAYER_ID);

			Bundle args = new Bundle();
			args.putLong(PlayerHistoryListFragment.ARG_PLAYER_ID, id);
			fragment.setArguments(args);

			// Add the fragment to the 'container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.player_history_fragment_container, fragment)
					.commit();
		}
	}
}
