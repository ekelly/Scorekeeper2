package net.erickelly.scorekeeper.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import static net.erickelly.scorekeeper.data.Players.*;

public class PlayerManager {
	
	/**
	 * Add a player
	 * @param name Name of the new player
	 * @return Id of the newly created player
	 */
	public long addPlayer(String name) {
		Log.d(TAG, "addPlayer");
		long result = -1;
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	cv.put(NAME, name);
    	cv.put(SCORE, 0);
    	try {
    		result = db.insertOrThrow(TABLE_NAME, null, cv);
    	} catch(Exception e) {
    		Log.d("Couldn't insert", e.toString());
    	}
    	db.close();
    	return result;
	}
	
	public long addPlayer(Context c, String name) {
		Log.d(TAG, "addPlayer: " + name);
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(SCORE, 0);
		Uri uri = c.getContentResolver().insert(CONTENT_URI, values);
		return Long.parseLong(uri.getLastPathSegment());
	}
	
	/**
	 * Delete the player by id
	 * @param i
	 */
	public void deletePlayer(long id) {
		Log.d(TAG, "deletePlayer: " + id);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
    	String [] args = new String [] { String.valueOf(id) };
    	db.delete(TABLE_NAME, _ID+"=?", args);
    	db.close();
	}
	
	/**
	 * Adjust the player's score
	 * @param playerId The id of the player to update the score
	 * @param adjustAmt The amount with which to update the score
	 */
	public void adjustScore(Context c, int playerId, int adjustAmt) {
		Log.d(TAG, "adjustScore: " + playerId + ", " + adjustAmt);
		Player p = getPlayer(c, playerId);
		ContentValues values = new ContentValues();
		values.put(SCORE, p.getScore() + adjustAmt);
		c.getContentResolver().update(CONTENT_URI, values, "? = ?", new String[] { _ID, String.valueOf(playerId)});
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "UPADATE ? SET ?=??";
		String[] selectionArgs = { TABLE_NAME, SCORE, SCORE, String.valueOf(adjustAmt) };
		db.rawQuery(sql, selectionArgs);
    	db.close();
	}
	
	/**
	 * Return the player associated with the given ID
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(Context c, int playerId) {
		Log.d(TAG, "getPlayer: " + playerId);
		Cursor cursor = c.getContentResolver().query(CONTENT_URI, 
				new String[] { _ID, NAME, SCORE, NOTES }, null, null, null);
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex(NAME));
		int score = cursor.getInt(cursor.getColumnIndex(SCORE));
		cursor.close();
		return new Player(playerId, name, score);
	}
	
	/**
	 * Return the player associated with the given ID
	 * @param playerId
	 * @return
	 */
	public Player getPlayerByIndex(Context c, int idx) {
		Log.d(TAG, "getPlayerByIndex: " + idx);
		Cursor cursor = c.getContentResolver().query(CONTENT_URI, 
				new String[] { _ID, NAME, SCORE, NOTES }, null, null, null);
		Player p = null;
		cursor.moveToFirst();
		if (cursor.move(idx)) {
			p = new Player(cursor.getInt(cursor.getColumnIndex(_ID)), 
					cursor.getString(cursor.getColumnIndex(NAME)), 
					cursor.getInt(cursor.getColumnIndex(SCORE)));
		}
		cursor.close();
		return p;
	}
	
	/**
	 * Return a cursor iterating over the set of all players
	 * (include name and score)
	 * @return Cursor
	 */
	public Cursor getAllPlayers(Context c) {
		Log.d(TAG, "getAllPlayers");
		return c.getContentResolver().query(CONTENT_URI, 
				new String[] { _ID, NAME, SCORE, NOTES }, null, null, null);
	}
	
	/**
	 * Get the player count
	 * @return The total number of players
	 */
	public int getPlayerCount(Context c) {
		Log.d(TAG, "getPlayerCount");
		Cursor cursor = c.getContentResolver().query(CONTENT_URI, 
				new String[] { _ID, NAME, SCORE, NOTES }, null, null, null);
		int size = cursor.getCount();
		cursor.close();
		return size;
	}
	
	public static PlayerManager getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new PlayerManager(c);
		}
		if (db != null) {
			db.close();
		}
		return mInstance;
	}
	private PlayerManager(Context c) {
		dbHelper = DatabaseHelper.getInstance(c);
	}
	private static PlayerManager mInstance;
	private DatabaseHelper dbHelper;
	private static SQLiteDatabase db;
	
	private static final String TAG = "PlayerManager";
}
