package net.sourceforge.segment.srx;

import java.io.Reader;

import net.sourceforge.segment.TextIterator;

public class LegacySrxTextIteratorTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getStringTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new LegacySrxTextIterator(document, languageCode, text);
	}

	protected TextIterator getReaderTextIterator(Reader reader,
			SrxDocument document, String languageCode) {
		return new LegacySrxTextIterator(document, languageCode, reader);
	}

}
