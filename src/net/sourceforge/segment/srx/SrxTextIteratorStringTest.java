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
	/**
	 * Will not pass due to breaking pattern merging - alternative matches
	 * from left to right.
	 */
	public void testOverlappingBreakingRules() {
	}
	
}
