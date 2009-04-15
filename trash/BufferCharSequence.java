package split.srx;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Queue;

import loomchild.util.exceptions.IORuntimeException;

public class BufferCharSequence implements CharSequence {

	public static final int INITIAL_BUFFER_SIZE = 1024;
	
	private Reader reader;
	
	private Queue<Character> buffer;
	
	private int position;
	
	private int length;
	
	public BufferCharSequence(Reader reader) {
		this.reader = reader;
		this.buffer = new ArrayDeque<Character>(INITIAL_BUFFER_SIZE);
		this.position = 0;
		calculateLength();
	}
	
	@Override
	public char charAt(int index) {
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void calculateLength() {
		try {
			this.length = (int)reader.skip(Long.MAX_VALUE);
			reader.reset();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

}
