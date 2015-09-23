package net.loomchild.segment.srx.legacy;

import java.util.Collections;
import java.util.Map;

import net.loomchild.segment.TextIterator;
import net.loomchild.segment.srx.AbstractSrxTextIteratorTest;
import net.loomchild.segment.srx.SrxDocument;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for @link {@link ScannerSrxTextIterator}.
 * @author loomchild
 */
public class ScannerSrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(SrxDocument document,
			String languageCode, String text) {
		Map<String, Object> parameterMap = Collections.emptyMap();
		return new ScannerSrxTextIterator(document, languageCode, text, parameterMap);
	}
	
	@Ignore
	@Test
	public void testOverlappingBreakRules() {
	}
	
}
