package net.erickelly.scorekeeper.data;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import static net.erickelly.scorekeeper.data.Players.*;

/**
 * This class encapsulates all player management
 * 
 * @author eric
 * 
 */
public class PlayerManager {

	/**
	 * Add a player
	 * 
	 * @param c
	 *            Context
	 * @param name
	 *            Name of the new player
	 * @return Id of the newly created player
	 */
	public long addPlayer(Context c, String name) {
		Log.d(TAG, "addPlayer: " + name);
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		Uri uri = c.getContentResolver().insert(PLAYERS_URI, values);
		if (mPlayerCount == null) {
			mPlayerCount = getPlayerCount(c);
		} else {
			mPlayerCount++;
		}
		return Long.parseLong(uri.getLastPathSegment());
	}

	/**
	 * Delete the player by id
	 * 
	 * @param i
	 */
	public void deletePlayer(Context c, long id) {
		Log.d(TAG, "deletePlayer: " + id);
		resetPlayerScore(c, id);
		c.getContentResolver().delete(
				Uri.withAppendedPath(PLAYERS_URI, "/" + id), null, null);
		if (mPlayerCount == null) {
			mPlayerCount = getPlayerCount(c);
		} else {
			mPlayerCount--;
		}
	}

	/**
	 * Reset the given player's score to 0
	 * 
	 * @param c
	 * @param playerId
	 *            ID of the player to reset
	 */
	public void resetPlayerScore(Context c, long playerId) {
		Log.d(TAG, "resetPlayerScore: " + playerId);
		c.getContentResolver().delete(
				Uri.withAppendedPath(SCORES_URI, "/" + playerId), null, null);
	}
	
	/**
	 * Reset all players scores to 0
	 * @param c
	 */
	public void resetAllPlayers(Context c) {
		Cursor cursor = getAllPlayers(c);
		final int idColumn = cursor.getColumnIndex(_ID);
		while (cursor.moveToNext()) {
			resetPlayerScore(c, cursor.getLong(idColumn));
		}
		cursor.close();
	}

	/**
	 * Adjust the player's score
	 * 
	 * @param playerId
	 *            The id of the player to update the score
	 * @param adjustAmt
	 *            The amount with which to update the score
	 * @param extra
	 *            Extra information associated with this round
	 */
	public void adjustScore(Context c, long playerId, int adjustAmt,
			String extra) {
		Log.d(TAG, "adjustScore: " + playerId + ", " + adjustAmt + ", " + extra);
		ContentValues values = new ContentValues();
		values.put(PLAYER_ID, playerId);
		values.put(ADJUST_AMT, adjustAmt);
		if (extra != null) {
			values.put(NOTES, extra);
		}
		c.getContentResolver()
				.insert(Uri.withAppendedPath(SCORES_URI,
						"/" + String.valueOf(playerId)), values);
	}

	/**
	 * Adjust the player's score
	 * 
	 * @param playerId
	 *            The id of the player to update the score
	 */
	public int getPlayerScore(Context c, long playerId) {
		Log.d(TAG, "getPlayerScore: " + playerId);
		Cursor cursor = c.getContentResolver().query(
				Uri.withAppendedPath(SCORES_URI, "/" + playerId),
				new String[] { PLAYER_ID, ADJUST_AMT }, null, null, null);
		int score = 0;
		int adjustAmtColumn = cursor.getColumnIndex(ADJUST_AMT);
		while (cursor.moveToNext()) {
			score += cursor.getInt(adjustAmtColumn);
		}
		return score;
	}

	/**
	 * Edit the player's name
	 * 
	 * @param playerId
	 *            The id of the player to update the name
	 * @param playerName
	 *            The new name of the player
	 */
	public void editPlayerName(Context c, long playerId, String playerName) {
		Log.d(TAG, "editPlayerName: " + playerId + ", " + playerName);
		ContentValues values = new ContentValues();
		values.put(NAME, playerName);
		c.getContentResolver().update(
				Uri.withAppendedPath(PLAYERS_URI,
						"/" + String.valueOf(playerId)), values, null, null);
	}

	/**
	 * Return the player associated with the given ID
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(Context c, long playerId) {
		Log.d(TAG, "getPlayer: " + playerId);
		Cursor cursor = c.getContentResolver().query(
				Uri.withAppendedPath(PLAYERS_URI, "/" + playerId),
				new String[] { _ID, NAME }, null, null, null);
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex(NAME));
		cursor.close();
		List<Pair<Integer, String>> history = getPlayerHistory(c, playerId);
		return new Player(playerId, name, history);
	}

	/**
	 * Return the score history for the given player
	 * 
	 * @param c
	 * @param id
	 *            ID of the player to query
	 * @return
	 */
	private List<Pair<Integer, String>> getPlayerHistory(Context c, long id) {
		Cursor cursor = c.getContentResolver().query(
				Uri.withAppendedPath(SCORES_URI, "/" + id),
				new String[] { _ID, ADJUST_AMT, NOTES }, null, null, null);
		List<Pair<Integer, String>> history = new LinkedList<Pair<Integer, String>>();
		while (cursor.moveToNext()) {
			history.add(Pair.create(
					cursor.getInt(cursor.getColumnIndex(ADJUST_AMT)),
					cursor.getString(cursor.getColumnIndex(NOTES))));
		}
		cursor.close();
		return history;
	}

	/**
	 * Return the player associated with the given ID
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayerByIndex(Context c, int idx) {
		Log.d(TAG, "getPlayerByIndex: " + idx);
		Cursor cursor = c.getContentResolver().query(PLAYERS_URI,
				new String[] { _ID, NAME }, null, null, null);
		Player p = null;
		cursor.moveToFirst();
		if (cursor.move(idx)) {
			long id = cursor.getInt(cursor.getColumnIndex(_ID));
			String name = cursor.getString(cursor.getColumnIndex(NAME));
			List<Pair<Integer, String>> history = getPlayerHistory(c, id);
			p = new Player(id, name, history);
		}
		cursor.close();
		return p;
	}

	/**
	 * Return a cursor iterating over the set of all players (include name and
	 * score)
	 * 
	 * @return Cursor
	 */
	public Cursor getAllPlayers(Context c) {
		Log.d(TAG, "getAllPlayers");
		return c.getContentResolver().query(PLAYERS_URI,
				new String[] { _ID, NAME }, null, null, null);
	}

	/**
	 * Get the player count
	 * 
	 * @return The total number of players
	 */
	public int getPlayerCount(Context c) {
		Log.d(TAG, "getPlayerCount");
		if (mPlayerCount == null) {
			Cursor cursor = c.getContentResolver().query(PLAYERS_URI,
					new String[] { _ID, NAME }, null, null, null);
			mPlayerCount = cursor.getCount();
			cursor.close();
		}
		return mPlayerCount;
	}

	/**
	 * Retrieve a PlayerManager instance
	 * 
	 * @return
	 */
	public static PlayerManager getInstance() {
		if (mInstance == null) {
			mInstance = new PlayerManager();
		}
		return mInstance;
	}

	private PlayerManager() {
	}

	private Integer mPlayerCount;
	private static PlayerManager mInstance;

	private static final String TAG = "PlayerManager";
}
