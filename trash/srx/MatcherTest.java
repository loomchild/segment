package srx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

public class MatcherTest extends TestCase {

	public static final String TEXT = 
		"Ala ma kota. Prof. Kot nie wie kim jest. Ech";
	
	public void testSplit() {
		Matcher matcher = 
			new Matcher(languageRule, TEXT);
		checkMatcher(matcher);
	}
	
	public void testReaderSplit() {
		Reader reader = createReader();
		Matcher matcher = null;
		try {
			matcher = new Matcher(languageRule, reader);
		} catch (IOException e) {
			fail("Error parsing srx document: " + e.getMessage());
		}
		checkMatcher(matcher);
	}
	
	private Reader createReader() {
		StringReader reader = new StringReader(TEXT);
		return reader;
	}

	
	private LanguageRule createRule() {
		LanguageRule rule = new LanguageRule();
		rule.addRule(new Rule(false, "Prof\\.", " "));
		rule.addRule(new Rule(true, "\\.", " "));
		return rule;
	}
	
	private void checkMatcher(Matcher matcher) {
		assertFalse(matcher.hitEnd());
		assertNull(matcher.getSegment());
		matcher.find();
		String segment1 = matcher.getSegment();
		assertEquals("Ala ma kota.", segment1);
		matcher.find();
		String segment2 = matcher.getSegment();
		assertEquals(" Prof. Kot nie wie kim jest.", segment2);
		matcher.find();
		String segment3 = matcher.getSegment();
		assertEquals(" Ech", segment3);
		assertFalse(matcher.find());
		assertTrue(matcher.hitEnd());
		String segment4 = matcher.getSegment();
		assertNull(segment4);
		//Łańcuch podzielony ma taką samą długość jak wejściowy
		assertEquals(TEXT, segment1 + segment2 + segment3);
	}
	
	private LanguageRule languageRule = createRule();

}
