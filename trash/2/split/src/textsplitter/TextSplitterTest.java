package textsplitter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

public class TextSplitterTest extends TestCase {

	public static final String DOCUMENT_NAME = "data/test/polsplit.srx";
	
	public static final String SEGMENT1 = "Ach i och.El.";
	public static final String SEGMENT2 = " I nic 81.05.10 A ja leże";
	public static final String SEGMENT3 = "\n i znowu";

	public static final String TEXT = SEGMENT1 + SEGMENT2 + SEGMENT3;
	
	public void testSimpleSplit() throws IOException {
		SimpleTextSplitter splitter = new SimpleTextSplitter();
		performDoubleTest(splitter);
	}
	
	public void testSrxSplit() throws IOException {
		SrxTextSplitter splitter = null;
		try {
			splitter = new SrxTextSplitter(DOCUMENT_NAME);
		} catch (Exception e) {
			fail("Error parsing srx document: " + e.getMessage());
		}
		performDoubleTest(splitter);
	}
	
	private void performDoubleTest(TextSplitter splitter) throws IOException {
		Reader reader = createReader();
		splitter.initialize(reader, "");
		checkSplitter(splitter);
		//Test reinitialize
		reader = createReader();
		splitter.initialize(reader, "");
		checkSplitter(splitter);
	}
	
	private Reader createReader() {
		StringReader reader = new StringReader(TEXT);
		return reader;
	}
	
	private void checkSplitter(TextSplitter splitter) throws IOException {
		String segment1 = splitter.nextString();
		assertEquals(SEGMENT1, segment1);
		String segment2 = splitter.nextString();
		assertEquals(SEGMENT2, segment2);
		String segment3 = splitter.nextString();
		assertEquals(SEGMENT3, segment3);
		assertFalse(splitter.hasMoreStrings());
		assertTrue(splitter.eofOccured());
	}
	
}
