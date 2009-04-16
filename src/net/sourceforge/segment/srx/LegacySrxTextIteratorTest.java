package net.sourceforge.segment.srx;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.segment.TextIterator;

public class LegacySrxTextIteratorTest extends AbstractSrxTextIteratorTest {
	
	protected List<TextIterator> getTextIteratorList(String text,
			SrxDocument document, String languageCode) {
		List<TextIterator> textIteratorList = new ArrayList<TextIterator>();
		
		textIteratorList.add(new LegacySrxTextIterator(document, languageCode, text));

		StringReader reader = new StringReader(text);
		textIteratorList.add(new LegacySrxTextIterator(document, languageCode, reader));

		return textIteratorList;
	}

}
