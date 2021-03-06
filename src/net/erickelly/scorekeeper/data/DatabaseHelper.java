package net.erickelly.scorekeeper.data;

import static android.provider.BaseColumns._ID;
import static net.erickelly.scorekeeper.data.Players.ADJUST_AMT;
import static net.erickelly.scorekeeper.data.Players.NAME;
import static net.erickelly.scorekeeper.data.Players.NOTES;
import static net.erickelly.scorekeeper.data.Players.PLAYERS_TABLE_NAME;
import static net.erickelly.scorekeeper.data.Players.PLAYER_ID;
import static net.erickelly.scorekeeper.data.Players.SCORE;
import static net.erickelly.scorekeeper.data.Players.SCORES_TABLE_NAME;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "players.db";
	private static final int DATABASE_VERSION = 5;

	public static DatabaseHelper getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new DatabaseHelper(c);
		}
		return mInstance;
	}

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + PLAYERS_TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
				+ " TEXT NOT NULL);");
		db.execSQL("CREATE TABLE " + SCORES_TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + PLAYER_ID
				+ " INTEGER NOT NULL, " + ADJUST_AMT + " INTEGER, " + SCORE
				+ " INTEGER, " + NOTES + " TEXT);");
		ContentValues cv = new ContentValues();
		cv.put(NAME, "Player 1");
		db.insert(PLAYERS_TABLE_NAME, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + PLAYERS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SCORES_TABLE_NAME);
		onCreate(db);
	}

	private static DatabaseHelper mInstance;
}
