package net.sourceforge.segment.srx;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;
import static net.rootnode.loomchild.util.testing.Utils.assertListEquals;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.io.Srx2Parser;

import org.junit.Ignore;
import org.junit.Test;

public abstract class AbstractSrxTextIteratorTest {

	public static final String[] SIMPLE_RESULT = new String[] { 
		"Ala ma kota.",
		" Prof. Kot nie wie kim jest.", " Ech.",
		"\nA inny prof. to już w ogole.", " Uch"
		};

	public static final String SIMPLE_LANGUAGE = "pl";
	
	public static final SrxDocument SIMPLE_DOCUMENT = 
		createSimpleDocument();
	
	public static SrxDocument createSimpleDocument() {
		LanguageRule languageRulePL = new LanguageRule("Polish");
		languageRulePL.addRule(new Rule(false, "[Pp]rof\\.", "\\s"));

		LanguageRule languageRuleEN = new LanguageRule("English");
		languageRuleEN.addRule(new Rule(false, "Mr\\.", "\\s"));

		LanguageRule languageRuleDEF = new LanguageRule("Default");
		languageRuleDEF.addRule(new Rule(true, "\\.", "\\s"));
		languageRuleDEF.addRule(new Rule(true, "", "\\n"));

		SrxDocument document = new SrxDocument();
		document.addLanguageMap("pl.*", languageRulePL);
		document.addLanguageMap("en.*", languageRuleEN);
		document.addLanguageMap(".*", languageRuleDEF);

		return document;
	}
	
	/**
	 * Test some simple splitting with multiple rules.
	 */
	@Test
	public void testSimpleSplit() {
		performTest(SIMPLE_RESULT, SIMPLE_DOCUMENT, SIMPLE_LANGUAGE);
	}


	public static final String[] EMPTY_RESULT = new String[] { 
		};

	public static final String EMPTY_LANGUAGE = "";
	
	public static final SrxDocument EMPTY_DOCUMENT = 
		createEmptyDocument();
	
	public static SrxDocument createEmptyDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, ".", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}

	/**
	 * Tests if when the text is empty no segments are returned.
	 */
	@Test
	public void testEmptyText() {
		performTest(EMPTY_RESULT, EMPTY_DOCUMENT, EMPTY_LANGUAGE);
	}

	
	public static final String[] ALTERNATIVE_RULE_RESULT = new String[] { 
		"W 59 n. e. Julek nie zrobił nic ciekawego.",
		" Drugie dn. to: Ja też nie"
		};

	public static final String ALTERNATIVE_RULE_LANGUAGE = "";
	
	public static final SrxDocument ALTERNATIVE_RULE_DOCUMENT = 
		createAlternativeRuleDocument();
	
	public static SrxDocument createAlternativeRuleDocument() {
		LanguageRule languageRule = new LanguageRule("Deafult");
		languageRule.addRule(new Rule(false, "(n\\.)|(e\\.)|(dn\\.)", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}

	/**
	 * Tests if splitter can deal with alternatives in patterns.
	 */
	@Test
	public void testAlternativeRuleSplit() {
		performTest(ALTERNATIVE_RULE_RESULT, 
				ALTERNATIVE_RULE_DOCUMENT, ALTERNATIVE_RULE_LANGUAGE);
	}
	

	public static final String[] OVERLAPPING_RULES_RESULT = new String[] { 
		"W 59 n.e. Julek nie zrobił nic ciekawego.", 
		" Ja też nie"
		};

	public static final String OVERLAPPING_RULES_LANGUAGE = "";
	
	public static final SrxDocument OVERLAPPING_RULES_DOCUMENT = 
		createOverlappingRulesDocument();
	
	public static SrxDocument createOverlappingRulesDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "n\\.", ""));
		languageRule.addRule(new Rule(false, "n\\.e\\.", ""));
		languageRule.addRule(new Rule(true, "\\.", ""));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Checks if splitter works with rules with common prefix.
	 */
	@Test
	public void testOverlappingRulesSplit() {
		performTest(OVERLAPPING_RULES_RESULT, 
				OVERLAPPING_RULES_DOCUMENT, OVERLAPPING_RULES_LANGUAGE);
	}


	public static final String[] INTERLACED_RULES_RESULT = new String[] { 
		"a? b? a. b.", 
		" c.", 
		" d."
		};

	public static final String INTERLACED_RULES_LANGUAGE = "";
	
	public static final SrxDocument INTERLACED_RULES_DOCUMENT = 
		createInterlacedRulesDocument();
	
	public static SrxDocument createInterlacedRulesDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "a[\\.\\?]", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		languageRule.addRule(new Rule(false, "(b[\\.\\?])", " "));
		languageRule.addRule(new Rule(true, "\\?", " "));
		languageRule.addRule(new Rule(false, "c[\\.\\?]", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Tests splitting when braking and non-breaking rules are interlaced.
	 */
	@Test
	public void testInterlacedRulesSplit() throws IOException {
		performTest(INTERLACED_RULES_RESULT, 
				INTERLACED_RULES_DOCUMENT, INTERLACED_RULES_LANGUAGE);
	}


	public static final String[] NO_BREAKING_RULES_RESULT = new String[] { 
		"abcab"
		};

	public static final String NO_BREAKING_RULES_LANGUAGE = "";
	
	public static final SrxDocument NO_BREAKING_RULES_DOCUMENT = 
		createNoBreakingRulesDocument();
	
	public static SrxDocument createNoBreakingRulesDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "a", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Tests if when there is no breaking rules text will not be splitted.
	 */
	@Test
	public void testNoBreakingRules() {
		performTest(NO_BREAKING_RULES_RESULT, 
				NO_BREAKING_RULES_DOCUMENT, NO_BREAKING_RULES_LANGUAGE);
	}


	public static final String[] INFINITE_NEGATIVE_RULE_RESULT = new String[] { 
		"Abc 99. Def.", 
		" Xyz."
		};

	public static final String INFINITE_NEGATIVE_RULE_LANGUAGE = "";
	
	public static final SrxDocument INFINITE_NEGATIVE_RULE_DOCUMENT = 
		createInfiniteNegativeRuleDocument();
	
	public static SrxDocument createInfiniteNegativeRuleDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "([0-9]+\\.|[0-9]{1,}\\.|[0-9][0-9]*\\.)", "\\s"));
		languageRule.addRule(new Rule(true, "\\.", "\\s"));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Tests if splitter can work with infinite length non breaking rule.
	 */
	@Test
	public void testInfiniteNegativeRule() {
		performTest(INFINITE_NEGATIVE_RULE_RESULT, 
				INFINITE_NEGATIVE_RULE_DOCUMENT, INFINITE_NEGATIVE_RULE_LANGUAGE);
	}


	public static final String[] ONLY_BREAKING_RULES_RESULT = new String[] { 
		"Abc 99.", 
		" Def.", 
		" Xyz."
		};

	public static final String ONLY_BREAKING_RULES_LANGUAGE = "";
	
	public static final SrxDocument ONLY_BREAKING_RULES_DOCUMENT = 
		createOnlyBreakingRulesDocument();
	
	public static SrxDocument createOnlyBreakingRulesDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, "\\.", "\\s"));
		languageRule.addRule(new Rule(true, "", "\\n"));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Tests if splitter can when there are only breaking rules.
	 */
	@Test
	public void testOnlyBreakingRules() {
		performTest(ONLY_BREAKING_RULES_RESULT, 
				 ONLY_BREAKING_RULES_DOCUMENT, ONLY_BREAKING_RULES_LANGUAGE);
	}

	
	public static final String[] BREAK_AT_THE_END_RESULT = new String[] { 
		"a."
		};

	public static final String BREAK_AT_THE_END_LANGUAGE = "";
	
	public static final SrxDocument BREAK_AT_THE_END_DOCUMENT = 
		createBreakAtTheEndDocument();
	
	public static SrxDocument createBreakAtTheEndDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, "\\.", ""));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Tests if when break is at the end of text no blank segment is returned.
	 */
	@Test
	public void testBreakAtTheEndOfText() {
		performTest(BREAK_AT_THE_END_RESULT, 
				BREAK_AT_THE_END_DOCUMENT, BREAK_AT_THE_END_LANGUAGE);
	}
	
	
	public static final String[] EMPTY_NON_BREAKING_RULE_RESULT = new String[] { 
		"a. b. c"
		};

	public static final String EMPTY_NON_BREAKING_RULE_LANGUAGE = "";
	
	public static final SrxDocument EMPTY_NON_BREAKING_RULE_DOCUMENT = 
		createEmptyNonBreakingRuleDocument();
	
	public static SrxDocument createEmptyNonBreakingRuleDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "", ""));
		languageRule.addRule(new Rule(true, "\\.", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}

	/**
	 * Tests if when there is empty (matching all) non breaking rule text 
	 * will not be split.
	 */
	@Test
	public void testEmptyNonBreakingRule() {
		performTest(EMPTY_NON_BREAKING_RULE_RESULT, 
				EMPTY_NON_BREAKING_RULE_DOCUMENT, 
				EMPTY_NON_BREAKING_RULE_LANGUAGE);
	}
	
	
	public static final String[] EMPTY_BREAKING_RULE_RESULT = new String[] { 
		"a", 
		" ", 
		"b", 
		"c"
		};

	public static final String EMPTY_BREAKING_RULE_LANGUAGE = "";
	
	public static final SrxDocument EMPTY_BREAKING_RULE_DOCUMENT = 
		createEmptyBreakingRuleDocument();
	
	public static SrxDocument createEmptyBreakingRuleDocument() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, "", ""));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}

	/**
	 * Tests if when there is empty (matching all) breaking rule text will
	 * be split after every character.
	 */
	@Test
	public void testEmptyBreakingRule() {
		performTest(EMPTY_BREAKING_RULE_RESULT, 
				EMPTY_BREAKING_RULE_DOCUMENT, EMPTY_BREAKING_RULE_LANGUAGE);
	}
	
	public static final String[] WORD_BOUNDARY_RESULT = new String[] { 
		    "Don't split strings like U.S.A. please.",
		};

	public static final String WORD_BOUNDARY_LANGUAGE = "en";
	
	public static final SrxDocument WORD_BOUNDARY_DOCUMENT = 
		createWordBoundaryDocument();

	public static SrxDocument createWordBoundaryDocument() {
		LanguageRule languageRule = new LanguageRule("");
		
		languageRule.addRule(new Rule(false, "\\b\\p{L}\\.", ""));
		languageRule.addRule(new Rule(true, "\\.", ""));

		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Test if matchers match \b word boundary pattern correctly.
	 */
	@Test
	public void testWordBoundary() {
		performTest(WORD_BOUNDARY_RESULT, 
				WORD_BOUNDARY_DOCUMENT, WORD_BOUNDARY_LANGUAGE);
	}
	
	public static final String[] TICKET_1_RESULT = new String[] { 
		"This is a sentence. "
		};

	public static final String TICKET_1_LANGUAGE = "";
	
	public static final SrxDocument TICKET_1_DOCUMENT = 
		createTicket1Document();
	
	public static SrxDocument createTicket1Document() {
		LanguageRule languageRule = new LanguageRule("");
		
		languageRule.addRule(new Rule(false, "[A-Z]\\.\\s", ""));
		languageRule.addRule(new Rule(true, "\\.\\s", ""));

		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}
	
	/**
	 * Tests situation from Ticket 1 reported by Marcin Miłkowski.
	 */
	@Test
	public void testTicket1Rule() {
		performTest(TICKET_1_RESULT, 
				TICKET_1_DOCUMENT, TICKET_1_LANGUAGE);
	}
	
	public static final String[][] LANGUAGETOOL_RESULTS = new String[][] { 
		new String[] { "Here's a" },
		new String[] { "Here's a sentence. ", "And here's one that's not comp" },
		new String[] { "This is a sentence. " },
		new String[] { "This is a sentence. ", "And this is another one." },
		new String[] { "This is a sentence.", "Isn't it?", "Yes, it is." },
		new String[] { "This is e.g. Mr. Smith, who talks slowly...",
        "But this is another sentence." },
        new String[] { "Chanel no. 5 is blah." },
        new String[] { "Mrs. Jones gave Peter $4.5, to buy Chanel No 5.",
        "He never came back." },
        new String[] { "On p. 6 there's nothing. ", "Another sentence." },
        new String[] { "Leave me alone!, he yelled. ", "Another sentence." },
        new String[] { "\"Leave me alone!\", he yelled." },
        new String[] { "'Leave me alone!', he yelled. ", "Another sentence." },
	    new String[] { "'Leave me alone!,' he yelled. ", "Another sentence." },
	    new String[] { "This works on the phrase level, i.e. not on the word level." },
	    new String[] { "Let's meet at 5 p.m. in the main street." },
	    new String[] { "James comes from the U.K. where he worked as a programmer." },
	    new String[] { "Don't split strings like U.S.A. please." },
	    new String[] { "Don't split strings like U. S. A. either." },
	    new String[] { "Don't split... ", "Well you know. ", "Here comes more text." },
	    new String[] { "Don't split... well you know. ", "Here comes more text." },
	    new String[] { "The \".\" should not be a delimiter in quotes." },
	    new String[] { "\"Here he comes!\" she said." },
	    new String[] { "\"Here he comes!\", she said." },
	    new String[] { "\"Here he comes.\" ", "But this is another sentence." },
	    new String[] { "\"Here he comes!\". ", "That's what he said." },
	    new String[] { "The sentence ends here. ", "(Another sentence.)" },
	    new String[] { "The sentence (...) ends here." },
	    new String[] { "The sentence [...] ends here." },
	    new String[] { "The sentence ends here (...). ", "Another sentence." },
	    new String[] { "He won't. ", "Really."},
	    new String[] { "He will not. ", "Really."},
	    new String[] { "He won't go. ", "Really." },
	    new String[] { "He won't say no.", "Not really." },
	    new String[] { "He won't say No.", "Not really." },
	    new String[] { "He won't say no. 5 is better. ", "Not really." },
	    new String[] { "He won't say No. 5 is better. ", "Not really." },
	    new String[] { "They met at 5 p.m. on Thursday." },
	    new String[] { "They met at 5 p.m. ", "It was Thursday." },
	    new String[] { "This is it: a test." },
	    new String[] { "James is from the Ireland!", "He lives in Spain now." },
	    new String[] { "Jones Bros. have built a succesful company." },
	    new String[] { "It (really!) works." },
	    new String[] { "It [really!] works." },
	    new String[] { "It works (really!). ", "No doubt." },
	    new String[] { "It works [really!]. ", "No doubt." },
	    new String[] { "It really(!) works well." },
	    new String[] { "It really[!] works well." }
	};

	public static final String LANGUAGETOOL_LANGUAGE = "en";

	public static final String LANGUAGETOOL_DOCUMENT_NAME = 
		"net/sourceforge/segment/res/test/languagetool.srx";

	public static final SrxDocument LANGUAGETOOL_DOCUMENT = 
		createLanguageToolDocument();

	public static SrxDocument createLanguageToolDocument() {
		Reader reader = getReader(getResourceStream(LANGUAGETOOL_DOCUMENT_NAME));

		SrxParser parser = new Srx2Parser();
		SrxDocument document = parser.parse(reader);

		return document;
	}

	@Test
	public void testLanguageTool() {
		for (int i = 0; i < LANGUAGETOOL_RESULTS.length; ++i) {
			String[] result = LANGUAGETOOL_RESULTS[i];
			performTest("" + i, result, 
					LANGUAGETOOL_DOCUMENT, LANGUAGETOOL_LANGUAGE);
			
		}
	}

	protected abstract List<TextIterator> getTextIteratorList(String text, 
			SrxDocument document, String languageCode);


	private void performTest(String[] expectedResult, 
			SrxDocument document, String languageCode) {
		performTest("", expectedResult, document, languageCode);
	}

	private void performTest(String message, String[] expectedResult, 
			SrxDocument document, String languageCode) {
		
		String text = merge(expectedResult);
		
		List<TextIterator> textIteratorList = 
			getTextIteratorList(text, document, languageCode);
		
		for (TextIterator textIterator : textIteratorList) {

			List<String> segmentList = new ArrayList<String>();
			while (textIterator.hasNext()) {
				segmentList.add(textIterator.next());
			}

			assertListEquals(message, expectedResult, segmentList);
		}
	}

	private String merge(String[] stringArray) {
		StringBuilder builder = new StringBuilder();
		for (String string : stringArray) {
			builder.append(string);
		}
		return builder.toString();
	}
	
}
