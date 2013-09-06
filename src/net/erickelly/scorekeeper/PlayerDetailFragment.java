package net.erickelly.scorekeeper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.erickelly.scorekeeper.data.Player;
import net.erickelly.scorekeeper.data.PlayerManager;

/**
 * A fragment representing a single Player detail screen. This fragment is
 * either contained in a {@link PlayerListActivity} in two-pane mode (on
 * tablets) or a {@link PlayerDetailActivity} on handsets.
 */
public class PlayerDetailFragment extends Fragment {
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PlayerDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		if (getArguments().containsKey(ARG_PLAYER_ID)) {
			long id = getArguments().getLong(ARG_PLAYER_ID);
			Log.d(TAG, "Creating fragment for Player@" + id);
			mPlayer = PlayerManager.getInstance().getPlayer(getActivity(), id);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		TextView scoreView = (TextView) inflater.inflate(
				R.layout.fragment_player_detail, container, false);

		if (mPlayer != null) {
			scoreView.setText(String.valueOf(mPlayer.getScore()));
			scoreView.setTag(mPlayer.getId());
		}

		return scoreView;
	}

	/**
	 * Reset the displayed score to the player's actual current score
	 */
	public void clear() {
		mPlayer = PlayerManager.getInstance().getPlayer(getActivity(),
				mPlayer.getId());
		setText(String.valueOf(mPlayer.getScore()));
	}

	/**
	 * Set the textview to display the given text
	 * @param text
	 */
	private void setText(String text) {
		Log.d(TAG, "setText");
		if (getView() != null) {
			((TextView) getView()).setText(text);
		}
	}

	/**
	 * Display the score adjust amount
	 * 
	 * @param amt
	 */
	public void adjustScore(int amt) {
		String content = "" + mPlayer.getScore();
		if (amt != 0) {
			content += amt < 0 ? " - " : " + ";
			content += Math.abs(amt);
			content += " = ";
			content += (mPlayer.getScore() + amt);
		}
		setText(content);
	}

	/**
	 * The fragment argument representing the player ID that this fragment
	 * represents.
	 */
	public static final String ARG_PLAYER_ID = "player_id";

	/**
	 * The Player which is being shown
	 */
	private Player mPlayer;

	private static final String TAG = "PlayerDetailFragment";
}