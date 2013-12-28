package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.data.ActionFocus;
import net.erickelly.scorekeeper.data.Sign;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * The fragment allowing number input
 * 
 * @author eric
 * 
 */
public class NumpadFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		if (getArguments() != null && getArguments().containsKey(ARG_POS_NEG)) {
			mSign = Sign.valueOf(getArguments().getBoolean(ARG_POS_NEG, true));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_numpad, container,
				false);

		Button oneButton = (Button) rootView.findViewById(R.id.one);
		Button twoButton = (Button) rootView.findViewById(R.id.two);
		Button threeButton = (Button) rootView.findViewById(R.id.three);
		Button fourButton = (Button) rootView.findViewById(R.id.four);
		Button fiveButton = (Button) rootView.findViewById(R.id.five);
		Button sixButton = (Button) rootView.findViewById(R.id.six);
		Button sevenButton = (Button) rootView.findViewById(R.id.seven);
		Button eightButton = (Button) rootView.findViewById(R.id.eight);
		Button nineButton = (Button) rootView.findViewById(R.id.nine);
		Button zeroButton = (Button) rootView.findViewById(R.id.zero);

		ImageButton delete = (ImageButton) rootView.findViewById(R.id.delete);
		Button enter = (Button) rootView.findViewById(R.id.enter);
		Button history = (Button) rootView.findViewById(R.id.history);
		Button undo = (Button) rootView.findViewById(R.id.undo);

		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String number = (String) ((Button) v).getText();
				onNumberClicked(number);
			}
		};
		oneButton.setOnClickListener(listener);
		twoButton.setOnClickListener(listener);
		threeButton.setOnClickListener(listener);
		fourButton.setOnClickListener(listener);
		fiveButton.setOnClickListener(listener);
		sixButton.setOnClickListener(listener);
		sevenButton.setOnClickListener(listener);
		eightButton.setOnClickListener(listener);
		nineButton.setOnClickListener(listener);
		zeroButton.setOnClickListener(listener);

		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDeleteClicked();
			}
		});
		enter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onEnterClicked();
			}
		});
		history.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onHistoryClicked();
			}
		});
		undo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onUndoClicked();
			}
		});

		plusMinus = (Button) rootView.findViewById(R.id.plus_minus);
		plusMinus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSignClicked();
			}
		});
		refreshOperationSign();

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof NumpadListener)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (NumpadListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NumpadFragment() {
	}

	/**
	 * Set the numpad to add or subtract
	 * 
	 * @param positive
	 */
	public void setOperationSign(Sign sign) {
		Log.d(TAG, "setOperationSign");
		mSign = sign;
		refreshOperationSign();
	}

	/**
	 * Set the text of the undo button to the appropriate label given the
	 * current focus
	 * 
	 * @param focus
	 */
	public void setUndoText(ActionFocus focus) {
		Button undo = (Button) getView().findViewById(R.id.undo);
		switch (focus) {
		case NOTES:
			undo.setText(getResources().getString(R.string.clear));
			break;
		default:
			undo.setText(getResources().getString(R.string.undo));
		}
	}

	/**
	 * Display the +/- sign depending on whether or not the numpad is positive
	 */
	private void refreshOperationSign() {
		Log.d(TAG, "refreshOperationSign");
		plusMinus.setText(mSign.toString());
		Log.d(TAG, "setting sign to: " + mSign.toString());
		switch (mSign) {
		case NEGATIVE:
			plusMinus.setTextColor(getResources().getColor(R.color.red));
			break;
		default:
			plusMinus.setTextColor(getResources().getColor(R.color.green));
		}
	}

	/**
	 * This function is called when a number is clicked, and allows the fragment
	 * to handle the event before passing it up to the attached activity
	 * 
	 * @param number
	 *            The String representation of the number which was clicked
	 */
	private void onNumberClicked(String number) {
		Log.d(TAG, "onNumberClicked: " + number);
		mCallbacks.onNumberClicked(number);
	}

	/**
	 * This function is called when delete is clicked, and allows the fragment
	 * to handle the event before passing it up to the attached activity
	 */
	private void onDeleteClicked() {
		Log.d(TAG, "onDeleteClicked");
		mCallbacks.onDeleteClicked();
	}

	/**
	 * This function is called when enter is clicked, and allows the fragment to
	 * handle the event before passing it up to the attached activity
	 */
	private void onEnterClicked() {
		Log.d(TAG, "onEnterClicked");
		mCallbacks.onEnterClicked();
	}

	/**
	 * This function is called when the sign button is clicked, and allows the
	 * fragment to handle the event before passing it up to the attached
	 * activity
	 */
	private void onSignClicked() {
		Log.d(TAG, "onSignClicked: " + mSign.inverse().toString());
		setOperationSign(mSign.inverse());
		mCallbacks.onSignClicked(mSign);
	}

	/**
	 * This function is called when the sign button is clicked, and allows the
	 * fragment to handle the event before passing it up to the attached
	 * activity
	 */
	private void onHistoryClicked() {
		Log.d(TAG, "onHistoryClicked");
		mCallbacks.onHistoryClicked();
	}

	/**
	 * This function is called when the undo button is clicked, and allows the
	 * fragment to handle the event before passing it up to the attached
	 * activity
	 */
	private void onUndoClicked() {
		Log.d(TAG, "onUndoClicked");
		mCallbacks.onUndoClicked();
	}

	/**
	 * Activities containing a numpad must implement NumpadListener to listen
	 * for numpad events
	 * 
	 * @author eric
	 * 
	 */
	public interface NumpadListener {
		/**
		 * When any number is clicked, this function will be called and provided
		 * with the string representation of the clicked number
		 * 
		 * @param number
		 *            The String representation of the tapped number
		 */
		public void onNumberClicked(String number);

		/**
		 * When the delete button is clicked, this function will be called
		 */
		public void onDeleteClicked();

		/**
		 * When the enter button is clicked, this function will be called
		 */
		public void onEnterClicked();

		/**
		 * When the change sign button is clicked, this number is called with
		 * the new sign
		 * 
		 * @param sign
		 */
		public void onSignClicked(Sign sign);

		/**
		 * When the history button is clicked, used to see a player's history
		 */
		public void onHistoryClicked();

		/**
		 * When the undo button is clicked, this function is called
		 */
		public void onUndoClicked();
	}

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_POS_NEG = "pos_neg";

	/**
	 * The sign of the keypad (default positive)
	 */
	private Sign mSign = Sign.POSITIVE;

	/**
	 * Plus/Minus button reference
	 */
	private Button plusMinus;

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private NumpadListener mCallbacks = sDummyCallbacks;

	/**
	 * A dummy implementation of the {@link NumpadListener} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static NumpadListener sDummyCallbacks = new NumpadListener() {

		@Override
		public void onNumberClicked(String number) {
		}

		@Override
		public void onDeleteClicked() {
		}

		@Override
		public void onEnterClicked() {
		}

		@Override
		public void onSignClicked(Sign sign) {
		}

		@Override
		public void onHistoryClicked() {
		}

		@Override
		public void onUndoClicked() {
		}
	};

	private static String TAG = "NumpadFragment";
}
