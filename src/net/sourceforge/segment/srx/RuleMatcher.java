package net.sourceforge.segment.srx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.segment.srx.legacy.PolengSrxTextIterator;
import net.sourceforge.segment.util.Util;


/**
 * Reprezentuje iterator po tekście wyszukujący kolejne wystąpienia danej reguły.
 * Używane przez {@link PolengSrxTextIterator}.
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
	 * Tworzy iterator.
	 * @param rule Reguła dla której będzie przeszukiwał tekst.
	 * @param text Tekst.
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
	 * Szuka następnego dopasowania do reguły w tekście po danej pozycji.
	 * @param start Pozycja w której należy zacząć poszukiwanie.
	 * @return Zwraca true jeśli udało się dopasować regułę.
	 */
	public boolean find(int start) {
		beforeMatcher.region(start, text.length());
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

}
