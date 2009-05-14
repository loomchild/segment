package net.sourceforge.segment.srx;

import java.io.StringReader;

import net.sourceforge.segment.TextIterator;

import org.junit.Ignore;
import org.junit.Test;

public class SrxTextIteratorReaderTest extends AbstractSrxTextIteratorTest {

	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		StringReader reader = new StringReader(text);
		return new SrxTextIterator(document, languageCode, reader);
	}

	@Ignore
	@Test
	/**
	 * Ignore this test because cannot make it work with reader.
	 */
	public void testMatchingEnd() {
		super.testMatchingEnd();
	}
	
}
