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
	
	private SrxDocument document;

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
	public RuleMatcher(SrxDocument document, Rule rule, CharSequence text) {
		this.document = document;
		this.rule = rule;
		this.text = text;
		Pattern beforePattern = compile(rule.getBeforePattern());
		Pattern afterPattern = compile(rule.getAfterPattern());
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
			afterMatcher.region(beforeMatcher.end(), text.length());
			found = afterMatcher.lookingAt();
		}
		return found;
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
		return beforeMatcher.end();
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

	private Pattern compile(String regex) {
		Pattern pattern = (Pattern)document.getCache().get(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			document.getCache().put(regex, pattern);
		}
		return pattern;
	}
}
