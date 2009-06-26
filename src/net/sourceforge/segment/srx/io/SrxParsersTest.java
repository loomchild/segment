package net.sourceforge.segment.srx.io;

import static net.sourceforge.segment.util.Util.getReader;
import static net.sourceforge.segment.util.Util.getResourceStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.util.List;

import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.util.XmlException;

import org.junit.Test;

public class SrxParsersTest {

	public static final String SRX_1_DOCUMENT_NAME = "net/sourceforge/segment/res/test/example1.srx";

	public static final String SRX_2_DOCUMENT_NAME = "net/sourceforge/segment/res/test/example.srx";

	public static final String TICKET_1_DOCUMENT_NAME = "net/sourceforge/segment/res/test/ticket1.srx";

	public static final String INVALID_DOCUMENT_NAME = "net/sourceforge/segment/res/test/invalid.srx";

	@Test
	public void testSrx1Parse() {
		testSrx1Parse(new Srx1Parser());
	}

	@Test
	public void testSrx2Parse() {
		testSrx2Parse(new Srx2Parser());
	}

	@Test
	public void testAnyParse() {
		testSrx1Parse(new SrxAnyParser());
		testSrx2Parse(new SrxAnyParser());
	}
	
	@Test(expected = XmlException.class)
	public void testSrx2ParseInvalid() {
	    Reader reader = getReader(getResourceStream(INVALID_DOCUMENT_NAME));
	    SrxParser parser = new Srx2Parser();
	    parser.parse(reader);
	}

	@Test
	public void testSrx2ParseTicket1() {
	    Reader reader = getReader(getResourceStream(TICKET_1_DOCUMENT_NAME));

	    SrxParser parser = new Srx2Parser();
	    SrxDocument document = parser.parse(reader);

	    assertTrue(document.getCascade());

	    List<LanguageRule> languageRuleList = document
	    .getLanguageRuleList("en");
	    
	    LanguageRule languageRule = languageRuleList.get(0);
	    assertEquals("Default", languageRule.getName());

	    List<Rule> ruleList = languageRule.getRuleList();
	    assertEquals(1, ruleList.size());    

	    Rule rule = ruleList.get(0);
	    assertEquals("[\\.!?…]['»\"”\\)\\]\\}]?\\u0002?\\s", rule.getBeforePattern());
	    assertEquals("", rule.getAfterPattern());
	}

	public void testSrx1Parse(SrxParser parser) {
		Reader reader = getReader(getResourceStream(SRX_1_DOCUMENT_NAME));

		SrxDocument document = parser.parse(reader);

		assertFalse(document.getCascade());

		List<LanguageRule> languageRuleList = document
				.getLanguageRuleList("en");
		assertEquals(1, languageRuleList.size());

		LanguageRule languageRule = languageRuleList.get(0);
		assertEquals("Default", languageRule.getName());

		List<Rule> ruleList = languageRule.getRuleList();
		assertEquals(5, ruleList.size());

		Rule rule = ruleList.get(1);
		assertEquals("[Ee][Tt][Cc]\\.", rule.getBeforePattern());
		assertEquals("\\s[a-z]", rule.getAfterPattern());
	}

	public void testSrx2Parse(SrxParser parser) {
		Reader reader = getReader(getResourceStream(SRX_2_DOCUMENT_NAME));

		SrxDocument document = parser.parse(reader);

		assertTrue(document.getCascade());

		List<LanguageRule> languageRuleList = document
				.getLanguageRuleList("fr_FR");
		assertEquals(2, languageRuleList.size());

		LanguageRule languageRule = languageRuleList.get(0);
		assertEquals("French", languageRule.getName());

		List<Rule> ruleList = languageRule.getRuleList();
		assertEquals(4, ruleList.size());

		Rule rule = ruleList.get(1);
		assertEquals("\\s[Mm]lles\\.", rule.getBeforePattern());
		assertEquals("\\s", rule.getAfterPattern());
	}

}
