package split.srx;


import java.io.Reader;
import java.rmi.UnexpectedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import loomchild.util.exceptions.EndOfStreamException;
import loomchild.util.io.ReaderCharSequence;

import split.splitter.Splitter;

/**
 * Reprezentuje splitter dzielący na podstawie reguł zawartych w pliku srx.
 *
 * @author loomchild
 */
public class SrxSplitter implements Splitter {

	private LanguageRule languageRule;
	
	private CharSequence text;
	
	private String segment;

	private List<RuleMatcher> ruleMatcherList;
	
	private int startPosition, endPosition;
	
	/**
	 * Tworzy splitter dla danej reguły językowej i tekstu.
	 * @param languageRule Reguła językowa.
	 * @param text Tekst.
	 */
	public SrxSplitter(LanguageRule languageRule, CharSequence text) {
		this.languageRule = languageRule;
		this.text = text;
		initialize();
	}

	public SrxSplitter(LanguageRule languageRule, Reader reader) {
		this(languageRule, new ReaderCharSequence(reader));
	}

	/**
	 * Wyszukuje następne dopasowanie.
	 * @return Zwraca następny segment albo null jeśli nie istnieje.
	 * @throws IOSRuntimeException Zgłaszany gdy nastąpi błąd przy odczycie strumienia.
	 */
	public String next() {
		if (hasNext()) {
			boolean found = false;
			while ((ruleMatcherList.size() > 0) && !found) {
				RuleMatcher minMatcher = getMinMatcher();
				found = minMatcher.getRule().isBreaking();
				endPosition = minMatcher.getBreakPosition();
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

	/**
	 * @return Zwraca true gdy strumień nie zablokuje przy kolejnym segmencie 
	 */
	public boolean isReady() {
		return hasNext();
	}

	/**
	 * Inicjalizuje splitter. Tworzy liste iteratorów.
	 * @param text Tekst.
	 */
	private void initialize() {
		this.ruleMatcherList = new LinkedList<RuleMatcher>();
		ruleMatcherList.clear();
		for (Rule rule : this.languageRule.getRuleList()) {
			RuleMatcher matcher = new RuleMatcher(rule, text);
			boolean found = moveMatcher(matcher, 0);
			if (found) {
				ruleMatcherList.add(matcher);
			}
		}
		segment = null;
		startPosition = 0;
		endPosition = text.length();
	}
	
	/**
	 * Przesuwa iteratory na kolejną pozycje jeśli to konieczne.
	 */
	private void moveMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			if (matcher.getBreakPosition() <= endPosition) {
				boolean found = moveMatcher(matcher, endPosition + 1);
				if (!found) {
					i.remove();
				}
			}
		}
	}
	
	private boolean moveMatcher(RuleMatcher matcher, int start) {
		try {
			matcher.find(start);
			return !matcher.hitEnd();
		} catch (EndOfStreamException e) {
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
