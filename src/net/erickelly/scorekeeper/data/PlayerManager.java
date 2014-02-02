package net.erickelly.scorekeeper.data;

import static net.erickelly.scorekeeper.data.Players.ADJUST_AMT;
import static net.erickelly.scorekeeper.data.Players.NAME;
import static net.erickelly.scorekeeper.data.Players.NOTES;
import static net.erickelly.scorekeeper.data.Players.PLAYERS_URI;
import static net.erickelly.scorekeeper.data.Players.PLAYER_ID;
import static net.erickelly.scorekeeper.data.Players.SCORE;
import static net.erickelly.scorekeeper.data.Players.SCORES_TABLE_NAME;
import static net.erickelly.scorekeeper.data.Players.SCORES_URI;
import static net.erickelly.scorekeeper.data.Players._ID;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

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
		c.getContentResolver()
				.delete(Uri.withAppendedPath(PLAYERS_URI, appendedPath(id)),
						null, null);
		if (mPlayerCount == null) {
			mPlayerCount = getPlayerCount(c);
		} else {
			mPlayerCount--;
		}
	}

	/**
	 * Get the player count
	 * 
	 * @return The total number of players
	 */
	public int getPlayerCount(Context c) {
		// Log.d(TAG, "getPlayerCount");
		if (mPlayerCount == null) {
			Cursor cursor = c.getContentResolver().query(PLAYERS_URI,
					new String[] { _ID, NAME }, null, null, null);
			mPlayerCount = cursor.getCount();
			cursor.close();
		}
		return mPlayerCount;
	}

	/**
	 * Reset the given player's score to 0
	 * 
	 * @param c
	 * @param playerId
	 *            ID of the player to reset
	 */
	public static void resetPlayerScore(Context c, long playerId) {
		Log.d(TAG, "resetPlayerScore: " + playerId);
		c.getContentResolver().delete(
				Uri.withAppendedPath(SCORES_URI, appendedPath(playerId)), null,
				null);
	}

	/**
	 * Reset all players scores to 0
	 * 
	 * @param c
	 */
	public static void resetAllPlayers(Context c) {
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
	 * @param update
	 *            Should this update an existing record or insert a new record?
	 */
	public static void adjustScore(Context c, long playerId, int adjustAmt,
			String extra, boolean update) {
		Log.d(TAG, "adjustScore: " + playerId + ", " + adjustAmt + ", " + extra
				+ ", " + update);
		int score = getPlayerScore(c, playerId);
		ContentValues values = new ContentValues();
		values.put(PLAYER_ID, playerId);
		values.put(ADJUST_AMT, adjustAmt);
		values.put(SCORE, score + adjustAmt);
		if (extra != null) {
			values.put(NOTES, extra);
		}
		if (update) {
			c.getContentResolver().update(
					Uri.withAppendedPath(SCORES_URI, appendedPath(playerId)),
					values, where(playerId), null);
		} else {
			c.getContentResolver().insert(
					Uri.withAppendedPath(SCORES_URI, appendedPath(playerId)),
					values);
		}
	}

	/**
	 * Update the note associated with the last score
	 * 
	 * @param playerId
	 *            The id of the player to update the score
	 * @param extra
	 *            Extra information associated with this round
	 * @param update
	 *            Should this update an existing record or insert a new record?
	 */
	public static void updateScoreNotes(Context c, long playerId, String extra,
			boolean update) {
		Log.d(TAG, "updateScoreNotes: " + playerId + ", " + extra
				+ ", update? " + update);
		int score = getPlayerScore(c, playerId);
		ContentValues values = new ContentValues();
		values.put(PLAYER_ID, playerId);
		values.put(SCORE, score);
		values.put(NOTES, extra);
		if (update) {
			c.getContentResolver().update(
					Uri.withAppendedPath(SCORES_URI, appendedPath(playerId)),
					values, where(playerId), null);
		} else {
			c.getContentResolver().insert(
					Uri.withAppendedPath(SCORES_URI, appendedPath(playerId)),
					values);
		}
	}

	/**
	 * Undo the last score commit for the player
	 * 
	 * @param c
	 * @param playerId
	 *            ID of the player to "undo" the last score
	 */
	public static void undoLastAdjustment(Context c, long playerId) {
		Log.d(TAG, "undoLastAdjustment: " + playerId);
		c.getContentResolver().delete(
				Uri.withAppendedPath(SCORES_URI, "/" + playerId),
				_ID + " = (SELECT MAX(" + _ID + ") FROM " + SCORES_TABLE_NAME
						+ " WHERE " + PLAYER_ID + " = " + playerId + ")", null);
	}

	/**
	 * Adjust the player's score
	 * 
	 * @param playerId
	 *            The id of the player to update the score
	 */
	public static int getPlayerScore(Context c, long playerId) {
		Log.d(TAG, "getPlayerScore: " + playerId);
		Cursor cursor = c.getContentResolver().query(
				Uri.withAppendedPath(SCORES_URI, appendedPath(playerId)),
				new String[] { PLAYER_ID, ADJUST_AMT }, null, null, null);
		int score = 0;
		int adjustAmtColumn = cursor.getColumnIndex(ADJUST_AMT);
		while (cursor.moveToNext()) {
			try {
				score += cursor.getInt(adjustAmtColumn);
			} catch (Exception e) {
			}
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
	public static void editPlayerName(Context c, long playerId,
			String playerName) {
		Log.d(TAG, "editPlayerName: " + playerId + ", " + playerName);
		ContentValues values = new ContentValues();
		values.put(NAME, playerName);
		c.getContentResolver().update(
				Uri.withAppendedPath(PLAYERS_URI, appendedPath(playerId)),
				values, null, null);
	}

	/**
	 * Return the player associated with the given ID
	 * 
	 * @param playerId
	 * @return
	 */
	public static Player getPlayer(Context c, long playerId) {
		Log.d(TAG, "getPlayer: " + playerId);
		Cursor cursor = c.getContentResolver().query(
				Uri.withAppendedPath(PLAYERS_URI, appendedPath(playerId)),
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
	public static List<Pair<Integer, String>> getPlayerHistory(Context c,
			long id) {
		Cursor cursor = c.getContentResolver().query(
				Uri.withAppendedPath(SCORES_URI, appendedPath(id)),
				new String[] { _ID, ADJUST_AMT, NOTES }, null, null, null);
		int adjustColumn = cursor.getColumnIndex(ADJUST_AMT);
		int notesColumn = cursor.getColumnIndex(NOTES);
		List<Pair<Integer, String>> history = new LinkedList<Pair<Integer, String>>();
		while (cursor.moveToNext()) {
			Integer adjustAmt = null;
			if (!cursor.isNull(adjustColumn)) {
				adjustAmt = cursor.getInt(adjustColumn);
			}
			history.add(Pair.create(adjustAmt, cursor.getString(notesColumn)));
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
	public static Player getPlayerByIndex(Context c, int idx) {
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
	public static Cursor getAllPlayers(Context c) {
		Log.d(TAG, "getAllPlayers");
		return c.getContentResolver().query(PLAYERS_URI,
				new String[] { _ID, NAME }, null, null, null);
	}

	/**
	 * Given a player id, return the string identifying that player to be
	 * appended to the URI
	 * 
	 * @param playerId
	 * @return
	 */
	private static String appendedPath(long playerId) {
		return "/" + String.valueOf(playerId);
	}

	/**
	 * Return the String where clause which reduces the scores table to the
	 * single last-added row (for the given player)
	 * 
	 * @param playerId
	 * @return
	 */
	private static String where(long playerId) {
		return _ID + " = (SELECT MAX(" + _ID + ") FROM " + SCORES_TABLE_NAME
				+ " WHERE " + PLAYER_ID + " = " + playerId + ")";
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
