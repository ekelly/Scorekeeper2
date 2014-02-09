package net.erickelly.scorekeeper;

import java.util.Arrays;

import net.erickelly.scorekeeper.data.PlayerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;

public class PlayerNameDialogFragment extends DialogFragment {

	/**
	 * Create a new dialog prompting for a player name
	 * 
	 * @param listener
	 * @return
	 */
	public static PlayerNameDialogFragment newInstance(
			PlayerNamePromptListener listener) {
		return newInstance(listener, null);
	}

	/**
	 * Create a new dialog prompting for a player name
	 * 
	 * @param listener
	 * @param text
	 * @return
	 */
	public static PlayerNameDialogFragment newInstance(
			PlayerNamePromptListener listener, String text) {
		if (listener == null) {
			listener = mDummyListener;
		}
		if (text != null) {
			prefill = text;
		}
		PlayerNameDialogFragment d = new PlayerNameDialogFragment();
		d.setListener(listener);
		return d;
	}

	/**
	 * Set this fragment's listener to the given listener
	 * 
	 * @param listener
	 */
	private void setListener(PlayerNamePromptListener listener) {
		mListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.dialog_fragment_new_player,
				null);
		EditText nameInput = (EditText) v.findViewById(R.id.dialog_player_name);

		// EditText stuff
		String editTextInitialContent;
		if (!prefill.isEmpty()) {
			Log.d(TAG, "using prefill: " + prefill);
			editTextInitialContent = prefill;
		} else {
			int numPlayers = PlayerManager.getInstance().getPlayerCount(
					getActivity());
			editTextInitialContent = "Player " + (numPlayers + 1);
			Log.d(TAG, "using new name: " + editTextInitialContent);
		}
		nameInput.setText(editTextInitialContent);
		nameInput.setSelection(editTextInitialContent.length());
		builder.setTitle(getResources().getString(R.string.player_name))
				.setView(v)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								EditText nameInput = (EditText) v
										.findViewById(R.id.dialog_player_name);
								mListener.onPlayerNameEntry(nameInput.getText()
										.toString().trim());
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
		Dialog d = builder.create();
		d.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return d;
	}

	/**
	 * Interface for the PlayerNameDialogFragment to communicate back with its
	 * host activity
	 */
	public interface PlayerNamePromptListener {
		public void onPlayerNameEntry(String name);
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private static class ColorPickerAdapter extends ArrayAdapter {
		private Activity mActivity;
		private int[] mColors;

		@SuppressWarnings("unchecked")
		public ColorPickerAdapter(Activity activity, int resource, int[] colors) {
			super(activity, resource, Arrays.asList(colors));
			mActivity = activity;
			mColors = colors;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		private View getCustomView(int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			FrameLayout colorItem = (FrameLayout) inflater.inflate(
					R.layout.color_item, parent);
			colorItem.setBackgroundColor(mActivity.getResources().getColor(
					mColors[position]));
			return colorItem;
		}
	}

	private static PlayerNamePromptListener mDummyListener = new PlayerNamePromptListener() {
		@Override
		public void onPlayerNameEntry(String name) {
		}
	};

	private static String prefill = "";

	private PlayerNamePromptListener mListener;

	private static final String TAG = "PlayerNameDialogFragment";
}
