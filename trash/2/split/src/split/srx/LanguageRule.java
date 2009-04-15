package split.srx;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reprezentuje regułe podziału dla danego języka. Zawiera liste reguł 
 * dzielących.
 *
 * @author loomchild
 */
public class LanguageRule {
	
	private List<Rule> ruleList;

	private String name;
	
	/**
	 * Tworzy pustą regułę językową.
	 */
	public LanguageRule(String name) {
		ruleList = new LinkedList<Rule>();
		this.name = name;
	}
	
	/**
	 * @return Zwraca niemodyfikowalną liste swoich reguł dzielących.
	 */
	public List<Rule> getRuleList() {
		return Collections.unmodifiableList(ruleList);
	}
	
	/**
	 * Dodaje regułę na koniec swojej listy reguł dzielących.
	 * @param rule Reguła.
	 */
	public void addRule(Rule rule) {
		ruleList.add(rule);
	}
	
	/**
	 * @return Zwraca swoją nazwę.
	 */
	public String getName() {
		return name;
	}
	
}
