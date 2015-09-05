package net.sourceforge.segment.srx.legacy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BufferTest {

	@Test
	public void testWorking() {
		Buffer charQueue = new Buffer(3);
		assertEquals(3, charQueue.getCapacity());
		assertEquals(0, charQueue.length());
		charQueue.forceEnqueue('a');
		charQueue.enqueue('b');
		assertEquals(2, charQueue.length());
		assertEquals('a', charQueue.charAt(0));
		assertEquals('b', charQueue.charAt(1));
		charQueue.enqueue('c');
		charQueue.dequeue();
		assertEquals('b', charQueue.charAt(0));
		charQueue.dequeue();
		assertEquals(1, charQueue.length());
		charQueue.enqueue('d');
		charQueue.enqueue('e');
		assertEquals(3, charQueue.length());
		assertEquals('c', charQueue.charAt(0));
		assertEquals('d', charQueue.charAt(1));
		assertEquals('e', charQueue.charAt(2));
		charQueue.dequeue();
		charQueue.forceEnqueue('f');
		charQueue.forceEnqueue('g');
		assertEquals(3, charQueue.length());
		assertEquals('e', charQueue.charAt(0));
		assertEquals('f', charQueue.charAt(1));
		assertEquals('g', charQueue.charAt(2));
	}

	@Test(expected = IllegalStateException.class)
	public void testOverflow() {
		Buffer charQueue = new Buffer(3);
		charQueue.enqueue('a');
		charQueue.enqueue('b');
		charQueue.enqueue('c');
		charQueue.enqueue('d');
	}

	@Test(expected = IllegalStateException.class)
	public void testUnderflow() {
		Buffer charQueue = new Buffer(3);
		charQueue.enqueue('a');
		charQueue.dequeue();
		charQueue.dequeue();
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testIndexOutOfBounds() {
		Buffer charQueue = new Buffer(3);
		charQueue.enqueue('a');
		charQueue.charAt(2);
	}

}
