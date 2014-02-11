package net.erickelly.scorekeeper.data;

import android.database.Cursor;
import android.database.CursorWrapper;

public class CursorWithDelete extends CursorWrapper {
	private int mHiddenPosition;
	private int mVirtualPosition;

	public CursorWithDelete(Cursor cursor, int positionToDelete) {
		super(cursor);
		mHiddenPosition = positionToDelete;
		mVirtualPosition = -1;
	}

	@Override
	public int getPosition() {
		return mVirtualPosition;
	}

	@Override
	public int getCount() {
		return super.getCount() - 1;
	}

	@Override
	public boolean move(int offset) {
		return moveToPosition(getPosition() + offset);
	}

	@Override
	public boolean moveToFirst() {
		return moveToPosition(0);
	}

	@Override
	public boolean moveToLast() {
		return moveToPosition(getCount() - 1);
	}

	@Override
	public boolean moveToNext() {
		return moveToPosition(getPosition() + 1);
	}

	@Override
	public boolean moveToPrevious() {
		return moveToPosition(getPosition() - 1);
	}

	@Override
	public boolean moveToPosition(int position) {
		mVirtualPosition = position;
		int cursorPosition = position;
		if (cursorPosition >= mHiddenPosition) {
			cursorPosition++;
		}

		return super.moveToPosition(cursorPosition);
	}

	@Override
	public boolean isBeforeFirst() {
		return getPosition() == -1 || getCount() == 0;
	}

	@Override
	public boolean isFirst() {
		return getPosition() == 0 && getCount() != 0;
	}

	@Override
	public boolean isLast() {
		int count = getCount();
		return getPosition() == (count - 1) && count != 0;
	}

	@Override
	public boolean isAfterLast() {
		int count = getCount();
		return getPosition() == count || count == 0;
	}
}
