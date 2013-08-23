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
	public static final String TABLE_NAME = "players";

	// Columns in the Events database
	public static final String _ID = BaseColumns._ID;
	public static final String NAME = "name";
	public static final String SCORE = "score";
	public static final String NOTES = "notes";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/players";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/player";

	// Creates a UriMatcher object.
	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int PLAYERS = 1;
	private static final int PLAYER_ID = 2;

	// The various patters that the UriMatcher should match
	static {
		sUriMatcher.addURI(AUTHORITY, TABLE_NAME, PLAYERS);
		sUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", PLAYER_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		int rowsDeleted = 0;
		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
			break;
		case PLAYER_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(TABLE_NAME, _ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(TABLE_NAME, _ID + "=" + id + " and "
						+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
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
		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			id = db.insert(TABLE_NAME, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(TABLE_NAME + "/" + id);
	}

	@Override
	public boolean onCreate() {
		mDatabase = DatabaseHelper.getInstance(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case 1:
			break;
		case 2:
			// Adding the ID to the original query
			queryBuilder.appendWhere(_ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = mDatabase.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		int rowsUpdated = 0;
		switch (sUriMatcher.match(uri)) {
		case PLAYERS:
			rowsUpdated = db.update(TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case PLAYER_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(TABLE_NAME, values, _ID + "=" + id,
						null);
			} else {
				rowsUpdated = db.update(TABLE_NAME, values, _ID + "=" + id
						+ " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { _ID, NAME, SCORE, NOTES };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
