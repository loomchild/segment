package textsplitter;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;
import static net.rootnode.loomchild.util.lang.Util.merge;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

public class TextSplitterTest extends TestCase {

	public static final String DOCUMENT_NAME = "srx/example.srx";

	public static final String[] SEGMENT_ARRAY = new String[] {
			"Ach i och.El...", " I nic etc. a ja leże.", " I znowu", };

	public static final String TEXT = merge(SEGMENT_ARRAY);

	public void testSimpleSplit() throws IOException {
		SimpleTextSplitter splitter = new SimpleTextSplitter();
		performDoubleTest(splitter);
	}

	public void testSrxSplit() throws Exception {
		Reader reader = getReader(getResourceStream(DOCUMENT_NAME));
		SrxTextSplitter splitter = new SrxTextSplitter(reader);
		performDoubleTest(splitter);
	}

	private void performDoubleTest(TextSplitter splitter) throws IOException {
		Reader reader = createReader();
		splitter.initialize(reader, "en");
		checkSplitter(splitter);
		// Test reinitialize
		reader = createReader();
		splitter.initialize(reader, "en");
		checkSplitter(splitter);
	}

	private Reader createReader() {
		StringReader reader = new StringReader(TEXT);
		return reader;
	}

	private void checkSplitter(TextSplitter splitter) throws IOException {
		StringBuilder actualText = new StringBuilder();
		for (String expectedSegment : SEGMENT_ARRAY) {
			assertTrue(splitter.hasMoreStrings());
			String actualSegment = splitter.nextString();
			assertEquals(expectedSegment, actualSegment);
			actualText.append(actualSegment);
		}
		assertFalse(splitter.hasMoreStrings());
		assertTrue(splitter.eofOccured());
		// Łańcuch podzielony ma taką samą długość jak wejściowy
		assertEquals(TEXT, actualText.toString());
	}

}
