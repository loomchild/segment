package split.srx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;
import split.splitter.Splitter;

public class SrxSplitterTest extends TestCase {

	public void testSimpleSplit() throws IOException {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "[Pp]rof\\.", "\\s"));
		languageRule.addRule(new Rule(true, "\\.", "\\s"));
		languageRule.addRule(new Rule(true, "", "\\n"));
		String text = 
			"Ala ma kota. Prof. Kot nie wie kim jest. Ech.\n" +
			"A inny prof. to już w ogole. Uch";
		String[] segmentArray = new String[] {
			"Ala ma kota.", " Prof. Kot nie wie kim jest.",
			" Ech.", "\nA inny prof. to już w ogole.", " Uch"
		};
		StringReader reader = new StringReader(text);
		SplitPattern splitPattern = new SplitPattern(languageRule);
		Splitter splitter = new SrxSplitter(splitPattern, reader);
		assertTrue(splitter.isReady());
		assertSplitterEquals(segmentArray, splitter);
		assertFalse(splitter.isReady());
		
		//Testowanie czy przez napis daje takie same wyniki
		splitter = new SrxSplitter(splitPattern, text);
		assertTrue(splitter.isReady());
		assertSplitterEquals(segmentArray, splitter);
		assertFalse(splitter.isReady());
	}
	
	/**
	 * Sprawdza czy splitter radzi sobie z alternatywami w regułach.
	 */
	public void testAlternativeRuleSplit() throws IOException {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "(n\\.)|(e\\.)|(dn\\.)", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		String text = 
			"W 59 n. e. Julek nie zrobił nic ciekawego. " +
			"Drugie dn. to: Ja też nie";
		String[] segmentArray = new String[] {
			"W 59 n. e. Julek nie zrobił nic ciekawego.", 
			" Drugie dn. to: Ja też nie"
		};
		StringReader reader = new StringReader(text);
		SplitPattern splitPattern = new SplitPattern(languageRule);
		Splitter splitter = new SrxSplitter(splitPattern, reader);
		assertSplitterEquals(segmentArray, splitter);
	}

	/**
	 * Sprawdza czy przeskakiwanie po znalezieniu reguły do pozycji po niej
	 * działa poprawnie. Przy okazji testuje łączenie reguł o wspólnej końcówce.
	 */
	public void testOverlappingRulesSplit() throws IOException {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "n\\.", " "));
		languageRule.addRule(new Rule(false, "n\\. e\\.", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		String text = 
			"W 59 n. e. Julek nie zrobił nic ciekawego. Ja też nie";
		String[] segmentArray = new String[] {
			"W 59 n. e. Julek nie zrobił nic ciekawego.", " Ja też nie"
		};
		StringReader reader = new StringReader(text);
		SplitPattern splitPattern = new SplitPattern(languageRule);
		Splitter splitter = new SrxSplitter(splitPattern, reader);
		assertSplitterEquals(segmentArray, splitter);
	}

	/**
	 * Sprawdza dziłanie dla przeplatanych reguł łamiących i wyjątków.
	 */
	public void testInterlacedRulesSplit() throws IOException {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "a[\\.\\?]", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		languageRule.addRule(new Rule(false, "b[\\.\\?]", " "));
		languageRule.addRule(new Rule(true, "\\?", " "));
		String text = 
			"a? b? a. b. Koniec";
		String[] segmentArray = new String[] {
			"a? b? a. b.", " Koniec"
		};
		StringReader reader = new StringReader(text);
		SplitPattern splitPattern = new SplitPattern(languageRule);
		Splitter splitter = new SrxSplitter(splitPattern, reader);
		assertSplitterEquals(segmentArray, splitter);
	}

	private void assertSplitterEquals(String[] expectedSegmentArray, 
			Splitter splitter) {
		for (int i = 0; i < expectedSegmentArray.length; ++i) {
			 String expectedSegment = expectedSegmentArray[i];
			assertTrue("nr = " + i, splitter.hasNext());
			String segment = null;
			try {
				segment = splitter.next();
			} catch (IOException e) {
				fail("Błąd wejścia wyjścia: " + e.getMessage());
			}
			assertEquals(expectedSegment, segment);
		}
		assertFalse(splitter.hasNext());
	}
	
}
 