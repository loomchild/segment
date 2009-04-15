package split.simple;

import java.io.IOException;
import java.io.Reader;

import net.rootnode.loomchild.util.exceptions.IORuntimeException;
import net.rootnode.loomchild.util.exceptions.ImpossibleException;
import split.AbstractTextIterator;

/**
 * Represents simple text iterator which splits after full-stop and newline
 * character if next letter is uppercase preceded by space.
 * 
 * @author loomchild
 */
public class SimpleTextIterator extends AbstractTextIterator {

	private Reader reader;

	private State state;

	private StringBuilder builder;

	private String segment;

	public static final char[] BREAK_CHARACTER_LIST = { '.', '!', '?' };

	public enum State {
		READY, AFTER_BREAK, AFTER_SPACE, END
	};

	/**
	 * Creates iterator.
	 * 
	 * @param reader
	 *            Input stream.
	 */
	public SimpleTextIterator(Reader reader) {
		this.reader = reader;
		this.state = State.READY;
		this.builder = new StringBuilder();
		this.segment = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String next() {
		if (hasNext()) {
			readSegment();
			String newSegment = segment;
			segment = null;
			return newSegment;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return (state != State.END) || (segment != null);
	}

	/**
	 * Reads next segment and stores it in segment variable.
	 * 
	 * @throws IORuntimeException
	 *             When there's exception reading input stream.
	 */
	private void readSegment() {
		try {
			if (segment != null) {
				return;
			}
			int leftCharacters = 0;
			while (state != State.END) {
				int readResult = reader.read();
				if (readResult == -1) {
					state = State.END;
					leftCharacters = 0;
				} else {
					char character = (char) readResult;
					builder.append(character);
					if (character == '\n') {
						state = State.READY;
						leftCharacters = 0;
						break;
					} else if (state == State.READY) {
						if (isBreakCharacter(character)) {
							state = State.AFTER_BREAK;
						}
					} else if (state == State.AFTER_BREAK) {
						if (Character.isWhitespace(character)) {
							++leftCharacters;
							state = State.AFTER_SPACE;
						} else if (isBreakCharacter(character)) {
							// Nic nie rób
						} else {
							state = State.READY;
							leftCharacters = 0;
						}
					} else if (state == State.AFTER_SPACE) {
						if (Character.isUpperCase(character)) {
							state = State.READY;
							++leftCharacters;
							break;
						} else if (isBreakCharacter(character)) {
							state = State.AFTER_BREAK;
							++leftCharacters;
							break;
						} else if (Character.isWhitespace(character)) {
							++leftCharacters;
						} else {
							state = State.READY;
							leftCharacters = 0;
						}
					} else {
						throw new ImpossibleException("Impossible state.");
					}
				}
			}
			segment = separateString(leftCharacters);
			return;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Retrieves segment from builder and Leave given amount of characters in
	 * it.
	 * 
	 * @param leftCharacters
	 *            Count of characters to leave in builder.
	 * @return Returns retrieved segment.
	 */
	private String separateString(int leftCharacters) {
		String result = builder.substring(0, builder.length() - leftCharacters);
		builder.delete(0, builder.length() - leftCharacters);
		return result;
	}

	/**
	 * @param character
	 * @return Returns true if character is breaking.
	 */
	private boolean isBreakCharacter(char character) {
		for (char ch : BREAK_CHARACTER_LIST) {
			if (ch == character) {
				return true;
			}
		}
		return false;
	}

}
