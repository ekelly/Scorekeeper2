package net.erickelly.scorekeeper.data;

public class Player {
	private String name;
	private int score;
	private String extra;
	private long id;
	
	public Player(long id, String name, int score) {
		this.id = id;
		this.name = name;
		this.score = score;
	}
	
	/**
	 * Adjust the score by the given amount
	 * @param adjustAmt
	 */
	public void adjustScore(int adjustAmt) {
		this.score = this.score + adjustAmt;
	}
	
	/**
	 * Getter for score
	 */
	public int getScore() {
		return this.score;
	}
	
	/**
	 * Setter for name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Setter for extra
	 * @param extra
	 */
	public void setExtra(String extra) {
		this.extra = extra;
	}
	
	/**
	 * Getter for extra
	 * @return
	 */
	public String getExtra() {
		return this.extra;
	}
	
	/**
	 * Getter for name
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Getter for id
	 * @return
	 */
	public long getId() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return "Player " + this.id + ": " + this.name + ", " + this.score;
	}
}
