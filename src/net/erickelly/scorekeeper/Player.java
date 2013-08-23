package net.erickelly.scorekeeper;

public class Player {
	private String name;
	private String extra;
	private int id;
	
	Player(int id, String name) {
		this.id = id;
		this.name = name;
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
	public int getId() {
		return this.id;
	}
}
