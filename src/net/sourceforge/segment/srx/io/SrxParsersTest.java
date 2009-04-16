package net.sourceforge.segment.srx.io;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;

import java.io.Reader;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;

public class SrxParsersTest extends TestCase {

	public static final String SRX_1_DOCUMENT_NAME = "net/sourceforge/segment/res/test/example1.srx";

	public static final String SRX_2_DOCUMENT_NAME = "net/sourceforge/segment/res/test/example.srx";

	public void testSrx1Parse() {
		testSrx1Parse(new Srx1Parser());
	}

	public void testSrx2Parse() {
		testSrx2Parse(new Srx2Parser());
	}

	public void testAnyParse() {
		testSrx1Parse(new SrxAnyParser());
		testSrx2Parse(new SrxAnyParser());
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
