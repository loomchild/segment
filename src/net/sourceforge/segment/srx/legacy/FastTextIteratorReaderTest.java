package net.sourceforge.segment.srx.legacy;

import java.io.StringReader;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.AbstractSrxTextIteratorTest;
import net.sourceforge.segment.srx.SrxDocument;

import org.junit.Ignore;
import org.junit.Test;

public class FastTextIteratorReaderTest extends AbstractSrxTextIteratorTest {

	private static final int BUFFER_SIZE = 60;
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		StringReader reader = new StringReader(text);
		CharSequence charSequence = new ReaderCharSequence(reader, 
				Integer.MAX_VALUE, BUFFER_SIZE);
		return new FastTextIterator(document, languageCode, charSequence);
	}
	
	@Ignore
	@Test
	public void testOverlappingBreakRules() {
	}

}
