package net.erickelly.scorekeeper;

import android.app.Activity;
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
 * A fragment representing a single Player detail screen.
 * This fragment is either contained in a {@link PlayerListActivity}
 * in two-pane mode (on tablets) or a {@link PlayerDetailActivity}
 * on handsets.
 */
public class PlayerDetailFragment extends Fragment {
    /**
     * The fragment argument representing the player ID that this fragment
     * represents.
     */
    public static final String ARG_PLAYER_ID = "player_id";
    
    /**
     * The Player which is being shown
     */
    private Player mPlayer;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_PLAYER_ID)) {
        	int id = getArguments().getInt(ARG_PLAYER_ID);
        	mPlayer = PlayerManager.getInstance().getPlayer(getActivity(), id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mPlayer != null) {
            ((TextView) rootView.findViewById(R.id.player_detail))
            	.setText(String.valueOf(mPlayer.getScore()));
            rootView.setTag(mPlayer.getId());
        }

        return rootView;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	Log.d(TAG, "onAttach");
    }
    
    private static final String TAG = "PlayerDetailFragment";
}