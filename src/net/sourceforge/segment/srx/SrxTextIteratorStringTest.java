package net.sourceforge.segment.srx;

import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.segment.TextIterator;

public class SrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new SrxTextIterator(document, languageCode, text);
	}

	@Ignore
	@Test
	public void testOverlappingBreakingRules() {
	}

}
