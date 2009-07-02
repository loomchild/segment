package net.sourceforge.segment.srx;

import java.io.IOException;
import java.io.Reader;

import net.sourceforge.segment.util.IORuntimeException;

/** 
 * Represents text manager.
 * Responsible for managing current text, reading more text from the reader
 * and checking if there is more text left.
 * @author loomchild
 */
public class TextManager {
	
	private CharSequence text;
	
	int nextCharacter;
	
	private Reader reader;
	
	private int bufferSize;
	
	/** 
	 * Creates text manager containing given text. Reading more text is not 
	 * possible when using this constructor.
	 * @param text
	 */
	public TextManager(CharSequence text) {
		this.text = text;
		this.nextCharacter = -1;
		this.reader = null;
		this.bufferSize = text.length();
	}

	/**
	 * Creates text manager reading text from given reader. Only specified
	 * amount of memory for buffer will be used. Managed text will never 
	 * be longer than given buffer size. 
	 * Text is not actually read until required (lazy initialization). 
	 * @param reader
	 * @param bufferSize read buffer size
	 */
	public TextManager(Reader reader, int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size: " + bufferSize + 
					" must be positive.");
		}
		
		this.text = null;
		this.reader = reader;
		this.bufferSize = bufferSize;
	}

	public int getBufferSize() {
		return bufferSize;
	}
	
	/**
	 * @return current text
	 */
	public CharSequence getText() {
		initText();
		return text;
	}
	
	/**
	 * @return true if more text can be read
	 */
	public boolean hasMoreText() {
		initText();
		return nextCharacter != -1;
	}
	
	/**
	 * Deletes given amount of characters from current character buffer and 
	 * tries to read up to given amount of new characters and stores them in
	 * current character buffer.
	 * @param amount amount of characters to read
	 * @throws IllegalArgumentException  if {@link #hasMoreText()} returns false or amount is greater than buffer size
	 */
	public void readText(int amount) {
		
		initText();
		
		if (amount <= 0) {
			throw new IllegalArgumentException("Amount must be positive.");
		}
		if (amount > bufferSize) {
			throw new IllegalArgumentException("Amount to read is larger than buffer size.");
		}
		if (!hasMoreText()) {
			throw new IllegalStateException("No more text to read.");
		}
		
		StringBuilder builder = new StringBuilder();
		
		// Text length is equal to buffer size so it is safe.
		builder.append(text.subSequence(amount, text.length()));
		
		// Next character cannot be null here, so it is safe.
		builder.append((char)nextCharacter);
		
		builder.append(read(amount));
		
		text = builder.toString();

	}
	
	/**
	 * Reads initial text from reader if it has not been initialized yet.
	 */
	private void initText() {
		if (text == null) {
			text = read(bufferSize + 1);
		}
	}
	
	/**
	 * Reads the given amount of characters and returns them as a string. 
	 * Updates {@link #nextCharacter} by reading one additional character.
	 * @param amount amount to be read
	 * @return read characters as a string
	 */
	private String read(int amount) {
		try {
			
			char[] charBuffer = new char[amount];
			int count = reader.read(charBuffer);
			
			String result;
			if (count == amount) {
				result = new String(charBuffer, 0, count - 1);
				nextCharacter = charBuffer[count - 1];
			} else if (count > 0 && count < amount) {
				result = new String(charBuffer, 0, count);
				nextCharacter = -1;
			} else {
				result = "";
				nextCharacter = -1;
			}

			return result;

		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
	
}
