package net.sourceforge.segment.srx.legacy;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.AbstractSrxTextIteratorTest;
import net.sourceforge.segment.srx.SrxDocument;

import org.junit.Ignore;
import org.junit.Test;

public class FastTextIteratorStringTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new FastTextIterator(document, languageCode, text);
	}
	
	@Ignore
	@Test
	public void testOverlappingBreakRules() {
	}

}
