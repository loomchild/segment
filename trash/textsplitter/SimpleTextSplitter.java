package textsplitter;

import java.io.Reader;

import split.simple.SimpleTextIterator;

/**
 * Adapter klasy SimpleTextIterator.
 * 
 * @author loomchild
 */
public class SimpleTextSplitter extends AbstractTextSplitter {

	/**
	 * Inicjalizuje splitter, kod języka jest ignorowany.
	 * 
	 * @param reader
	 *            Strumień wejściowy.
	 * @param languageCode
	 *            Kod języka.
	 */
	public void initialize(Reader reader, String languageCode) {
		setTextIterator(new SimpleTextIterator(reader));
	}

}
