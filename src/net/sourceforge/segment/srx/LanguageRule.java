package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents rule for segmenting text in some language. Contains breaking or
 * non breaking rule list.
 * 
 * @author loomchild
 */
public class LanguageRule {

	private List<Rule> ruleList;

	private String name;

	/**
	 * Creates language rule.
	 * 
	 * @param name
	 *            Language rule name.
	 * @param ruleList
	 *            Rule list (it will be shallow copied).
	 */
	public LanguageRule(String name, List<Rule> ruleList) {
		this.ruleList = new ArrayList<Rule>(ruleList);
		this.name = name;
	}

	/**
	 * Creates empty language rule.
	 * 
	 * @param name
	 *            Language rule name.
	 */
	public LanguageRule(String name) {
		this(name, new ArrayList<Rule>());
	}

	/**
	 * @return Returns unmodifiable rules list.
	 */
	public List<Rule> getRuleList() {
		return Collections.unmodifiableList(ruleList);
	}

	/**
	 * Adds rule to the end of rule list.
	 * 
	 * @param rule
	 *            Rule.
	 */
	public void addRule(Rule rule) {
		ruleList.add(rule);
	}

	/**
	 * @return Returns language rule name.
	 */
	public String getName() {
		return name;
	}

}
