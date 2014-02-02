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
public class PlayerDetailFragment extends Fragment implements NumpadListener {
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

		if (getArguments().containsKey(ARG_RETURN_TO_LIST)) {
			mReturnToList = getArguments().getBoolean(ARG_RETURN_TO_LIST);
		}

		// Are we using the notes area or not?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mUsingNotes = prefs.getBoolean(SettingsFragment.PREF_NOTES, false);
		Log.d(TAG, mUsingNotes ? "Using notes" : "Not using notes");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		if (mUsingNotes) {
			mDetailView = (LinearLayout) inflater.inflate(
					R.layout.fragment_player_detail, container, false);
		} else {
			mDetailView = (LinearLayout) inflater.inflate(
					R.layout.fragment_player_detail_no_notes, container, false);
		}

		mScoreContainerView = (FrameLayout) mDetailView
				.findViewById(R.id.score_container);

		mLargeScoreView = (TextView) mDetailView
				.findViewById(R.id.player_score_large);
		mScoreView = (TextView) mDetailView.findViewById(R.id.player_detail);
		mTotalScoreView = (TextView) mDetailView.findViewById(R.id.total_score);
		mSignView = (TextView) mDetailView.findViewById(R.id.plus_minus);
		mAdjustAmtView = (TextView) mDetailView.findViewById(R.id.adjust_amt);
		mPlayerEditScoreView = (LinearLayout) mDetailView
				.findViewById(R.id.player_edit_score);

		if (mPlayer != null) {
			setScore(mPlayer.getScore());
			setNotesArea(mPlayer.getLastNotesField());
			mDetailView.setTag(mPlayer.getId());
		}

		if (mUsingNotes) {
			mNotesContainerView = (LinearLayout) mDetailView
					.findViewById(R.id.notes_container);
			mNotesView = (TextView) mDetailView.findViewById(R.id.score_notes);
			mNotesContainerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onFocusChanged(v);
				}
			});

			mScoreContainerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onFocusChanged(v);
				}
			});
		}

		mScoreContainerView.setSelected(true);

		return mDetailView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, "onViewCreated");

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle args = new Bundle();
			NumpadFragment fragment = new NumpadFragment();
			args.putBoolean(NumpadFragment.ARG_POS_NEG, mSign.isPositive());
			fragment.setArguments(args);
			getChildFragmentManager().beginTransaction()
					.add(R.id.numpad_container, fragment, NUMPAD_FRAGMENT)
					.commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getNumpadFragment().registerListener(this);
	}

	/**
	 * Reset the displayed score to the player's actual current score
	 */
	public void clear() {
		Log.d(TAG, "clear");
		refreshPlayer();
		setScore(mPlayer.getScore());
		setAdjustAmt(0);
		setFinalScore(mPlayer.getScore());
		setNotesArea(mPlayer.getLastNotesField());
		setPlayerScoreVisibility(true);
	}

	/**
	 * Resets the state of the Activity
	 */
	public void reset() {
		Log.d(TAG, "reset");
		// Notes
		mNotes = getPlayer().getLastNotesField();
		if (mNotes == null)
			mNotes = "";
		setNotes(mNotes); // TODO: Do I really need this line?
		refreshPlayer();

		// Adjust amt
		mAdjustAmount = "";
		setSign(Sign.POSITIVE);

		// Focus resets
		setFocus(mFocus);
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
		if (mScoreView != null) {
			String scoreText = String.valueOf(score);
			mScoreView.setText(scoreText);
			mLargeScoreView.setText(scoreText);
		}
	}

	/**
	 * Display the correct sign for the operation
	 * 
	 * @param positive
	 */
	private void setSign(boolean positive) {
		Log.d(TAG, "setSign: " + mSign);
		if (mSignView != null) {
			// mSignView.setText(positive ? "+" : "-");
			mSignView.setText(mSign.toString());
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
		if (mAdjustAmtView != null) {
			mAdjustAmtView.setText(String.valueOf(Math.abs(amt)));
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
		if (mTotalScoreView != null) {
			mTotalScoreView.setText(String.valueOf(finalScore));
		}
	}

	/**
	 * Display the contents of the notes
	 * 
	 * @param notes
	 */
	private void setNotesArea(String notes) {
		Log.d(TAG, "setNotesArea: " + notes);
		if (mNotesView != null) {
			mNotesView.setText(notes);
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
		if (mPlayerEditScoreView != null && mLargeScoreView != null) {
			mPlayerEditScoreView.setVisibility(visible ? View.GONE
					: View.VISIBLE);
			mLargeScoreView.setVisibility(visible ? View.VISIBLE : View.GONE);
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
		if (mScoreContainerView != null && mNotesContainerView != null) {
			mScoreContainerView.setSelected(focus.equals(ActionFocus.SCORE));
			mNotesContainerView.setSelected(focus.equals(ActionFocus.NOTES));
		}
		mFocus = focus;
		getNumpadFragment().setUndoText(focus);
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
	}

	/**
	 * Return the numpad fragment
	 * 
	 * @return
	 */
	public NumpadFragment getNumpadFragment() {
		return (NumpadFragment) getChildFragmentManager().findFragmentByTag(
				NUMPAD_FRAGMENT);
	}

	@Override
	public void onNumberClicked(String number) {
		Log.d(TAG, "onNumberClicked: " + number);
		if (mFocus.equals(ActionFocus.SCORE)) {
			mAdjustAmount += number;
			Integer amt = getCurrentAdjustAmount();
			adjustScore(amt);
		} else {
			mNotes += number;
			setNotes(mNotes);
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
				adjustScore(amt);
			}
		} else {
			if (mNotes.length() > 0) {
				mNotes = mNotes.substring(0, mNotes.length() - 1);
				setNotes(mNotes);
				// updateNote(mNotes);
			}
		}
	}

	@Override
	public void onEnterClicked() {
		Log.d(TAG, "onEnterClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			if (adjustScore()) {
				clear();
				reset();
				mUpdate = false;
			}
		} else {
			updateNote(mNotes);
			reset();
		}
		if (mReturnToList) {
			getActivity().finish();
		}
	}

	@Override
	public void onSignClicked(Sign sign) {
		Log.d(TAG, "onSignClicked: " + sign.toString());
		if (mFocus.equals(ActionFocus.SCORE)) {
			mSign = sign;
			adjustScore(getCurrentAdjustAmount());
		}
	}

	@Override
	public void onHistoryClicked() {
		Log.d(TAG, "onHistoryClicked");
		long id = getPlayer().getId();
		Intent i = new Intent(getActivity(), PlayerHistoryListActivity.class);
		i.putExtra(PlayerHistoryListFragment.ARG_PLAYER_ID, id);
		startActivity(i);
	}

	@Override
	public void onUndoClicked() {
		Log.d(TAG, "onUndoClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			PlayerManager
					.undoLastAdjustment(getActivity(), getPlayer().getId());
			clear();
		} else {
			resetNotes();
		}
	}

	/**
	 * Update the current player's note
	 * 
	 * @param note
	 */
	private void updateNote(String note) {
		updateNote(getPlayer().getId(), note);
	}

	/**
	 * Update the note associated with the given player id
	 * 
	 * @param playerId
	 * @param note
	 */
	private void updateNote(long playerId, String note) {
		Log.d(TAG, "updateNote: " + playerId + ", " + note);
		PlayerManager.updateScoreNotes(getActivity(), playerId, note, mUpdate);
		mUpdate = true;
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
	 * Adjust the score to the "current" adjust amount
	 */
	private boolean adjustScore() {
		Log.d(TAG, "adjustScore");
		Integer adjustAmt = getCurrentAdjustAmount();
		if (adjustAmt != null) {
			Player p = getPlayer();
			PlayerManager.adjustScore(getActivity(), p.getId(), adjustAmt,
					mNotes, mUpdate);
			mUpdate = true;
			return true;
		}
		return false;
	}

	/**
	 * Set the notes field to an empty string and show it
	 */
	private void resetNotes() {
		mNotes = "";
		setNotes(mNotes);
	}

	/**
	 ******************************************************************** 
	 * The below functions pass on values to the Numpad Fragment *
	 ******************************************************************** 
	 */

	/**
	 * Sets the sign
	 * 
	 * @author eric
	 */
	public void setSign(Sign sign) {
		mSign = sign;
	}

	/**
	 * The fragment argument representing the player ID that this fragment
	 * represents.
	 */
	public static final String ARG_PLAYER_ID = "player_id";

	/**
	 * Should the activity return to the player screen when enter is clicked?
	 */
	public static final String ARG_RETURN_TO_LIST = "return";

	/**
	 * The Player which is being shown
	 */
	private Player mPlayer;
	private LinearLayout mDetailView;
	private LinearLayout mPlayerEditScoreView;
	private LinearLayout mNotesContainerView;
	private FrameLayout mScoreContainerView;
	private TextView mScoreView;
	private TextView mTotalScoreView;
	private TextView mSignView;
	private TextView mAdjustAmtView;
	private TextView mNotesView;
	private TextView mLargeScoreView;

	private String mNotes = "";
	private String mAdjustAmount = "";
	private Sign mSign = Sign.POSITIVE;
	private boolean mReturnToList = false;
	private boolean mUpdate = false; // Insert/Update a new record
	private ActionFocus mFocus = ActionFocus.SCORE;
	private boolean mUsingNotes = false;

	private static final String NUMPAD_FRAGMENT = "numpad";

	private static final String TAG = "PlayerDetailFragment";
}