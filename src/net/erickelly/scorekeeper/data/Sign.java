package net.erickelly.scorekeeper.data;

public enum Sign {
	POSITIVE(true), NEGATIVE(false);
	
	private boolean isPositive;
	
	public String toString() {
		if (isPositive) return "+";
		else return "-";
	}
	
	/**
	 * Return the sign that is the opposite of the current sign
	 * @return
	 */
	public Sign inverse() {
		if (isPositive) return NEGATIVE;
		else return POSITIVE;
	}

	/**
	 * Is this sign positive?
	 * @return true if positive, false if negative
	 */
	public boolean isPositive() {
		return isPositive;
	}
	
	/**
	 * Return the Sign associated with the given boolean value
	 * @param value
	 * @return
	 */
	public static Sign valueOf(boolean isPositive) {
		if (isPositive) return POSITIVE;
		else return NEGATIVE;
	}
	
	Sign(boolean isPositive) {
		this.isPositive = isPositive;
	}
}
