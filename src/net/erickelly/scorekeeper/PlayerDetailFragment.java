package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.data.ActionFocus;
import net.erickelly.scorekeeper.data.Player;
import net.erickelly.scorekeeper.data.PlayerManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A fragment representing a single Player detail screen. This fragment is
 * either contained in a {@link PlayerListActivity} in two-pane mode (on
 * tablets) or a {@link PlayerDetailActivity} on handsets. Activities using this
 * fragment must implement its Callbacks interface
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
			mPlayer = PlayerManager.getPlayer(getActivity(), id);
		}
		
		if (getArguments().containsKey(ARG_NOTES)) {
			mUsingNotes = getArguments().getBoolean(ARG_NOTES);
			Log.d(TAG, mUsingNotes ? "Using notes" : "Not using notes");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		if (mUsingNotes) {
			detailView = (LinearLayout) inflater.inflate(
					R.layout.fragment_player_detail, container, false);
		} else {
			detailView = (LinearLayout) inflater.inflate(
					R.layout.fragment_player_detail_no_notes, container, false);
		}

		scoreContainerView = (FrameLayout) detailView
				.findViewById(R.id.score_container);

		largeScoreView = (TextView) detailView
				.findViewById(R.id.player_score_large);
		scoreView = (TextView) detailView.findViewById(R.id.player_detail);
		totalScoreView = (TextView) detailView.findViewById(R.id.total_score);
		signView = (TextView) detailView.findViewById(R.id.plus_minus);
		adjustAmtView = (TextView) detailView.findViewById(R.id.adjust_amt);
		playerEditScoreView = (LinearLayout) detailView
				.findViewById(R.id.player_edit_score);

		if (mPlayer != null) {
			setScore(mPlayer.getScore());
			setNotesArea(mPlayer.getLastNotesField());
			detailView.setTag(mPlayer.getId());
		}

		if (mUsingNotes) {
			notesContainerView = (LinearLayout) detailView
					.findViewById(R.id.notes_container);
			notesView = (TextView) detailView.findViewById(R.id.score_notes);
			notesContainerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onFocusChanged(v);
				}
			});
			
			scoreContainerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onFocusChanged(v);
				}
			});
		}
		
		scoreContainerView.setSelected(true);

		return detailView;
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

	/**
	 * Reset the displayed score to the player's actual current score
	 */
	public void clear() {
		refreshPlayer();
		setScore(mPlayer.getScore());
		setAdjustAmt(0);
		setFinalScore(mPlayer.getScore());
		setNotesArea(mPlayer.getLastNotesField());
		setPlayerScoreVisibility(true);
	}
	
	/**
	 * Reloads the player with the latest information from the database
	 */
	public void refreshPlayer() {
		mPlayer = PlayerManager.getPlayer(getActivity(), mPlayer.getId());
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
	 */
	public void adjustScore(Integer amt) {
		setScore(mPlayer.getScore());
		if (amt != null) {
			setAdjustAmt(amt);
			setFinalScore(mPlayer.getScore() + amt);
			setSign(amt >= 0);
			setPlayerScoreVisibility(false);
		} else {
			setPlayerScoreVisibility(true);
		}
	}

	/**
	 * Display the notes
	 */
	public void setNotes(String notes) {
		setNotesArea(notes);
	}

	/**
	 * Returns the player associated with this detail fragment
	 */
	public Player getPlayer() {
		return mPlayer;
	}

	/**
	 * Set the focus to the given focus
	 * 
	 * @param focus
	 */
	public void setFocus(ActionFocus focus) {
		Log.d(TAG, "setFocus: " + focus);
		if (scoreContainerView != null && notesContainerView != null) {
			scoreContainerView.setSelected(focus.equals(ActionFocus.SCORE));
			notesContainerView.setSelected(focus.equals(ActionFocus.NOTES));
		}
	}

	/**
	 * When a user taps a particular part of the DetailView, this function is
	 * called. It allows the user to select between typing notes and adjusting a
	 * player's score
	 * 
	 * @param v
	 */
	public void onFocusChanged(View v) {
		// Change the "selected" background color
		ActionFocus focus = (v.getId() == R.id.score_container) ? ActionFocus.SCORE
				: ActionFocus.NOTES;
		setFocus(focus);
		mCallbacks.onSwitchFocus(focus);
	}

	public interface Callbacks {
		/**
		 * When a user focuses on one action or another (entering notes or
		 * entering score), this function will be triggered
		 * 
		 * @param focus
		 */
		public void onSwitchFocus(ActionFocus focus);
	}

	private static final Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onSwitchFocus(ActionFocus focus) {
		}
	};

	private static Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The fragment argument representing the player ID that this fragment
	 * represents.
	 */
	public static final String ARG_PLAYER_ID = "player_id";
	
	/**
	 * The fragment argument representing whether the notes/bidding area
	 * will be displayed
	 */
	public static final String ARG_NOTES = "notes";

	/**
	 * The Player which is being shown
	 */
	private Player mPlayer;
	private LinearLayout detailView;
	private LinearLayout playerEditScoreView;
	private LinearLayout notesContainerView;
	private FrameLayout scoreContainerView;
	private TextView scoreView;
	private TextView totalScoreView;
	private TextView signView;
	private TextView adjustAmtView;
	private TextView notesView;
	private TextView largeScoreView;
	
	private boolean mUsingNotes = false;

	private static final String TAG = "PlayerDetailFragment";
}