package net.erickelly.scorekeeper.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static android.provider.BaseColumns._ID;
import static net.erickelly.scorekeeper.data.Players.*;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "players.db";
	private static final int DATABASE_VERSION = 1;
	
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
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
				+ " TEXT NOT NULL," + SCORE + " INTEGER," + NOTES 
				+ " TEXT);");
		ContentValues cv = new ContentValues();
		cv.put(NAME, "Player 1");
		cv.put(SCORE, 0);
		db.insert(TABLE_NAME, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	private static DatabaseHelper mInstance;
}
