package net.erickelly.scorekeeper.data;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class Players extends ContentProvider {

	private DatabaseHelper mDatabase;

	public static final String AUTHORITY = "net.erickelly.scorekeeper.provider";
	public static final String PLAYERS_TABLE_NAME = "players";
	public static final String SCORES_TABLE_NAME = "scores";
	public static final String SUMMARY = "summary";

	// Columns in the Events database
	public static final String _ID = BaseColumns._ID;
	public static final String PLAYER_ID = "player_id";
	public static final String NAME = "name";
	public static final String SCORE = "score";
	public static final String ADJUST_AMT = "adjust_amt";
	public static final String SCORE_SUM = "IFNULL(SUM(" + ADJUST_AMT + "), 0) AS " + SCORE;
	public static final String NOTES = "notes";
	
	private static final String COMBINED_PLAYERS_AND_SCORES = PLAYERS_TABLE_NAME 
			+ " LEFT OUTER JOIN " + "(SELECT MAX(" + _ID + "), " + PLAYER_ID 
			+ ", " + SCORE + " FROM " + SCORES_TABLE_NAME + " GROUP BY " + PLAYER_ID 
			+ ") AS " + SCORES_TABLE_NAME + " ON (" + PLAYERS_TABLE_NAME 
			+ "." + _ID + "=" + SCORES_TABLE_NAME + "." + PLAYER_ID + ")";

	public static final Uri BASE_URI = Uri
			.parse("content://" + AUTHORITY + "/");
	public static final Uri PLAYERS_URI = Uri.withAppendedPath(BASE_URI,
			PLAYERS_TABLE_NAME);
	public static final Uri SCORES_URI = Uri.withAppendedPath(BASE_URI,
			SCORES_TABLE_NAME);
	public static final Uri PLAYERS_WITH_SCORE_URI = Uri.withAppendedPath(
			BASE_URI, SUMMARY);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/players";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/player";

	// Creates a UriMatcher object.
	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int PLAYERS = 1;
	private static final int PLAYER_BY_ID = 2;
	private static final int SCORES_BY_ID = 3;
	private static final int PLAYERS_WITH_SCORE = 4;

	// The various patters that the UriMatcher should match
	static {
		sUriMatcher.addURI(AUTHORITY, PLAYERS_TABLE_NAME, PLAYERS);
		sUriMatcher.addURI(AUTHORITY, PLAYERS_TABLE_NAME + "/#", PLAYER_BY_ID);
		sUriMatcher.addURI(AUTHORITY, SCORES_TABLE_NAME + "/#", SCORES_BY_ID);
		sUriMatcher.addURI(AUTHORITY, SUMMARY, PLAYERS_WITH_SCORE);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String id;
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		int rowsDeleted = 0;
		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			rowsDeleted = db.delete(PLAYERS_TABLE_NAME, selection,
					selectionArgs);
			break;
		case PLAYER_BY_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(PLAYERS_TABLE_NAME, _ID + "=" + id,
						null);
			} else {
				rowsDeleted = db.delete(PLAYERS_TABLE_NAME, _ID + "=" + id
						+ " and " + selection, selectionArgs);
			}
			break;
		case SCORES_BY_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(SCORES_TABLE_NAME,
						PLAYER_ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(SCORES_TABLE_NAME, PLAYER_ID + "=" + id
						+ " and " + selection, selectionArgs);
			}
			getContext().getContentResolver().notifyChange(SCORES_URI, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(PLAYERS_WITH_SCORE_URI,
				null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		long id = 0;
		String table;
		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			id = db.insert(PLAYERS_TABLE_NAME, null, values);
			table = PLAYERS_TABLE_NAME;
			break;
		case SCORES_BY_ID:
			if (!values.containsKey(PLAYER_ID)) {
				long player_id = Long.valueOf(uri.getLastPathSegment());
				values.put(PLAYER_ID, player_id);
			}
			id = db.insert(SCORES_TABLE_NAME, null, values);
			table = SCORES_TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(PLAYERS_WITH_SCORE_URI,
				null);
		return Uri.withAppendedPath(BASE_URI, table + "/" + id);
	}

	@Override
	public boolean onCreate() {
		mDatabase = DatabaseHelper.getInstance(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String groupBy = null;

		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			// Set the table
			queryBuilder.setTables(PLAYERS_TABLE_NAME);
			break;
		case PLAYER_BY_ID:
			// Set the table
			queryBuilder.setTables(PLAYERS_TABLE_NAME);
			// Adding the ID to the original query
			queryBuilder.appendWhere(_ID + "=" + uri.getLastPathSegment());
			break;
		case SCORES_BY_ID:
			// Set the table
			queryBuilder.setTables(SCORES_TABLE_NAME);
			// Adding the ID to the original query
			queryBuilder
					.appendWhere(PLAYER_ID + "=" + uri.getLastPathSegment());
			break;
		case PLAYERS_WITH_SCORE:
			// Set the table
			queryBuilder.setTables(COMBINED_PLAYERS_AND_SCORES);
			groupBy = PLAYERS_TABLE_NAME + "." + _ID;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = mDatabase.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, groupBy, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String id;
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		int rowsUpdated = 0;
		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			rowsUpdated = db.update(PLAYERS_TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case PLAYER_BY_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(PLAYERS_TABLE_NAME, values, _ID + "="
						+ id, null);
			} else {
				rowsUpdated = db.update(PLAYERS_TABLE_NAME, values, _ID + "="
						+ id + " and " + selection, selectionArgs);
			}
			break;
		case SCORES_BY_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(SCORES_TABLE_NAME, values, PLAYER_ID
						+ "=" + id, null);
			} else {
				rowsUpdated = db.update(SCORES_TABLE_NAME, values, PLAYER_ID
						+ "=" + id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(PLAYERS_WITH_SCORE_URI,
				null);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@SuppressWarnings("unused")
	private void checkColumns(String[] projection) {
		if (false) {
			String[] available = { _ID, PLAYERS_TABLE_NAME + "." + _ID,
					SCORES_TABLE_NAME + "." + _ID, NAME, SCORE, SCORE_SUM,
					"IFNULL( " + SCORE + ", 0) AS " + SCORE, PLAYER_ID, ADJUST_AMT, NOTES };
			if (projection != null) {
				HashSet<String> requestedColumns = new HashSet<String>(
						Arrays.asList(projection));
				HashSet<String> availableColumns = new HashSet<String>(
						Arrays.asList(available));
				// Check if all columns which are requested are available
				if (!availableColumns.containsAll(requestedColumns)) {
					throw new IllegalArgumentException(
							"Unknown columns in projection: " + requestedColumns);
				}
			}
		}
	}
}
