package net.erickelly.scorekeeper.data;

import static net.erickelly.scorekeeper.data.Players._ID;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlayerAdapter extends SimpleCursorAdapter {
	private final Set<Long> mDeletedPlayers;
	private final int mLayout;
	private final int mDeletedLayout;
	private final int[] mTo;
	private final String[] mFrom;

	public PlayerAdapter(Context context, int layout, int deletedLayout,
			Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		mContext = context;
		mDeletedPlayers = new HashSet<Long>();
		mLayout = layout;
		mDeletedLayout = deletedLayout;
		mFrom = from;
		mTo = to;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view.getTag() == null || !view.getTag().equals("deleted")) {
			for (int i = 0; i < mTo.length; i++) {
				TextView textView = (TextView) view.findViewById(mTo[i]);
				textView.setText(cursor.getString(cursor
						.getColumnIndex(mFrom[i])));
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		Cursor cursor = (Cursor) getItem(position);
		Long id = cursor.getLong(cursor.getColumnIndex(_ID));
		String tag = null;

		// Get the right type of view
		if (convertView != null) {
			tag = (String) convertView.getTag();
		}
		if (mDeletedPlayers.contains(id)) {
			if (convertView == null || tag == null || !tag.equals("deleted")) {
				convertView = inflater.inflate(mDeletedLayout, parent, false);
				convertView.setTag("deleted");
			}
		} else {
			if (convertView == null || (tag != null && tag.equals("deleted"))) {
				convertView = inflater.inflate(mLayout, parent, false);
			}
		}

		// Fill the view with data
		bindView(convertView, mContext, cursor);

		return convertView;
	}

	public void markPlayerForDeletion(long playerId) {
		mDeletedPlayers.add(playerId);
		notifyDataSetChanged();
	}

	public void undoDeletedPlayer(long playerId) {
		mDeletedPlayers.remove(playerId);
		notifyDataSetChanged();
	}

	public void clearDeletedPlayers() {
		if (mDeletedPlayers.size() > 0) {
			for (Long playerId : mDeletedPlayers) {
				PlayerManager.getInstance().deletePlayer(mContext, playerId);
			}
			mDeletedPlayers.clear();
			notifyDataSetChanged();
		}
	}

	public boolean isPlayerScheduledForDeletion(long playerId) {
		return mDeletedPlayers.contains(playerId);
	}
}
