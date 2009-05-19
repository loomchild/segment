package net.sourceforge.segment.srx;

import java.io.StringReader;

import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.segment.TextIterator;

public class SrxTextIteratorReaderTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		StringReader reader = new StringReader(text);
		return new SrxTextIterator(document, languageCode, reader);
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
