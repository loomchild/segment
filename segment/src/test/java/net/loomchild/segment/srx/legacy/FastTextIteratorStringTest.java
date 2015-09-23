package net.loomchild.segment.srx.legacy;

import net.loomchild.segment.srx.SrxDocument;
import net.loomchild.segment.TextIterator;
import net.loomchild.segment.srx.AbstractSrxTextIteratorTest;

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
