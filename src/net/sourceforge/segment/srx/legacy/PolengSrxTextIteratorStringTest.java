package net.sourceforge.segment.srx.legacy;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.AbstractSrxTextIteratorTest;
import net.sourceforge.segment.srx.SrxDocument;

public class PolengSrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new PolengSrxTextIterator(document, languageCode, text);
	}
}
