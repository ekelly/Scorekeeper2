package net.erickelly.scorekeeper;

import static net.erickelly.scorekeeper.utils.NumberUtils.willAdditionOverflow;
import net.erickelly.scorekeeper.NumpadFragment.NumpadListener;
import net.erickelly.scorekeeper.data.ActionFocus;
import net.erickelly.scorekeeper.data.Player;
import net.erickelly.scorekeeper.data.PlayerManager;
import net.erickelly.scorekeeper.data.Sign;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import android.widget.Toast;

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
			loadPlayer(id);
		}

		if (getArguments().containsKey(ARG_START_IN_NOTES)) {
			mStartInNotes = getArguments().getBoolean(ARG_START_IN_NOTES);
		}

		integerOverflowToastText = getResources().getString(
				R.string.integer_overflow);

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

			mNotesContainerView = (LinearLayout) mDetailView
					.findViewById(R.id.notes_container);
			mNotesView = (TextView) mDetailView
					.findViewById(R.id.player_detail_bid);
			mNotesContainerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onFocusChanged(v);
				}
			});

			TextView biddingText = (TextView) mDetailView
					.findViewById(R.id.player_detail_bidding);
			Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
					"calligraffiti/calligraffiti.ttf");
			biddingText.setTypeface(font);
			mNotesView.setTypeface(font);

			setNotesArea(mNotes);
		} else {
			mDetailView = (LinearLayout) inflater.inflate(
					R.layout.fragment_player_detail_no_notes, container, false);
		}

		mScoreContainerView = (FrameLayout) mDetailView
				.findViewById(R.id.score_container);

		if (mUsingNotes) {
			mScoreContainerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onFocusChanged(v);
				}
			});
		}

		mLargeScoreView = (TextView) mDetailView
				.findViewById(R.id.player_score_large);
		mScoreView = (TextView) mDetailView.findViewById(R.id.large_score_view);
		mTotalScoreView = (TextView) mDetailView.findViewById(R.id.total_score);
		mSignView = (TextView) mDetailView.findViewById(R.id.plus_minus);
		mAdjustAmtView = (TextView) mDetailView.findViewById(R.id.adjust_amt);
		mPlayerEditScoreView = (LinearLayout) mDetailView
				.findViewById(R.id.player_edit_score);

		if (mPlayer != null) {
			setScore(mPlayer.getScore());
			mDetailView.setTag(mPlayer.getId());
		}

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

		startFocus();
	}

	/**
	 * Whenever we return to the PlayerDetail, we want to start the focus in the
	 * right place
	 */
	private void startFocus() {
		boolean startInNotes = mStartInNotes && mNotes.equals("");
		setFocus(startInNotes ? ActionFocus.NOTES : ActionFocus.SCORE);
	}

	/**
	 * Resets the state of the Activity
	 */
	public void reset(boolean updateFocusAndSign) {
		Log.d(TAG, "reset");
		new ResetAsyncTask(this).execute(updateFocusAndSign);
	}

	public void reset() {
		reset(true);
	}

	/**
	 * Reloads the player with the latest information from the database
	 */
	public void refreshPlayer() {
		loadPlayer(mPlayer.getId());
	}

	/**
	 * Loads the player with the given id
	 * 
	 * @param id
	 */
	public void loadPlayer(long id) {
		mPlayer = PlayerManager.getPlayer(getActivity(), id);
		mUpdate = mPlayer.shouldUpdate();
		mNotes = mUpdate ? mPlayer.getLastNotesField() : "";
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
		Log.d(TAG, "adjustScore: " + amt);
		setScore(mPlayer.getScore());
		if (amt != null) {
			if (!willAdditionOverflow(mPlayer.getScore(), amt)) {
				setAdjustAmt(amt);
				setFinalScore(mPlayer.getScore() + amt);
				setSign(amt >= 0);
				setPlayerScoreVisibility(false);
			} else {
				Toast.makeText(getActivity(), integerOverflowToastText,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			setPlayerScoreVisibility(true);
		}
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
		// When not using notes, this is the only valid focus
		if (!mUsingNotes) {
			focus = ActionFocus.SCORE;
		}
		if (mScoreContainerView != null) {
			mScoreContainerView.setSelected(focus.equals(ActionFocus.SCORE));
		}
		if (mNotesContainerView != null) {
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
			String adjustAmt = mAdjustAmount + number;
			Integer amt;
			try {
				amt = getAdjustAmount(adjustAmt);
				if (willAdditionOverflow(mPlayer.getScore(), amt)) {
					throw new NumberFormatException();
				}
				mAdjustAmount = adjustAmt;
			} catch (NumberFormatException e) {
				Toast.makeText(getActivity(), integerOverflowToastText,
						Toast.LENGTH_SHORT).show();
				amt = getCurrentAdjustAmount();
			}
			adjustScore(amt);
		} else {
			mNotes += number;
			setNotesArea(mNotes);
		}
	}

	@Override
	public void onDeleteClicked() {
		Log.d(TAG, "onDeleteClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			if (mAdjustAmount.length() > 0) {
				String adjustAmount = mAdjustAmount.substring(0,
						mAdjustAmount.length() - 1);
				try {
					Integer amt = getAdjustAmount(adjustAmount);
					mAdjustAmount = adjustAmount;
					adjustScore(amt);
				} catch (NumberFormatException e) {
				}
			}
		} else {
			if (mNotes.length() > 0) {
				mNotes = mNotes.substring(0, mNotes.length() - 1);
				setNotesArea(mNotes);
			}
		}
	}

	@Override
	public void onEnterClicked() {
		Log.d(TAG, "onEnterClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			if (adjustScore()) {
				reset();
				mNotes = "";
				setNotesArea(mNotes);
				mUpdate = false;
			}
		} else {
			updateNote(mNotes);
			reset();
			setFocus(ActionFocus.SCORE);
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
		getActivity().overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
	}

	@Override
	public void onUndoClicked() {
		Log.d(TAG, "onUndoClicked");
		if (mFocus.equals(ActionFocus.SCORE)) {
			if (mAdjustAmount.isEmpty()) {
				PlayerManager.undoLastAdjustment(getActivity(), getPlayer()
						.getId());
				reset(false);
			} else {
				reset();
			}
		} else {
			mNotes = "";
			setNotesArea(mNotes);
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
	private Integer getAdjustAmount(String adjustAmount)
			throws NumberFormatException {
		if (!adjustAmount.isEmpty()) {
			Integer adjustAmt = Integer
					.parseInt((mSign.isPositive() ? "" : "-") + adjustAmount);
			return adjustAmt;
		} else {
			return null;
		}
	}

	private Integer getCurrentAdjustAmount() throws NumberFormatException {
		return getAdjustAmount(mAdjustAmount);
	}

	/**
	 * Adjust the score to the "current" adjust amount
	 */
	private boolean adjustScore() {
		Log.d(TAG, "adjustScore");
		try {
			Integer adjustAmt = getCurrentAdjustAmount();
			if (adjustAmt != null) {
				Player p = getPlayer();
				if (!willAdditionOverflow(p.getScore(), adjustAmt)) {
					PlayerManager.adjustScore(getActivity(), p.getId(),
							adjustAmt, mNotes, mUpdate);
					mUpdate = true;
					return true;
				}
			}
		} catch (NumberFormatException e) {
		}
		return false;
	}

	private static class ResetAsyncTask extends
			AsyncTask<Boolean, Void, Boolean> {
		private PlayerDetailFragment mFragment;

		public ResetAsyncTask(PlayerDetailFragment fragment) {
			mFragment = fragment;
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			mFragment.refreshPlayer();
			return params[0];
		}

		@Override
		protected void onPostExecute(Boolean changeFocusAndSign) {
			mFragment.setScore(mFragment.mPlayer.getScore());
			// Adjust amt
			mFragment.mAdjustAmount = "";
			mFragment.setAdjustAmt(0);
			mFragment.setFinalScore(mFragment.mPlayer.getScore());
			mFragment.setNotesArea(mFragment.mNotes);
			mFragment.setPlayerScoreVisibility(true);
			if (changeFocusAndSign) {
				mFragment.setSign(Sign.POSITIVE);
				mFragment.startFocus();
			}
		}
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
		getNumpadFragment().setOperationSign(mSign);
	}

	/**
	 * The fragment argument representing the player ID that this fragment
	 * represents.
	 */
	public static final String ARG_PLAYER_ID = "player_id";

	/**
	 * Should the notes be selected first?
	 */
	public static final String ARG_START_IN_NOTES = "start_in_notes";

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

	private String mNotes;
	private String mAdjustAmount = "";
	private Sign mSign = Sign.POSITIVE;
	private boolean mReturnToList = false;
	private Boolean mUpdate; // Insert/Update a new record
	private ActionFocus mFocus = ActionFocus.SCORE;
	private boolean mUsingNotes = false;
	private boolean mStartInNotes = false;

	private String integerOverflowToastText;
	private static final String NUMPAD_FRAGMENT = "numpad";

	private static final String TAG = "PlayerDetailFragment";
}