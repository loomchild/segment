package net.sourceforge.segment.util;

import junit.framework.TestCase;

public class UtilTest extends TestCase {

	public static final String QUOTED_PATTERN = "\\Q\\a\\Qaa\\\\Ebb\\Q\\E\\Qcc\\Edd";
	public static final String EXPECTED_UNQUOTED_PATTERN = "\\\\\\a\\\\\\Q\\a\\a\\\\bb\\c\\cdd";

	public void testRemoveBlockQuotes() {
		String unqotedPattern = Util.removeBlockQuotes(QUOTED_PATTERN);
		assertEquals(EXPECTED_UNQUOTED_PATTERN, unqotedPattern);
	}

	public static final String INFINITE_PATTERN = "a*b\\*\\\\+c+d\\+\\\\\\\\*e++f{1,4}+g{3,}+h{1}+\\Qa+\\E";
	public static final String EXPECTED_FINITE_PATTERN = "a{0,100}b\\*\\\\{1,100}c{1,100}d\\+\\\\\\\\{0,100}e{1,100}+f{1,4}+g{3,100}+h{1}+\\a\\+";

	public void testFinitize() {
		String finitePattern = Util.finitize(INFINITE_PATTERN, 100);
		assertEquals(EXPECTED_FINITE_PATTERN, finitePattern);
	}

	public static final String CAPTURING_GROUPS_PATTERN = "(aa)\\(bb\\\\(cc(dd))ee(?:ff)\\Q()\\E";
	public static final String EXPECTED_NONCAPTURING_GROUPS_PATTERN = "(?:aa)\\(bb\\\\(?:cc(?:dd))ee(?:ff)\\(\\)";

	public void testRemoveCapturingGroups() {
		String noncapturingGroupsPattern = 
				Util.removeCapturingGroups(CAPTURING_GROUPS_PATTERN);
		assertEquals(EXPECTED_NONCAPTURING_GROUPS_PATTERN,
				noncapturingGroupsPattern);
	}
	
}
