package net.loomchild.segment.srx.io;

import static net.loomchild.segment.util.Util.getReader;
import static net.loomchild.segment.util.Util.getResourceStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.util.List;

import net.loomchild.segment.srx.LanguageRule;
import net.loomchild.segment.srx.SrxDocument;
import net.loomchild.segment.srx.SrxParser;
import net.loomchild.segment.srx.LanguageMap;
import net.loomchild.segment.srx.Rule;
import net.loomchild.segment.util.XmlException;

import org.junit.Test;

public class SrxParsersTest {
	
	private static final SrxParser ONE = new Srx1Parser();
	private static final SrxParser TWO = new Srx2Parser();
	private static final SrxParser ANY = new SrxAnyParser();
	private static final SrxParser SAX = new Srx2SaxParser();
	private static final SrxParser STAX = new Srx2StaxParser();
	

	@Test
	public void testSrx1One() {
		testSrx1(ONE);
	}

	@Test
	public void testSrx1Any() {
		testSrx1(ANY);
	}
	
	@Test
	public void testSrx1Compare() {
		testCompare(SRX_1_DOCUMENT_NAME, new SrxParser[] {ONE, ANY});
	}

	private static final String SRX_1_DOCUMENT_NAME = "net/loomchild/segment/res/test/example1.srx";

	private void testSrx1(SrxParser parser) {
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
	
	
	@Test
	public void testSrx2Two() {
		testSrx2(TWO);
	}

	@Test
	public void testSrx2Sax() {
		testSrx2(SAX);
	}

	@Test
	public void testSrx2Stax() {
		testSrx2(STAX);
	}

	@Test
	public void testSrx2Any() {
		testSrx2(ANY);
	}

	@Test
	public void testSrx2Compare() {
		testCompare(SRX_2_DOCUMENT_NAME, new SrxParser[] {TWO, SAX, STAX, ANY});
	}

	private static final String SRX_2_DOCUMENT_NAME = "net/loomchild/segment/res/test/example.srx";

	private void testSrx2(SrxParser parser) {
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

		Rule rule0 = ruleList.get(0);
		assertEquals(" [Mm]lle\\.", rule0.getBeforePattern());
		assertEquals("\\s", rule0.getAfterPattern());

		Rule rule1 = ruleList.get(1);
		assertEquals("\\s[Mm]lles\\.", rule1.getBeforePattern());
		assertEquals("\\s", rule1.getAfterPattern());
	}


	@Test(expected = XmlException.class)
	public void testSrx2InvalidTwo() {
		testSrx2Invalid(TWO);
	}

	@Test(expected = XmlException.class)
	public void testSrx2InvalidSax() {
		testSrx2Invalid(SAX);
	}

	@Test(expected = XmlException.class)
	public void testSrx2InvalidAny() {
		testSrx2Invalid(ANY);
	}
		
	public static final String INVALID_DOCUMENT_NAME = "net/loomchild/segment/res/test/invalid.srx";

	private void testSrx2Invalid(SrxParser parser) {
	    Reader reader = getReader(getResourceStream(INVALID_DOCUMENT_NAME));
	    parser.parse(reader);
	}

	
	@Test
	public void testSrx2Ticket1Two() {
		testSrx2Ticket1(TWO);
	}

	@Test
	public void testSrx2Ticket1Sax() {
		testSrx2Ticket1(SAX);
	}

	@Test
	public void testSrx2Ticket1Stax() {
		testSrx2Ticket1(STAX);
	}

	@Test
	public void testSrx2Ticket1Any() {
		testSrx2Ticket1(ANY);
	}

	@Test
	public void testSrx2Ticket1Compare() {
		testCompare(TICKET_1_DOCUMENT_NAME, new SrxParser[] {TWO, SAX, STAX, ANY});
	}
	
	public static final String TICKET_1_DOCUMENT_NAME = "net/loomchild/segment/res/test/ticket1.srx";

	private void testSrx2Ticket1(SrxParser parser) {
	    Reader reader = getReader(getResourceStream(TICKET_1_DOCUMENT_NAME));

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
	
	
	public void testCompare(String documentName, SrxParser[] parsers) {
		SrxDocument previousDocument = null;
		for (SrxParser parser : parsers) {
		    Reader reader = getReader(getResourceStream(documentName));
		    SrxDocument document = parser.parse(reader);
		    
			if (previousDocument != null) {
				assertSrxDocumentEquals(previousDocument, document);
			}
			
			previousDocument = document;
		}
	}
	
	private void assertSrxDocumentEquals(SrxDocument leftDocument, SrxDocument rightDocument) {
		assertEquals(leftDocument.getCascade(), rightDocument.getCascade());
		
		List<LanguageMap> leftLanguageMapList = leftDocument.getLanguageMapList();
		List<LanguageMap> rightLanguageMapList = rightDocument.getLanguageMapList();
		assertEquals(leftLanguageMapList.size(), rightLanguageMapList.size());
		
		for (int i = 0; i < leftLanguageMapList.size(); ++i) {
			LanguageMap leftLanguageMap = leftLanguageMapList.get(i);
			LanguageMap rightLanguageMap = rightLanguageMapList.get(i);
			
			assertEquals(leftLanguageMap.getLanguagePattern().pattern(), 
					rightLanguageMap.getLanguagePattern().pattern());
			
			LanguageRule leftLanguageRule = leftLanguageMap.getLanguageRule();
			LanguageRule rightLanguageRule = rightLanguageMap.getLanguageRule();
			
			assertEquals(leftLanguageRule.getName(), rightLanguageRule.getName());
			
			List<Rule> leftRuleList = leftLanguageRule.getRuleList();
			List<Rule> rightRuleList = rightLanguageRule.getRuleList();
			
			assertEquals(leftRuleList.size(), rightRuleList.size());
			
			for (int k = 0; k < leftRuleList.size(); ++ k) {
				Rule leftRule = leftRuleList.get(k);
				Rule rightRule = rightRuleList.get(k);
				
				assertEquals(leftRule.isBreak(), rightRule.isBreak());
				assertEquals(leftRule.getBeforePattern(), rightRule.getBeforePattern());
				assertEquals(leftRule.getAfterPattern(), rightRule.getAfterPattern());
			}
			
		}
		
	}

}
