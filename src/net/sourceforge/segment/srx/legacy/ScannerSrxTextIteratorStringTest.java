package net.sourceforge.segment.srx.legacy;

import java.util.Collections;
import java.util.Map;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.AbstractSrxTextIteratorTest;
import net.sourceforge.segment.srx.SrxDocument;

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
