package split.srx;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static loomchild.util.Utils.readAll;
import split.splitter.Splitter;

/**
 * Reprezentuje splitter dzielący na podstawie reguł zawartych w pliku srx.
 *
 * @author loomchild
 */
public class SrxSplitter implements Splitter {
	
	private SplitPattern splitPattern;
	
	private Matcher matcher;
	
	private CharSequence text;
	
	private String segment;

	private int startPosition, endPosition;
	
	private Reader reader;

	/**
	 * Tworzy splitter dla danego wzorca dzielenia i tekstu.
	 * @param splitPattern Wzorzec dzielenia.
	 * @param text Tekst.
	 */
	public SrxSplitter(SplitPattern splitPattern, CharSequence text) {
		this.reader = null;
		this.splitPattern = splitPattern;
		initialize(text);
	}

	/**
	 * Tworzy splitter dla danego wzorca dzielenia i strumienia wejściowego. 
	 * Obecna implementacja po prostu czyta cały strumień na początku więc
	 * odpowiada wersji z ciągiem znaków.
	 * @param splitPattern Wzorzec dzielenia.
	 * @param reader Strumień wejsćiowy.
	 */
	public SrxSplitter(SplitPattern splitPattern, Reader reader) {
		this.reader = reader;
		this.text = null;
		this.splitPattern = splitPattern;
	}

	/**
	 * Wyszukuje następne dopasowanie.
	 * @return Zwraca następny segment albo null jeśli nie istnieje.
	 * @throws IOException Zgłaszany gdy nastąpi błąd przy odczycie strumienia.
	 */
	public String next() throws IOException {
		if (text == null) {
			String text = readAll(reader);
			initialize(text);
		}
		if (hasNext()) {
			boolean found = matcher.find();
			if (found) {
				//while (endPosition == matcher.end()) {
				//	matcher.find();
				//}
				endPosition = matcher.end();
				matcher.region(endPosition + 1, matcher.regionEnd());
			} else {
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
		return (matcher == null) || (!matcher.hitEnd());
	}

	/**
	 * @return Zwraca true gdy strumień nie zablokuje przy kolejnym segmencie 
	 */
	public boolean isReady() throws IOException {
		return hasNext();
	}

	/**
	 * Inicjalizuje splitter..
	 */
	private void initialize(CharSequence text) {
		this.text = text;
		startPosition = 0;
		endPosition = text.length();
		matcher = splitPattern.get().matcher(text);
	}
	
}
