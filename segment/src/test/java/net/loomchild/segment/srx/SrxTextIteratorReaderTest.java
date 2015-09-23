package net.loomchild.segment.srx;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import net.loomchild.segment.TextIterator;

public class SrxTextIteratorReaderTest extends AbstractSrxTextIteratorTest {

	private static final int BUFFER_SIZE = 60;
	
	private static final int MARGIN = 10;
	
	protected TextIterator getTextIterator(SrxDocument document,
			String languageCode, String text) {
		StringReader reader = new StringReader(text);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(SrxTextIterator.BUFFER_LENGTH_PARAMETER, BUFFER_SIZE);
		parameterMap.put(SrxTextIterator.MARGIN_PARAMETER, MARGIN);
		return new SrxTextIterator(document, languageCode, reader, parameterMap);
	}
	
}
