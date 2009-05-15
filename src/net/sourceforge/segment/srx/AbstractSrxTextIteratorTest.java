package net.sourceforge.segment.srx;

import static net.rootnode.loomchild.util.testing.Utils.assertListEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.segment.TextIterator;

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
	
	public static final String[] NON_BREAKING_LONGER_THAN_BREAKING_RESULT = 
			new String[] { 
	    "Ala ma kota.", " "
	};

	public static final String NON_BREAKING_LONGER_THAN_BREAKING_LANGUAGE = "";

	public static final SrxDocument NON_BREAKING_LONGER_THAN_BREAKING_DOCUMENT = 
		createNonBreakingLongerThanBreakingDocument();

	public static SrxDocument createNonBreakingLongerThanBreakingDocument() {
	    LanguageRule languageRule = new LanguageRule("");
	
		languageRule.addRule(new Rule(false, "\\.", "\\sa"));
		languageRule.addRule(new Rule(true, "\\.", "\\s"));

		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}

	/**
	 * Test when non breaking rule is longer than breaking rule everything
	 * is OK (problems with lookingAt throwing EndOfStreamException).
	 */
	@Test
	public void testNonBreakingLongerThanBreaking() {
		performTest(NON_BREAKING_LONGER_THAN_BREAKING_RESULT, 
				NON_BREAKING_LONGER_THAN_BREAKING_DOCUMENT, 
				NON_BREAKING_LONGER_THAN_BREAKING_LANGUAGE);
	}
	
	public static final String[] MATCHING_END_RESULT = 
		new String[] { 
		"A.", "."
	};

	public static final String MATCHING_END_LANGUAGE = "";

	public static final SrxDocument MATCHING_END_DOCUMENT = 
		createMatchingEndDocument();

	public static SrxDocument createMatchingEndDocument() {
		LanguageRule languageRule = new LanguageRule("");

		languageRule.addRule(new Rule(true, "\\.\\.\\.", ""));
		languageRule.addRule(new Rule(true, "\\.", ""));

		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		return document;
	}

	/**
	 * Test if unfinished rule matching the end containing other rule will not
	 * supress it. Does not work in reader version of SrxTextIterator 
	 * ({@link SrxTextIteratorReaderTest}) because matching throws 
	 * IndexOutOfBoundsException before it finds the shorter rule.
 	 * In some real cases it will work anyway because of lookahead buffer 
	 * (set to 1 for tests, but in real life will be 2K).
	 */
	@Test
	public void testMatchingEnd() {
		performTest(MATCHING_END_RESULT, MATCHING_END_DOCUMENT, 
				MATCHING_END_LANGUAGE);
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

	protected abstract TextIterator getTextIterator(String text, 
			SrxDocument document, String languageCode);

	private void performTest(String[] expectedResult, 
			SrxDocument document, String languageCode) {
		
		String text = merge(expectedResult);
		
		TextIterator textIterator;
		List<String> segmentList;
		
		textIterator = getTextIterator(text, document, languageCode);
		segmentList = segment(textIterator);
		assertListEquals(expectedResult, segmentList);
	}

	private List<String> segment(TextIterator textIterator) {
		List<String> segmentList = new ArrayList<String>();
		while (textIterator.hasNext()) {
			segmentList.add(textIterator.next());
		}
		return segmentList;
	}
	
	private String merge(String[] stringArray) {
		StringBuilder builder = new StringBuilder();
		for (String string : stringArray) {
			builder.append(string);
		}
		return builder.toString();
	}
	
}
