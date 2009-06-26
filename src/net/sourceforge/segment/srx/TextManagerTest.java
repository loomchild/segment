package net.sourceforge.segment.srx;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

public class TextManagerTest {

	@Test
	public void testCharSequence() {
		TextManager manager = new TextManager("text");
		assertEquals("text", manager.getText().toString());
		assertEquals(4, manager.getBufferSize());
		assertEquals(false, manager.hasMoreText());
	}
	
	@Test
	public void testEmptyString() {
		TextManager manager = new TextManager("");
		assertEquals("", manager.getText().toString());
		assertEquals(0, manager.getBufferSize());
		assertEquals(false, manager.hasMoreText());
	}

	@Test(expected=IllegalStateException.class)
	public void testCannotReadCharSequence() {
		TextManager manager = new TextManager("text");
		manager.readText(1);
	}
	
	@Test
	public void testReader() {
		
		StringReader reader = new StringReader("text");
		TextManager manager = new TextManager(reader, 2);
		assertEquals(2, manager.getBufferSize());

		assertEquals("te", manager.getText().toString());
		assertEquals(true, manager.hasMoreText());		

		manager.readText(1);
		assertEquals("ex", manager.getText().toString());
		assertEquals(true, manager.hasMoreText());		

		manager.readText(1);
		assertEquals("xt", manager.getText().toString());
		assertEquals(false, manager.hasMoreText());		

	}
	
	@Test
	public void testEmptyReader() {
		StringReader reader = new StringReader("");
		TextManager manager = new TextManager(reader, 2);
		assertEquals("", manager.getText().toString());
		assertEquals(2, manager.getBufferSize());
		assertEquals(false, manager.hasMoreText());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBufferZeroLength() {
		StringReader reader = new StringReader("");
		new TextManager(reader, 0);
	}

}
