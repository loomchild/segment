package net.sourceforge.segment.srx;

import net.sourceforge.segment.TextIterator;

public class LegacySrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new LegacySrxTextIterator(document, languageCode, text);
	}
}
