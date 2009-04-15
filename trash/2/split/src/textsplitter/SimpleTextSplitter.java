package textsplitter;

import java.io.Reader;

import split.simple.SimpleSplitter;

/**
 * Adapter klasy SimpleSplitter.
 *
 * @author loomchild
 */
public class SimpleTextSplitter extends AbstractTextSplitter {

	/**
	 * Inicjalizuje splitter, kod języka jest ignorowany.
	 * @param reader Strumień wejściowy.
	 * @param languageCode Kod języka.
	 */
	public void initialize(Reader reader, String languageCode) {
		setSplitter(new SimpleSplitter(reader));
	}

}
