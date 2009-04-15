package net.sourceforge.segment.srx;

import java.util.List;

import junit.framework.TestCase;

public class SrxDocumentTest extends TestCase {

	public void testDocument() {
		SrxDocument document = new SrxDocument();

		LanguageRule languageRule1 = new LanguageRule("1");
		LanguageRule languageRule2 = new LanguageRule("2");
		LanguageRule languageRule3 = new LanguageRule("3");

		document.addLanguageMap("aaa", languageRule1);
		document.addLanguageMap("ab", languageRule2);
		document.addLanguageMap("a+", languageRule3);

		document.setCascade(true);

		List<LanguageRule> languageRuleList = document
				.getLanguageRuleList("aaa");
		assertEquals(2, languageRuleList.size());
		assertEquals(languageRule1, languageRuleList.get(0));
		assertEquals(languageRule3, languageRuleList.get(1));

		languageRuleList = document.getLanguageRuleList("xxx");
		assertEquals(0, languageRuleList.size());

		document.setCascade(false);

		languageRuleList = document.getLanguageRuleList("aaa");
		assertEquals(1, languageRuleList.size());
		assertEquals(languageRule1, languageRuleList.get(0));
	}

}
