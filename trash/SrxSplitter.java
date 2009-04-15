package split.srx;

import static loomchild.util.io.Util.readAll;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import loomchild.util.exceptions.IORuntimeException;

import split.splitter.Splitter;

/**
 * Reprezentuje splitter dzielący na podstawie reguł zawartych w pliku srx.
 *
 * @author loomchild
 */
public class SrxSplitter implements Splitter {
	
	public static final int CHUNK_MAX_LENGTH = 1024;
	public static final int CHUNK_OVERLAP = 50;
	

	private LanguageRule languageRule;
	
	private StringBuffer chunk;
	
	private String segment;

	private List<RuleMatcher> ruleMatcherList;
	
	private int startPosition, endPosition;
	
	private Reader reader;
	
	private boolean hitEnd;

	/**
	 * Tworzy splitter dla danej reguły językowej i tekstu.
	 * @param languageRule Reguła językowa.
	 * @param text Tekst.
	 */
	public SrxSplitter(LanguageRule languageRule, CharSequence text) {
		this.reader = new EmptyReader();
		this.languageRule = languageRule;
		this.chunk = text;
		initialize();
	}

	/**
	 * Tworzy splitter dla danej reguły językowej i strumienia wejściowego. 
	 * Obecna implementacja po prostu czyta cały strumień na początku więc
	 * odpowiada wersji z ciągiem znaków.
	 * @param languageRule Reguła językowa.
	 * @param reader Strumień wejsćiowy.
	 */
	public SrxSplitter(LanguageRule languageRule, Reader reader) {
		this.reader = reader;
		this.languageRule = languageRule;
		this.chunk = null;
		initialize();
	}

	/**
	 * Wyszukuje następne dopasowanie.
	 * @return Zwraca następny segment albo null jeśli nie istnieje.
	 * @throws IOSRuntimeException Zgłaszany gdy nastąpi błąd przy odczycie strumienia.
	 */
	public String next() {
		if (chunk == null) {
			String text = readAll(reader);
			initialize(text);
		}
		if (hasNext()) {
			boolean found = false;
			while ((ruleMatcherList.size() > 0) && !found) {
				RuleMatcher minMatcher = getMinMatcher();
				found = minMatcher.getRule().isBreaking();
				endPosition = minMatcher.getBreakPosition();
				moveMatchers();
			}
			if (!found) {
				endPosition = chunk.length();
			}
			segment = chunk.subSequence(startPosition, endPosition).toString();
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
		return !hitEnd;
	}

	/**
	 * @return Zwraca true gdy strumień nie zablokuje przy kolejnym segmencie 
	 */
	public boolean isReady() {
		return false;
	}

	/**
	 * Inicjalizuje splitter. Tworzy liste iteratorów.
	 */
	private void initialize() {
		this.ruleMatcherList = new LinkedList<RuleMatcher>();
		ruleMatcherList.clear();
		for (Rule rule : this.languageRule.getRuleList()) {
			RuleMatcher matcher = new RuleMatcher(rule, text);
			matcher.find();
			if (!matcher.hitEnd()) {
				ruleMatcherList.add(matcher);
			}
		}
		segment = null;
		startPosition = 0;
		endPosition = text.length();
	}
	
	private void readSegment(boolean canBlock) {
		fillChunk(canBlock);
	}
	
	/**
	 * Reads characters from the reader until chunk length is equal to 
	 * maximum or end of the reader has been reached.
	 * @param canBlock If false then it will stop reading when reader.ready() 
	 * returns false.
	 */
	private void fillChunk(boolean canBlock) {
		try {
			if (canBlock) {
				if (!hitEnd && (chunk.length() < CHUNK_MAX_LENGTH)) {
					int toRead = CHUNK_MAX_LENGTH - chunk.length();
					char[] buffer = new char[toRead];
					int read = reader.read(buffer);
					if (read < toRead) {
						hitEnd = true;
					}
					if (read > 0) {
						chunk.append(buffer, 0, read);
					}
				}
			} else {
				while (!hitEnd && reader.ready() && 
						(chunk.length() < CHUNK_MAX_LENGTH)) {
					int character = reader.read();
					if (character < 0) {
						hitEnd = true;
					} else {
						chunk.append(character);
					}
				}
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
	
	/**
	 * Przesuwa iteratory na kolejną pozycje jeśli to konieczne.
	 */
	private void moveMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			if (matcher.getBreakPosition() <= endPosition) {
				matcher.find(endPosition + 1);
				if (matcher.hitEnd()) {
					i.remove();
				}
			}
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
