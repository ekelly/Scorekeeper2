package net.erickelly.scorekeeper;

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

public class NumpadFragment extends Fragment {
	/**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_POS_NEG = "pos_neg";
    
    /**
     * Whether the keypad should be positive or negative
     * (default positive)
     */
    private boolean mNumpadIsPositive;
    
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
		public void onSignClicked(boolean sign) {
		}
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NumpadFragment() {
    }
    
    /**
     * Set the numpad to add or subtract
     * @param positive
     */
    public void setOperationSign(boolean positive) {
    	Log.d(TAG, "setOperationSign");
    	mNumpadIsPositive = positive;
    	refreshOperationSign();
    }
    
    /**
     * Display the +/- sign depending on whether or not the numpad is positive
     */
    private void refreshOperationSign() {
    	Log.d(TAG, "refreshOperationSign");
    	if (!mNumpadIsPositive) {
        	plusMinus.setText(getResources().getString(R.string.minus));
        	plusMinus.setTextColor(getResources().getColor(R.color.red));
        } else {
        	plusMinus.setText(getResources().getString(R.string.plus));
        	plusMinus.setTextColor(getResources().getColor(R.color.green));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (getArguments() != null && getArguments().containsKey(ARG_POS_NEG)) {
            mNumpadIsPositive = getArguments().getBoolean(ARG_POS_NEG, true);
        }
    }
   
    public interface NumpadListener {
    	public void onNumberClicked(String number);
    	public void onDeleteClicked();
    	public void onEnterClicked();
    	public void onSignClicked(boolean sign);
    }
    
    public void onNumberClicked(String number) {
    	Log.d(TAG, "onNumberClicked: " + number);
    	mCallbacks.onNumberClicked(number);
    }
    
    public void onDeleteClicked() {
    	Log.d(TAG, "onDeleteClicked");
    	mCallbacks.onDeleteClicked();
    }
    
    public void onEnterClicked() {
    	Log.d(TAG, "onEnterClicked");
    	mCallbacks.onEnterClicked();
    }
    
    public void onSignClicked() {
    	Log.d(TAG, "onSignClicked: " + mNumpadIsPositive);
    	setOperationSign(!mNumpadIsPositive);
    	mCallbacks.onSignClicked(mNumpadIsPositive);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_numpad, container, false);
        
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
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (NumpadListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }
    
    private static String TAG = "NumpadFragment";
}
