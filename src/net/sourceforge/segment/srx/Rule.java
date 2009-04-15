package net.sourceforge.segment.srx;

/**
 * Represents breaking or non breaking rule. Contains after break and before
 * break patterns,
 * 
 * @author loomchild
 */
public class Rule {

	private boolean breaking;

	private String beforePattern;

	private String afterPattern;

	/**
	 * Creates rule.
	 * 
	 * @param breaking
	 *            Type of rule; true - breaking, false - non breaking.
	 * @param beforePattern
	 *            Pattern matching text before break.
	 * @param afterPattern
	 *            Pattern matching text after break.
	 */
	public Rule(boolean breaking, String beforePattern, String afterPattern) {
		this.breaking = breaking;
		this.beforePattern = beforePattern;
		this.afterPattern = afterPattern;
	}

	/**
	 * @return Returns type of rule; true - breaking, false - non breaking.
	 */
	public boolean isBreaking() {
		return breaking;
	}

	/**
	 * @return Returns pattern matching text before break.
	 */
	public String getBeforePattern() {
		return beforePattern;
	}

	/**
	 * @return Returns pattern matching text after break.
	 */
	public String getAfterPattern() {
		return afterPattern;
	}

}
