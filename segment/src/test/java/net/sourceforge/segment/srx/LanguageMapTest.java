package net.sourceforge.segment.srx;

import junit.framework.TestCase;

public class LanguageMapTest extends TestCase {

	public void testMatches() {
		LanguageMap map = new LanguageMap("PL.*", null);
		assertTrue(map.matches("PL_pl"));
		assertFalse(map.matches("EN_us"));
	}

}
