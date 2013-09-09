package net.erickelly.scorekeeper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
		detailView = (LinearLayout) inflater.inflate(
				R.layout.fragment_player_detail, container, false);

		largeScoreView = (TextView) detailView
				.findViewById(R.id.player_score_large);
		scoreView = (TextView) detailView.findViewById(R.id.player_detail);
		totalScoreView = (TextView) detailView.findViewById(R.id.total_score);
		signView = (TextView) detailView.findViewById(R.id.plus_minus);
		adjustAmtView = (TextView) detailView.findViewById(R.id.adjust_amt);
		notesView = (TextView) detailView.findViewById(R.id.score_notes);
		playerEditScoreView = (LinearLayout) detailView
				.findViewById(R.id.player_edit_score);

		if (mPlayer != null) {
			setScore(mPlayer.getScore());
			detailView.setTag(mPlayer.getId());
		}

		return detailView;
	}

	/**
	 * Reset the displayed score to the player's actual current score
	 */
	public void clear() {
		mPlayer = PlayerManager.getInstance().getPlayer(getActivity(),
				mPlayer.getId());
		setScore(mPlayer.getScore());
		setAdjustAmt(0);
		setFinalScore(mPlayer.getScore());
		setPlayerScoreVisibility(true);
	}

	/**
	 * Display the "starting" score
	 * 
	 * @param score
	 */
	private void setScore(int score) {
		Log.d(TAG, "setScore: " + score);
		if (scoreView != null) {
			String scoreText = String.valueOf(score);
			scoreView.setText(scoreText);
			largeScoreView.setText(scoreText);
		}
	}

	/**
	 * Display the correct sign for the operation
	 * 
	 * @param positive
	 */
	private void setSign(boolean positive) {
		Log.d(TAG, "setSign: " + positive);
		if (signView != null) {
			signView.setText(positive ? "+" : "-");
		}
	}

	/**
	 * Display the adjust amount. If the adjust amount is zero, it will not
	 * display any text
	 * 
	 * @param amt
	 */
	private void setAdjustAmt(int amt) {
		Log.d(TAG, "setAdjustAmt: " + amt);
		if (adjustAmtView != null) {
			adjustAmtView.setText(String.valueOf(Math.abs(amt)));
		}
	}

	/**
	 * Display the final score. If the given score is the same as mPlayer's
	 * current score, it will not display any text
	 * 
	 * @param finalScore
	 */
	private void setFinalScore(int finalScore) {
		Log.d(TAG, "setFinalScore: " + finalScore);
		if (totalScoreView != null) {
			totalScoreView.setText(String.valueOf(finalScore));
		}
	}

	/**
	 * Display the contents of the notes
	 * 
	 * @param notes
	 */
	@SuppressWarnings("unused")
	private void setNotesArea(String notes) {
		Log.d(TAG, "setNotesArea: " + notes);
		if (notesView != null) {
			notesView.setText(notes);
		}
	}

	/**
	 * Swap showing the "editing" a score view and displaying a score view
	 * 
	 * @param visible
	 *            If true, the "large" view is visible. If false, the "edit"
	 *            view is visible
	 */
	private void setPlayerScoreVisibility(boolean visible) {
		Log.d(TAG, "setPlayerScoreVisibility: " + visible);
		if (playerEditScoreView != null && largeScoreView != null) {
			playerEditScoreView.setVisibility(visible ? View.GONE
					: View.VISIBLE);
			largeScoreView.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Display the score adjust amount
	 * 
	 * @param amt
	 * @param true if the sign is +, false if the sign is - This parameter
	 *        cannot be guessed from the sign of the amt due to the 0 case
	 */
	public void adjustScore(Integer amt, boolean sign) {
		setScore(mPlayer.getScore());
		if (amt != null) {
			setAdjustAmt(amt);
			setFinalScore(mPlayer.getScore() + amt);
			setSign(sign);
			setPlayerScoreVisibility(false);
		} else {
			setPlayerScoreVisibility(true);
		}
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
	private LinearLayout detailView;
	private LinearLayout playerEditScoreView;
	private TextView scoreView;
	private TextView totalScoreView;
	private TextView signView;
	private TextView adjustAmtView;
	private TextView notesView;
	private TextView largeScoreView;

	private static final String TAG = "PlayerDetailFragment";
}