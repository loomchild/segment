package net.sourceforge.segment.srx;

import java.io.Reader;

import net.sourceforge.segment.TextIterator;

public class SrxTextIteratorTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getStringTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new SrxTextIterator(document, languageCode, text);
	}

	protected TextIterator getReaderTextIterator(Reader reader,
			SrxDocument document, String languageCode) {
		return new SrxTextIterator(document, languageCode, reader);
	}

}
