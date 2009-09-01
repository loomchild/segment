package net.sourceforge.segment.srx;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.segment.TextIterator;

import org.junit.Test;

public class SrxTextIteratorStringTest extends AbstractSrxTextIteratorTest {
	
	protected TextIterator getTextIterator(String text,
			SrxDocument document, String languageCode) {
		return new SrxTextIterator(document, languageCode, text);
	}
	
	public static final String[] MAX_LOOKBEHIND_CONSTRUCT_LENGTH_RESULT = 
		new String[] {"XAAA.", "XBB.XC"};

	public static final SrxDocument MAX_LOOKBEHIND_CONSTRUCT_LENGTH_DOCUMENT = 
		createMaxLookbehindConstructLengthDocument();
	
	public static SrxDocument createMaxLookbehindConstructLengthDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "XA+\\.", ""));
		languageRule.addRule(new Rule(false, "XB+\\.", ""));
		languageRule.addRule(new Rule(true, "\\.", ""));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);
		return document;
	}
	
	/**
	 * Test if setting 
	 * {@link SrxTextIterator#MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER} works.
	 */
	@Test
	public void testMaxLookbehindConstructLength() {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(SrxTextIterator.MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER, 2);
		
		String text = merge(MAX_LOOKBEHIND_CONSTRUCT_LENGTH_RESULT);
		TextIterator textIterator = 
			new SrxTextIterator(MAX_LOOKBEHIND_CONSTRUCT_LENGTH_DOCUMENT, 
					"", text, parameterMap);
		List<String> segmentList = segment(textIterator);
		String[] segmentArray = segmentList.toArray(new String[segmentList.size()]); 

		assertEquals(MAX_LOOKBEHIND_CONSTRUCT_LENGTH_RESULT, segmentArray);
	}
	
}
