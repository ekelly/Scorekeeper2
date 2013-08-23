package net.erickelly.scorekeeper;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.erickelly.scorekeeper.dummy.DummyContent;

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
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

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
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
        	int id = getArguments().getInt(ARG_PLAYER_ID);
        	mItem = new DummyContent.DummyItem(id, "Item #: " + id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.player_detail)).setText(mItem.content);
        }
        
        rootView.setTag(mItem.id);

        return rootView;
    }
    
    public DummyContent.DummyItem getItem() {
    	return mItem;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	Log.d(TAG, "onAttach");
    }
    
    private static final String TAG = "PlayerDetailFragment";
}