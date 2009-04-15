package split.simple;

import static net.rootnode.loomchild.util.lang.Util.merge;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;
import split.TextIterator;

public class SimpleTextIteratorTest extends TestCase {

	public static final String[] SEGMENT_ARRAY = new String[] {
			"Ala ma kota.X.", " A  Prof...", " Kot nie wie kim jest.",
			" . nic.", "  Ech\n", "Nic.", "\t A jednak.", " Ala" };

	public static final String TEXT = merge(SEGMENT_ARRAY);

	public void testSimpleTextIterator() throws IOException {
		Reader reader = createReader();
		TextIterator textIterator = new SimpleTextIterator(reader);
		checkTextIterator(textIterator);
	}

	private Reader createReader() {
		StringReader reader = new StringReader(TEXT);
		return reader;
	}

	private void checkTextIterator(TextIterator textIterator)
			throws IOException {
		StringBuilder actualText = new StringBuilder();
		for (String expectedSegment : SEGMENT_ARRAY) {
			assertTrue(textIterator.hasNext());
			String actualSegment = textIterator.next();
			assertEquals(expectedSegment, actualSegment);
			actualText.append(actualSegment);
		}
		assertNull(textIterator.next());
		assertFalse(textIterator.hasNext());
		// No lost characters
		assertEquals(TEXT, actualText.toString());
	}

}
