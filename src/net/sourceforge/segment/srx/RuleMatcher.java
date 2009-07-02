package net.sourceforge.segment.srx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.segment.util.Util;


/**
 * Represents matcher finding subsequent occurrences of one rule.
 *
 * @author loomchild
 */
public class RuleMatcher {
	
	@SuppressWarnings("unused")
	private SrxDocument document;

	private Rule rule;
	
	private CharSequence text;
	
	private Matcher beforeMatcher;
	
	private Matcher afterMatcher;
	
	boolean found;

	
	/**
	 * Creates matcher.
	 * @param rule rule which will be searched in the text
	 * @param text
	 */
	public RuleMatcher(SrxDocument document, Rule rule, CharSequence text) {
		this.document = document;
		this.rule = rule;
		this.text = text;
		Pattern beforePattern = Util.compile(document, rule.getBeforePattern());
		Pattern afterPattern = Util.compile(document, rule.getAfterPattern());
		this.beforeMatcher = beforePattern.matcher(text);
		this.afterMatcher = afterPattern.matcher(text);	
		this.found = true;
	}
	
	/**
	 * Finds next rule match after previously found.
	 * @return true if rule has been matched
	 */
	public boolean find() {
		found = false;
		while ((!found) && beforeMatcher.find()) {
			afterMatcher.region(beforeMatcher.end(), text.length());
			found = afterMatcher.lookingAt();
		}
		return found;
	}

	/**
	 * Finds next rule match after given start position. 
	 * @param start start position
	 * @return true if rule has been matched
	 */
	public boolean find(int start) {
		beforeMatcher.region(start, text.length());
		return find();
	}
	
	/**
	 * @return true if end of text has been reached while searching
	 */
	public boolean hitEnd() {
		return !found;
	}
	
	/**
	 * @return position in text where the last matching starts
	 */
	public int getStartPosition() {
		return beforeMatcher.start();
	}

	/**
	 * @return position in text where text should be splitted according to last matching
	 */
	public int getBreakPosition() {
		return afterMatcher.start();
	}

	/**
	 * @return position in text where the last matching ends
	 */
	public int getEndPosition() {
		return afterMatcher.end();
	}

	/**
	 * @return matcher rule
	 */
	public Rule getRule() {
		return rule;
	}

}
