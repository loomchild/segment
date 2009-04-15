package split.srx;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import loomchild.util.xml.XmlException;


public class ParserTest extends TestCase {

	public static final String DOCUMENT_NAME = "data/test/polsplit.srx";
	
	public void testParse() {
		Document document = null;
		try {
			document = Parser.getInstance().parse(DOCUMENT_NAME);
		} catch (IOException e) {
			fail("IO error parsing SRX document: " + e.getMessage());
		} catch (XmlException e) {
			fail("XML error parsing SRX document: " + e.getMessage());
		}
		MapRule mapRule = document.getSingletonMapRule();
		assertNotNull(mapRule);
		LanguageRule defaultLanguageRule = document.getLanguageRule("Default");
		LanguageRule polishLanguageRule = document.getLanguageRule("Polish");
		LanguageRule englishLanguageRule = document.getLanguageRule("English");
		assertEquals(defaultLanguageRule, 
				mapRule.getLanguageMap("").getLanguageRule());
		assertEquals(polishLanguageRule, 
				mapRule.getLanguageMap("PL_pl").getLanguageRule());
		assertEquals(englishLanguageRule, 
				mapRule.getLanguageMap("EN_us").getLanguageRule());
		List<Rule> defaultRuleList = defaultLanguageRule.getRuleList();
		assertEquals(3, defaultRuleList.size());
		Rule rule0 = defaultRuleList.get(0);
		assertFalse(rule0.isBreaking());
		assertEquals("^\\s*[0-9]+\\.", rule0.getBeforePattern());
		assertEquals("\\s", rule0.getAfterPattern());
		Rule rule1 = defaultRuleList.get(1);
		assertTrue(rule1.isBreaking());
		assertEquals("[\\.\\?!]+", rule1.getBeforePattern());
		assertEquals("\\s", rule1.getAfterPattern());
	}
	
}
