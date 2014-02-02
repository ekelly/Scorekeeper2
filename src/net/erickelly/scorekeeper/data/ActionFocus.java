package net.erickelly.scorekeeper.data;

public enum ActionFocus {
	NOTES, SCORE;
	
	public boolean toBoolean() {
		return this.equals(SCORE);
	}
	
	public static ActionFocus fromBoolean(boolean bool) {
		return bool ? SCORE : NOTES;
	}
}
