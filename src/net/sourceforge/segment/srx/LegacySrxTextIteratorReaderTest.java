package net.sourceforge.segment.srx;

import java.io.StringReader;

import net.sourceforge.segment.TextIterator;

public class LegacySrxTextIteratorReaderTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		StringReader reader = new StringReader(text);
		return new LegacySrxTextIterator(document, languageCode, reader);
	}

}
