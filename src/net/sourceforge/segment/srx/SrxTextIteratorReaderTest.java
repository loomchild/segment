package net.sourceforge.segment.srx;

import java.io.StringReader;

import net.sourceforge.segment.TextIterator;

public class SrxTextIteratorReaderTest extends AbstractSrxTextIteratorTest {

	private static final int BUFFER_SIZE = 60;
	
	private static final int MARGIN = 10;
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		StringReader reader = new StringReader(text);
		return new SrxTextIterator(document, languageCode, 
				reader, BUFFER_SIZE, MARGIN);
	}
	
}
