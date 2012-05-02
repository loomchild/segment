package net.sourceforge.segment.srx.legacy;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.segment.AbstractTextIterator;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.RuleMatcher;
import net.sourceforge.segment.srx.SrxDocument;

/**
 * Reprezentuje splitter dzielący na podstawie reguł zawartych w pliku srx.
 *
 * @author loomchild
 */
public class AccurateSrxTextIterator extends AbstractTextIterator {

	private List<LanguageRule> languageRuleList;
	
	private CharSequence text;
	
	private String segment;

	private List<RuleMatcher> ruleMatcherList;
	
	private int startPosition, endPosition;
	
	/**
	 * Creates text iterator that obtains language rules form given document
	 * using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}.
	 * 
	 * @param document document containing language rules
	 * @param languageCode language code to select the rules
	 * @param text
	 */
	public AccurateSrxTextIterator(SrxDocument document, String languageCode, 
			CharSequence text) {
		this.languageRuleList = document.getLanguageRuleList(languageCode);
		this.text = text;
		this.segment = null;
		this.startPosition = 0;
		this.endPosition = 0;

		this.ruleMatcherList = new LinkedList<RuleMatcher>();
		for (LanguageRule languageRule : languageRuleList) {
			for (Rule rule : languageRule.getRuleList()) {
				RuleMatcher matcher = new RuleMatcher(document, rule, text);
				ruleMatcherList.add(matcher);
			}
		}

	}

	/**
	 * Wyszukuje następne dopasowanie.
	 * @return Zwraca następny segment albo null jeśli nie istnieje
	 * @throws IOSRuntimeException Zgłaszany gdy nastąpi błąd przy odczycie strumienia
	 */
	public String next() {
		if (hasNext()) {
			// Initialize matchers before first search.
			if (segment == null) {
				initMatchers();
			}
			boolean found = false;
			while ((ruleMatcherList.size() > 0) && !found) {
				RuleMatcher minMatcher = getMinMatcher();
				endPosition = minMatcher.getBreakPosition();
				if (minMatcher.getRule().isBreak() && 
						endPosition > startPosition) {
					found = true;
					cutMatchers();
				}
				moveMatchers();
			}
			if (!found) {
				endPosition = text.length();
			}
			segment = text.subSequence(startPosition, endPosition).toString();
			startPosition = endPosition;
			return segment;
		} else {
			return null;
		}
	}

	/**
	 * @return Zwraca true gdy są dostępne kolejne segmenty
	 */
	public boolean hasNext() {
		return (startPosition < text.length());
	}
	
	private void initMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			matcher.find();
			if (matcher.hitEnd()) {
				i.remove();
			}
		}
	}

	/**
	 * Przesuwa iteratory na kolejną pozycje jeśli to konieczne.
	 */
	private void moveMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			while (matcher.getBreakPosition() <= endPosition) {
				matcher.find();
				if (matcher.hitEnd()) {
					i.remove();
					break;
				}
			}
		}
	}

	/**
	 * Move matchers that start before previous segment end.
	 */
	private void cutMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			if (matcher.getStartPosition() < endPosition) {
				matcher.find(endPosition);
				if (matcher.hitEnd()) {
					i.remove();
				}
			}
		}
	}

	/**
	 * @return Zwraca iterator pierwszego trafionego dopasowania
	 */
	private RuleMatcher getMinMatcher() {
		int minPosition = Integer.MAX_VALUE;
		RuleMatcher minMatcher = null;
		for (RuleMatcher matcher : ruleMatcherList) {
			if (matcher.getBreakPosition() < minPosition) {
				minPosition = matcher.getBreakPosition();
				minMatcher = matcher;
			}
		}
		return minMatcher;
	}
	
}
