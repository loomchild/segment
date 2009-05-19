package net.sourceforge.segment.srx;

import java.io.StringReader;

import net.sourceforge.segment.TextIterator;

public class SrxTextIteratorReaderTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		StringReader reader = new StringReader(text);
		return new SrxTextIterator(document, languageCode, reader);
	}
	
}
