package net.sourceforge.segment.srx.legacy;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.AbstractSrxTextIteratorTest;
import net.sourceforge.segment.srx.SrxDocument;

import org.junit.Ignore;
import org.junit.Test;

public class FastTextIteratorStringTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getTextIterator(SrxDocument document,
			String languageCode, String text) {
		return new FastTextIterator(document, languageCode, text);
	}
	
	@Ignore
	@Test
	public void testOverlappingBreakRules() {
	}

}
