package net.loomchild.segment.srx.legacy;

import net.loomchild.segment.TextIterator;
import net.loomchild.segment.srx.AbstractSrxTextIteratorTest;
import net.loomchild.segment.srx.SrxDocument;

public class AccurateSrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(SrxDocument document,
			String languageCode, String text) {
		return new AccurateSrxTextIterator(document, languageCode, text);
	}
}
