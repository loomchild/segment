package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents rule for segmenting text in some language. Contains {@link Rule}
 * list.
 * 
 * @author loomchild
 */
public class LanguageRule {

	private List<Rule> ruleList;

	private String name;

	/**
	 * Creates language rule.
	 * 
	 * @param name language rule name
	 * @param ruleList rule list (it will be shallow copied)
	 */
	public LanguageRule(String name, List<Rule> ruleList) {
		this.ruleList = new ArrayList<Rule>(ruleList);
		this.name = name;
	}

	/**
	 * Creates empty language rule.
	 * 
	 * @param name language rule name
	 */
	public LanguageRule(String name) {
		this(name, new ArrayList<Rule>());
	}

	/**
	 * @return unmodifiable rules list
	 */
	public List<Rule> getRuleList() {
		return Collections.unmodifiableList(ruleList);
	}

	/**
	 * Adds rule to the end of rule list.
	 * @param rule
	 */
	public void addRule(Rule rule) {
		ruleList.add(rule);
	}

	/**
	 * @return language rule name
	 */
	public String getName() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		LanguageRule other = (LanguageRule)object;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
