package net.sourceforge.segment.srx;

import static net.rootnode.loomchild.util.testing.Utils.assertListEquals;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.segment.TextIterator;

import junit.framework.TestCase;

public class SrxTextIteratorTest extends TestCase {

	/**
	 * Test some simple splitting with multiple rules.
	 */
	public void testSimpleSplit() {
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

		String text = "Ala ma kota. Prof. Kot nie wie kim jest. Ech.\n"
				+ "A inny prof. to już w ogole. Uch";

		String[] segmentArray = new String[] { "Ala ma kota.",
				" Prof. Kot nie wie kim jest.", " Ech.",
				"\nA inny prof. to już w ogole.", " Uch" };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "pl", reader);
		List<String> segmentList = split(textIterator);
		assertListEquals(segmentArray, segmentList);

		textIterator = new SrxTextIterator(document, "pl", text);
		segmentList = split(textIterator);
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if splitter can deal with alternatives in patterns.
	 */
	public void testAlternativeRuleSplit() {
		LanguageRule languageRule = new LanguageRule("Deafult");
		languageRule.addRule(new Rule(false, "(n\\.)|(e\\.)|(dn\\.)", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "W 59 n. e. Julek nie zrobił nic ciekawego. "
				+ "Drugie dn. to: Ja też nie";
		String[] segmentArray = new String[] {
				"W 59 n. e. Julek nie zrobił nic ciekawego.",
				" Drugie dn. to: Ja też nie" };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Checks if splitter works with rules with common prefix.
	 */
	public void testOverlappingRulesSplit() throws IOException {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "n\\.", " "));
		languageRule.addRule(new Rule(false, "n\\. e\\.", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "W 59 n. e. Julek nie zrobił nic ciekawego. Ja też nie";
		String[] segmentArray = new String[] {
				"W 59 n. e. Julek nie zrobił nic ciekawego.", " Ja też nie" };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests splitting when braking and non-breaking rules are interlaced.
	 */
	public void testInterlacedRulesSplit() throws IOException {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "a[\\.\\?]", " "));
		languageRule.addRule(new Rule(true, "\\.", " "));
		languageRule.addRule(new Rule(false, "(b[\\.\\?])", " "));
		languageRule.addRule(new Rule(true, "\\?", " "));
		languageRule.addRule(new Rule(false, "c[\\.\\?]", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "a? b? a. b. c. d.";
		String[] segmentArray = new String[] { "a? b? a. b.", " c.", " d." };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if when there is no breaking rules text will not be splitted.
	 */
	public void testNoBreakingRules() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "a", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "abcab";
		String[] segmentArray = new String[] { "abcab" };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if splitter can work with infinite length non breaking rule.
	 */
	public void testInfiniteNegativeRule() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "[0-9]+\\.", "\\s"));
		languageRule.addRule(new Rule(true, "\\.", "\\s"));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "Abc 99. Def. Xyz.";
		String[] segmentArray = new String[] { "Abc 99. Def.", " Xyz." };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if splitter can when there are only breaking rules.
	 */
	public void testOnlyBreakingRules() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, "\\.", "\\s"));
		languageRule.addRule(new Rule(true, "", "\\n"));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "Abc 99. Def. Xyz.";
		String[] segmentArray = new String[] { "Abc 99.", " Def.", " Xyz." };

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if when break is at the end of text only one segment is returned.
	 */
	public void testBreakAtTheEndOfText() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, "\\.", ""));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "a.";
		String[] segmentArray = new String[] {"a."};

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if when there is empty (matching all) non breaking rule text 
	 * will not be split.
	 */
	public void testEmptyNonBreakingRule() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(false, "", ""));
		languageRule.addRule(new Rule(true, "\\.", " "));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "a. b. c";
		String[] segmentArray = new String[] {"a. b. c"};

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}

	/**
	 * Tests if when there is empty (matching all) breaking rule text will
	 * be split after every character.
	 */
	public void testEmptyBreakingRule() {
		LanguageRule languageRule = new LanguageRule("");
		languageRule.addRule(new Rule(true, "", ""));
		SrxDocument document = new SrxDocument();
		document.addLanguageMap(".*", languageRule);

		String text = "a bc";
		String[] segmentArray = new String[] {"a", " ", "b", "c"};

		StringReader reader = new StringReader(text);
		TextIterator textIterator = new SrxTextIterator(document, "", reader);
		List<String> segmentList = split(textIterator);
		
		assertListEquals(segmentArray, segmentList);
	}
	
	private List<String> split(TextIterator iterator) {
		List<String> segmentList = new ArrayList<String>();
		while (iterator.hasNext()) {
			segmentList.add(iterator.next());
		}
		return segmentList;
	}

}
