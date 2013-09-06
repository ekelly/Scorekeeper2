package net.erickelly.scorekeeper.data;

import java.util.List;

import android.util.Pair;

/**
 * A data access object for dealing with players
 * 
 * @author eric
 * 
 */
public class Player {
	private String name;
	private int score;
	private String extra;
	private long id;
	private List<Pair<Integer, String>> history;

	public Player(long id, String name, int score) {
		this.id = id;
		this.name = name;
		this.score = score;
	}

	public Player(long id, String name, List<Pair<Integer, String>> history) {
		this.id = id;
		this.name = name;
		this.history = history;
		this.score = calculateScore(history);
	}

	/**
	 * Adjust the score by the given amount
	 * 
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
	 * Getter for score history
	 */
	public List<Pair<Integer, String>> getHistory() {
		return this.history;
	}

	/**
	 * Setter for name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter for extra
	 * 
	 * @param extra
	 */
	public void setExtra(String extra) {
		this.extra = extra;
	}

	/**
	 * Getter for extra
	 * 
	 * @return
	 */
	public String getExtra() {
		return this.extra;
	}

	/**
	 * Getter for name
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Getter for id
	 * 
	 * @return
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Given a history (list of previous adjust amounts), calculate the current
	 * score
	 * 
	 * @param scores
	 * @return
	 */
	private static int calculateScore(List<Pair<Integer, String>> scores) {
		int score = 0;
		for (Pair<Integer, String> p : scores) {
			score += p.first;
		}
		return score;
	}

	@Override
	public String toString() {
		return "Player " + this.id + ": " + this.name + ", " + this.score;
	}
}
