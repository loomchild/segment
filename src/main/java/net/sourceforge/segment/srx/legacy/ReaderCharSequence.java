package net.sourceforge.segment.srx.legacy;

import java.io.IOException;
import java.io.Reader;

import net.sourceforge.segment.util.IORuntimeException;

/**
 * Adapter of reader class to CharSequence interface. Due to behavior
 * differences CharSequence is not implemented perfectly.
 * 
 * @author loomchild
 */
public class ReaderCharSequence implements CharSequence {

	public static final int DEFAULT_BUFFER_LENGTH = 64 * 1024;
	
	public static final int DEFAULT_LOOKAHEAD = 1;
	
	/**
	 * Value to be used as length when it is unknown. It must be less than
	 * {@link Integer#MAX_VALUE} because some code may add something to length.
	 */
	public static final int INFINITE_LENGTH = Integer.MAX_VALUE - 8;

	private Reader reader;
	
	private int lookahead;

	private Buffer buffer;

	private int position;

	private int length;

	/**
	 * Create.
	 * 
	 * @param reader reader from which char sequence will be read
	 * @param bufferLength size of the character buffer
	 * @param length length of the input; when it cannot be determined it can 
	 * 		be set to {@link #INFINITE_LENGTH} (max value); cannot be set to 
	 * 		{@link Integer#MAX_VALUE} because it may cause overflow.
	 * @param lookahead number of characters to read after current position
	 */
	public ReaderCharSequence(Reader reader, int bufferLength, int length, 
			int lookahead) {
		if (length > INFINITE_LENGTH) {
			throw new IllegalArgumentException("Length cannot be greater " +
					"than infinity (" + INFINITE_LENGTH + ") bacause it may " +
					"cause overflow (when matching regular expression " +
					"for example). Use " + ReaderCharSequence.class.getName() +
					".INFINITE_LENGTH constant instead.");
		}
		this.reader = reader;
		this.lookahead = lookahead;
		this.buffer = new Buffer(bufferLength);
		this.position = 0;
		this.length = length;
		fillBuffer(-1);
	}

	public ReaderCharSequence(Reader reader, int bufferLength, int length) {
		this(reader, bufferLength, length, DEFAULT_LOOKAHEAD);
	}

	public ReaderCharSequence(Reader reader, int bufferLength) {
		this(reader, bufferLength, INFINITE_LENGTH);
	}

	public ReaderCharSequence(Reader reader) {
		this(reader, DEFAULT_BUFFER_LENGTH);
	}

	public int length() {
		return length;
	}

	public char charAt(int index) {
		if (index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index " + index
					+ " not in <0, " + length + ")");
		} else if (index < getMinIndex()) {
			throw new IllegalStateException("Character lost, buffer too small.");
		} else {
			fillBuffer(index);
			if (index >= length) {
				throw new IndexOutOfBoundsException("End of stream.");
			}
			int relativeIndex = getRelativeIndex(index);
			char character = buffer.charAt(relativeIndex);
			return character;
		}
	}

	/**
	 * The length of returned subsequence can be smaller than (end - start) when
	 * the end of stream is reached.
	 */
	public CharSequence subSequence(int start, int end) {
		if (start < 0 || end < 0 || end > length || start > end) {
			throw new IndexOutOfBoundsException("Subsequence " + "<" + start
					+ ", " + end + ") not in " + "<0, " + length + ").");
		} else {
			fillBuffer(end - 1);
			if (end > length) {
				end = length;
			}
			if (start > end) {
				throw new IndexOutOfBoundsException("Subsequence " + "<" + start
					+ ", " + end + ") not in " + "<0, " + length + ").");
			}
			if (end - start > buffer.length() || start < getMinIndex()) {
				throw new IllegalStateException("Cannot retrieve subsequence "
						+ "<" + start + ", " + end + "). "
						+ "Characters lost, buffer too small.");
			}
			int relativeStart = getRelativeIndex(start);
			int relativeEnd = getRelativeIndex(end);
			CharSequence subSequence = buffer.subSequence(relativeStart,
					relativeEnd);
			return subSequence;
		}
	}

	/**
	 * <p>Calculate buffer relative index from sequence index.</p>
	 * <pre>
	 * The equation is this:
	 * relative index = index - (position - buffer length)
	 * Example:
	 * position = 10
	 * buffer length = 5
	 * index = 7
	 *                    
	 *                    |   |
	 * position - buffer  |   |  index
	 *            length  V   V
	 * 
	 * stream  |-|-|-|-|-|-|-|-|-|-|
	 *          0 1 2 3 4 5 6 7 8 9 10  <--- position
	 * 
	 *                        |
	 *                        |  relative index
	 *                        V
	 * 
	 * buffer            |-|-|-|-|-|
	 *                    0 1 2 3 4 
	 * 
	 * relative index = 2
	 * </pre>
	 * 
	 * @param index sequence index
	 * @return buffer relative index
	 */
	private int getRelativeIndex(int index) {
		return index - (position - buffer.length());
	}
	
	private int getMinIndex() {
		return position - buffer.length();		
	}

	private void fillBuffer(int index) {
		// Index can be MAX_INT so all arithmetic operations should be
		// on the left side of equation to avoid integer overflow.
		while (index >= position - lookahead && position < length) {
			readCharacter();
		}
	}

	private void readCharacter() {
		int readResult;
		try {
			readResult = reader.read();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}			
		if (readResult == -1) {
			length = position;
		} else {
			char character = (char)readResult;
			buffer.forceEnqueue(character);
			++position;
		}
	}

}
