package net.sourceforge.segment.srx.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

public class ReaderCharSequenceTest {

	private static final String DATA = "abcde";

	private ReaderCharSequence sequence;

	@Before
	public void setUp() {
		StringReader reader = new StringReader(DATA);
		this.sequence = new ReaderCharSequence(reader, DATA.length(), 3);
	}

	@Test
	public void testWorking() {
		assertEquals(5, sequence.length());
		assertEquals('a', sequence.charAt(0));
		assertEquals('b', sequence.charAt(1));
		assertEquals('c', sequence.charAt(2));
		assertEquals('d', sequence.charAt(3));
		assertEquals("cde", sequence.subSequence(2, 5).toString());
		assertEquals('e', sequence.charAt(4));
		assertEquals("de", sequence.subSequence(3, 5).toString());
		assertEquals('d', sequence.charAt(3));
	}

	public void testZeroLengthSubsequence() {
		assertEquals(0, sequence.subSequence(1, 1).length());
	}

	@Test(expected = IllegalStateException.class)
	public void testSubsequenceLongerThanBuffer() {
		assertEquals("abcde", sequence.subSequence(0, 5).toString());
	}

	@Test(expected = IllegalStateException.class)
	public void testReadBack() {
		sequence.charAt(4);
		sequence.charAt(0);
	}

	@Test(expected = IllegalStateException.class)
	public void testWindowTooNarrow() {
		sequence.subSequence(0, 5);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testIndexPastEnd() {
		sequence.charAt(5);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testIndexNegative() {
		sequence.charAt(-1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testEndBeforeStart() {
		sequence.subSequence(2, 1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testStartNegative() {
		sequence.subSequence(-1, 1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testEndPastEnd() {
		sequence.subSequence(1, 6);
	}

	@Test
	public void testInfiniteStream() {
		StringReader reader = new StringReader(DATA);
		ReaderCharSequence infiniteSequence = 
			new ReaderCharSequence(reader, Integer.MAX_VALUE, 3, 2);
		assertEquals('c', infiniteSequence.charAt(2));
		assertEquals('e', infiniteSequence.charAt(4));
		assertEquals(5, infiniteSequence.length());
		try {
			infiniteSequence.charAt(5);
			fail();
		} catch (IndexOutOfBoundsException e) {
			
		}
	}

	@Test
	public void testAllSubsequence() {
		StringReader reader = new StringReader(DATA);
		ReaderCharSequence infiniteSequence = 
			new ReaderCharSequence(reader, Integer.MAX_VALUE, 5, 2);
		CharSequence subsequence = 
			infiniteSequence.subSequence(0, infiniteSequence.length()); 
		assertEquals(DATA, subsequence.toString());
	}

	@Test
	public void testIterate() {
		StringReader reader = new StringReader(DATA);
		ReaderCharSequence infiniteSequence = 
			new ReaderCharSequence(reader, Integer.MAX_VALUE, 3);
		for (int i = 0; i < infiniteSequence.length(); ++i) {
			assertEquals(DATA.charAt(i), infiniteSequence.charAt(i));
		}
	}

}
