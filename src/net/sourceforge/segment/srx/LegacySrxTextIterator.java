package net.sourceforge.segment.srx;


import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.rootnode.loomchild.util.io.ReaderCharSequence;
import net.sourceforge.segment.AbstractTextIterator;

/**
 * Reprezentuje splitter dzielący na podstawie reguł zawartych w pliku srx.
 *
 * @author loomchild
 */
public class LegacySrxTextIterator extends AbstractTextIterator {

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
	 * @param document
	 *            Document containing language rules.
	 * @param languageCode
	 *            Language code to select the rules.
	 * @param text
	 *            Text.
	 */
	public LegacySrxTextIterator(SrxDocument document, String languageCode, 
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

	public LegacySrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader, int length, int bufferSize) {
		this(document, languageCode, new ReaderCharSequence(reader, length, bufferSize));
	}

	public LegacySrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader, int length) {
		this(document, languageCode, new ReaderCharSequence(reader, length));
	}

	public LegacySrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader) {
		this(document, languageCode, new ReaderCharSequence(reader));
	}

	/**
	 * Wyszukuje następne dopasowanie.
	 * @return Zwraca następny segment albo null jeśli nie istnieje.
	 * @throws IOSRuntimeException Zgłaszany gdy nastąpi błąd przy odczycie strumienia.
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
				if (minMatcher.getRule().isBreaking() && 
						endPosition > startPosition) {
					found = true;
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
	 * @return Zwraca true gdy są dostępne kolejne segmenty.
	 */
	public boolean hasNext() {
		return (startPosition < text.length());
	}
	
	private void initMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			boolean found = moveMatcher(matcher);
			if (!found) {
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
			boolean found = true;
			while (found && matcher.getBreakPosition() <= endPosition) {
				found = moveMatcher(matcher);
				if (!found) {
					i.remove();
				}
			}
		}
	}
	
	private boolean moveMatcher(RuleMatcher matcher) {
		try {
			matcher.find();
			return !matcher.hitEnd();
		} catch (IndexOutOfBoundsException e) {
			return false;
		}		
	}
	
	/**
	 * @return Zwraca iterator pierwszego trafionego dopasowania.
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
