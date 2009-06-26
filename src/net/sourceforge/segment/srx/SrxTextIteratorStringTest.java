package net.sourceforge.segment.srx;

import net.sourceforge.segment.TextIterator;

public class SrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new SrxTextIterator(document, languageCode, text);
	}
}
