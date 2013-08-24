package net.erickelly.scorekeeper;

import net.erickelly.scorekeeper.data.PlayerManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class PlayerNameDialogFragment extends DialogFragment {
	
	/**
	 * Interface for the PlayerNameDialogFragment to communicate
	 * back with its host activity
	 */
	public interface PlayerNamePromptListener {
		public void onPlayerNameEntry(String name);
	}
	
	private static PlayerNamePromptListener mDummyListener = new PlayerNamePromptListener() {
		@Override
		public void onPlayerNameEntry(String name) {
		}
	};
	
	private static String prefill = "";
	
	private PlayerNamePromptListener mListener;
	
	static PlayerNameDialogFragment newInstance(PlayerNamePromptListener listener) {
		return newInstance(listener, null);
	}
	
	static PlayerNameDialogFragment newInstance(PlayerNamePromptListener listener, String text) {
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
	
	private void setListener(PlayerNamePromptListener listener) {
		mListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_fragment_new_player, null);
        EditText nameInput = (EditText) v.findViewById(R.id.dialog_player_name);
        if (!prefill.isEmpty()) {
        	Log.d(TAG, "using prefill: " + prefill);
        	nameInput.setText(prefill);
        } else {
	        int numPlayers = PlayerManager.getInstance().getPlayerCount(getActivity());
	        nameInput.setText("Player " + (numPlayers + 1));
	        Log.d(TAG, "using new name: Player" + (numPlayers + 1));
        }
        builder.setTitle(getResources().getString(R.string.player_name))
               .setView(v)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   EditText nameInput = (EditText) v.findViewById(R.id.dialog_player_name);
                       mListener.onPlayerNameEntry(nameInput.getText().toString());
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.dismiss();
                   }
               });
        return builder.create();
    }
	
	private static final String TAG = "PlayerNameDialogFragment";
}
