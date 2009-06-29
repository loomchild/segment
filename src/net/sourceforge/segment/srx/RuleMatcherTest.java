package net.sourceforge.segment.srx;

import junit.framework.TestCase;

public class RuleMatcherTest extends TestCase {

	public void testFind() {
		SrxDocument document = new SrxDocument();
		Rule rule = new Rule(true, "ab+", "ca+");
		String text = "abaabbcabcabcaa";
		RuleMatcher matcher = new RuleMatcher(document, rule, text);
		assertFalse(matcher.hitEnd());
		assertTrue(matcher.find());
		assertFalse(matcher.hitEnd());
		assertEquals(3, matcher.getStartPosition());
		assertEquals(6, matcher.getBreakPosition());
		assertEquals(8, matcher.getEndPosition());
		assertTrue(matcher.find());
		assertFalse(matcher.hitEnd());
		assertEquals(7, matcher.getStartPosition());
		assertEquals(9, matcher.getBreakPosition());
		assertEquals(11, matcher.getEndPosition());
		assertTrue(matcher.find());
		assertFalse(matcher.hitEnd());
		assertEquals(10, matcher.getStartPosition());
		assertEquals(12, matcher.getBreakPosition());
		assertEquals(15, matcher.getEndPosition());
		assertFalse(matcher.find());
		assertTrue(matcher.hitEnd());
		assertTrue(matcher.find(6));
		assertEquals(7, matcher.getStartPosition());
		assertEquals(9, matcher.getBreakPosition());
		assertEquals(11, matcher.getEndPosition());

	}
	
}
