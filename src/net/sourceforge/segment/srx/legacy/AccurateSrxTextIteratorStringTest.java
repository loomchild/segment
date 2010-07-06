package net.sourceforge.segment.srx.legacy;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.AbstractSrxTextIteratorTest;
import net.sourceforge.segment.srx.SrxDocument;

public class AccurateSrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(SrxDocument document,
			String languageCode, String text) {
		return new AccurateSrxTextIterator(document, languageCode, text);
	}
}
