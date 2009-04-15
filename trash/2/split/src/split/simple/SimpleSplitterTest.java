package split.simple;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;
import split.splitter.Splitter;

public class SimpleSplitterTest extends TestCase {
	
	public static final String SEGMENT1 = "Ala ma kota.X.";
	public static final String SEGMENT2 = " Prof.";
	public static final String SEGMENT3 = " Kot nie wie kim jest.";
	public static final String SEGMENT4 = "  Ech";
	public static final String SEGMENT5 = "\nNic.";
	public static final String SEGMENT6 = "\t A jednak.";

	public static final String TEXT = 
		SEGMENT1 + SEGMENT2 + SEGMENT3 + SEGMENT4+ SEGMENT5 + SEGMENT6;
	
	public void testSimpleSplitter() throws IOException {
		Reader reader = createReader();
		Splitter splitter = new SimpleSplitter(reader);
		checkSplitter(splitter);
	}
	
	public void testIsReady() throws IOException {
		PipedWriter writer = new PipedWriter();
		PipedReader reader = new PipedReader(writer);
		Splitter splitter = new SimpleSplitter(reader);
		writer.write("Ala ma");
		assertTrue(splitter.hasNext());
		assertFalse(splitter.isReady());
		writer.write(" kota. A ja nie");
		assertTrue(splitter.hasNext());
		assertTrue(splitter.isReady());
		String segment1 = splitter.next();
		assertEquals("Ala ma kota.", segment1);
		writer.close();
		assertTrue(splitter.hasNext());
		//Głupie ale nie da sie tego dobrze zaimplementować
		assertFalse(splitter.isReady());
		String segment2 = splitter.next();
		assertEquals(" A ja nie", segment2);
		assertFalse(splitter.hasNext());
		assertFalse(splitter.isReady());
	}

	private Reader createReader() {
		StringReader reader = new StringReader(TEXT);
		return reader;
	}

	private void checkSplitter(Splitter splitter) throws IOException {
		assertTrue(splitter.hasNext());
		String segment1 = splitter.next();
		assertEquals(SEGMENT1, segment1);
		String segment2 = splitter.next();
		assertEquals(SEGMENT2, segment2);
		String segment3 = splitter.next();
		assertEquals(SEGMENT3, segment3);
		String segment4 = splitter.next();
		assertEquals(SEGMENT4, segment4);
		String segment5 = splitter.next();
		assertEquals(SEGMENT5, segment5);
		String segment6 = splitter.next();
		assertEquals(SEGMENT6, segment6);
		assertNull(splitter.next());
		assertFalse(splitter.hasNext());
		assertFalse(splitter.isReady());
		//Łańcuch podzielony ma taką samą długość jak wejściowy
		assertEquals(TEXT, segment1 + segment2 + segment3 + segment4 
				+ segment5 + segment6);
	}

}
 