package net.sourceforge.segment.srx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Reprezentuje iterator po tekście wyszukujący kolejne wystąpienia danej reguły.
 * Używane przez {@link LegacySrxTextIterator}.
 *
 * @author loomchild
 */
public class RuleMatcher implements Cloneable {

	private Rule rule;
	
	private CharSequence text;
	
	private Matcher beforeMatcher;
	
	private Matcher afterMatcher;
	
	boolean found;

	
	/**
	 * Tworzy iterator.
	 * @param rule Reguła dla której będzie przeszukiwał tekst.
	 * @param text Tekst.
	 */
	public RuleMatcher(Rule rule, CharSequence text) {
		this.rule = rule;
		this.text = text;
		Pattern beforePattern = Pattern.compile(rule.getBeforePattern());
		Pattern afterPattern = Pattern.compile(rule.getAfterPattern());
		this.beforeMatcher = beforePattern.matcher(text);
		this.afterMatcher = afterPattern.matcher(text);	
		this.found = true;
	}
	
	/**
	 * Szuka następnego dopasowania do reguły w tekście po ostatnio znalezionym
	 * dopasowaniu.
	 * @return Zwraca true jeśli udało się dopasować regułę.
	 */
	public boolean find() {
		found = false;
		while ((!found) && beforeMatcher.find()) {
			afterMatcher.region(beforeMatcher.end(), afterMatcher.regionEnd());
			found = afterMatcher.lookingAt();
		}
		return found;
	}

	/**
	 * Szuka dopasowania do reguły od danej pozycji w tekście.
	 * @param start Pozycja.
	 * @return Zwraca true jeśli udało się dopasować regułę.
	 */
	public boolean find(int start) {
		beforeMatcher.region(start, beforeMatcher.regionEnd());
		return find();
	}
	
	/**
	 * @return Zwraca true jeśli napotkano koniec tekstu.
	 */
	public boolean hitEnd() {
		return !found;
	}
	
	/**
	 * @return Zwraca pozycje początku dopasowania.
	 */
	public int getStartPosition() {
		return beforeMatcher.start();
	}

	/**
 * @return Zwraca pozycje po znaku podziału.
	 */
	public int getBreakPosition() {
		return afterMatcher.start();
	}

	/**
	 * @return Zwraca pozycje końca dopasowania.
	 */
	public int getEndPosition() {
		return afterMatcher.end();
	}
	
	/**
	 * @return Zwraca dopasowywaną przez ten iterator regułę.
	 */
	public Rule getRule() {
		return rule;
	}

	public RuleMatcher clone() {
		RuleMatcher ruleMatcher = new RuleMatcher(rule, text);
		ruleMatcher.found = found;
		cloneMatcher(ruleMatcher.beforeMatcher, beforeMatcher);
		cloneMatcher(ruleMatcher.afterMatcher, afterMatcher);
		return ruleMatcher;
	}
	
	private void cloneMatcher(Matcher matcher, Matcher referenceMatcher) {
		matcher.region(referenceMatcher.regionStart(), 
				referenceMatcher.regionEnd());
	}
}
