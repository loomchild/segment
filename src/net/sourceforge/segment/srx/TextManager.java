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
	

	public TextManager(CharSequence text) {
		this.text = text;
		this.nextCharacter = -1;
		this.reader = null;
		this.bufferSize = text.length();
	}

	public TextManager(Reader reader, int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size: " + bufferSize + 
					" must be positive.");
		}
		
		this.text = null;
		this.reader = reader;
		this.bufferSize = bufferSize;
		
		text = read(bufferSize + 1);
	}

	public int getBufferSize() {
		return bufferSize;
	}
	
	public CharSequence getText() {
		return text;
	}
	
	public boolean hasMoreText() {
		return nextCharacter != -1;
	}
	
	public void readText(int amount) {
		
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
