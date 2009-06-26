package net.sourceforge.segment.srx.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.regex.Pattern;

import org.junit.Test;

public class ReaderMatcherTest {

	private static final String REGEX_DATA = "abcde - fghijklmno pqrstuvw - x -";

	private static final Pattern REGEX = Pattern.compile(" - ");

	@Test
	public void testSimple() {
		StringReader reader = new StringReader(REGEX_DATA);
		CharSequence text = new ReaderCharSequence(reader);
		ReaderMatcher matcher = new ReaderMatcher(REGEX, text);
		boolean found;
		found = matcher.find();
		assertTrue(found);
		assertEquals(5, matcher.start());
		found = matcher.find();
		assertTrue(found);
		assertEquals(27, matcher.start());
		found = matcher.find();
		assertFalse(found);
		assertTrue(matcher.hitEnd());
	}

	private static final Pattern REGEX_WHOLE = Pattern.compile(".*");

	@Test
	public void testWhole() {
		StringReader reader = new StringReader(REGEX_DATA);
		CharSequence text = new ReaderCharSequence(reader);
		ReaderMatcher matcher = new ReaderMatcher(REGEX_WHOLE, text);
		boolean found = matcher.find();
		assertTrue(found);
		assertEquals(REGEX_DATA, matcher.group());
	}

	private static final Pattern REGEX_ALTERNATIVE = Pattern.compile("b|bc");

	@Test
	public void testAlternative() {
		StringReader reader = new StringReader(REGEX_DATA);
		CharSequence text = new ReaderCharSequence(reader);
		ReaderMatcher matcher = new ReaderMatcher(REGEX_ALTERNATIVE, text);
		boolean found = matcher.find();
		assertTrue(found);
		assertEquals(1, matcher.start());
		assertEquals(2, matcher.end());
		found = matcher.find();
		assertFalse(found);
	}

	private static final Pattern REGEX_LOOKING_AT = Pattern.compile("bcd");

	@Test
	public void testLookingAt() {
		StringReader reader = new StringReader(REGEX_DATA);
		CharSequence text = new ReaderCharSequence(reader);
		ReaderMatcher matcher = new ReaderMatcher(REGEX_LOOKING_AT, text);
		matcher.region(1, text.length());
		assertTrue(matcher.lookingAt());
		matcher.region(2, text.length());
		assertFalse(matcher.lookingAt());
	}

}
