package net.sourceforge.segment.srx.legacy;

/**
 * Represents character buffer with fixed capacity. 
 * Implements {@link CharSequence} methods and in addition queue methods.
 * @author loomchild
 */
public class Buffer implements CharSequence {

	private char[] buffer;
	private int head;
	private int size;

	private Buffer(char[] buffer, int head, int size) {
		this.buffer = buffer;
		this.head = head;
		this.size = size;
	}

	public Buffer(int capacity) {
		this(new char[capacity], 0, 0);
	}

	public int getCapacity() {
		return buffer.length;
	}

	public void enqueue(char character) {
		if (isFull()) {
			throw new IllegalStateException(
					"Not enough capacity to enqueue element");
		} else {
			buffer[(head + length()) % getCapacity()] = character;
			++size;
		}
	}

	public void dequeue() {
		if (isEmpty()) {
			throw new IllegalStateException("No element to dequeue");
		} else {
			head = (head + 1) % getCapacity();
			--size;
		}
	}

	/**
	 * This is the same as: 
	 * if (buffer.length() * == buffer.getCapacity()) buffer.dequeue(); 
	 * buffer.enqueue(character);
	 * 
	 * @param character
	 */
	public void forceEnqueue(char character) {
		buffer[(head + length()) % getCapacity()] = character;
		if (isFull()) {
			head = (head + 1) % getCapacity();
		} else {
			++size;
		}
	}

	public int length() {
		return size;
	}

	public char charAt(int index) {
		if (index < 0 || index >= length()) {
			throw new IndexOutOfBoundsException("Buffer index " + index
					+ " not in <0, " + length() + ").");
		} else {
			char character = buffer[(head + index) % getCapacity()];
			return character;
		}
	}

	public CharSequence subSequence(int start, int end) {
		if (start < 0 || start > end || end > length()) {
			throw new IndexOutOfBoundsException("Buffer subsequence " + "<"
					+ start + ", " + end + ") not in " + "<0, " + length()
					+ ").");
		} else {
			int subHead = (head + start) % getCapacity();
			int subSize = end - start;
			return new Buffer(buffer, subHead, subSize);
		}
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder(size);
		int position = head;
		for (int i = 0; i < size; ++i) {
			stringBuilder.append(buffer[position]);
			position = (position + 1) % getCapacity();
		}
		return stringBuilder.toString();
	}

	private boolean isEmpty() {
		return length() == 0;
	}

	private boolean isFull() {
		return length() == getCapacity();
	}

}
