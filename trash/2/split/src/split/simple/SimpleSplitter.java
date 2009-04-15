package split.simple;

import java.io.IOException;
import java.io.Reader;

import loomchild.util.exceptions.ImpossibleException;
import split.splitter.Splitter;

/**
 * Implementuje proste dzielenie na zdania po kropce i końcu linii, patrząc czy
 * następna litera jest duża i czy występuje przed nią odstęp. 
 * Działa strumieniowo.
 *
 * @author loomchild
 */
public class SimpleSplitter implements Splitter {

	/**
	 * Znaki podziału.
	 */
	public static final char[] BREAK_CHARACTER_LIST = {'.', '!', '?'};

	public enum State {READY, AFTER_BREAK, END};
	
	/**
	 * Inicjalizuje splitter.
	 * @param reader Strumień wejściowy.
	 */
	public SimpleSplitter(Reader reader) {
		this.reader = reader;
		this.state = State.READY;
		this.builder = new StringBuilder();
		this.segment = null;
	}

	/**
	 * Zwraca następny segment. 
	 */
	public String next() throws IOException {
		if (hasNext()) {
			readSegment(true);
			String newSegment = segment;
			segment = null;
			return newSegment;
		} else {
			return null;
		}
	}

	/**
	 * @return Zwraca false gdy napotkano koniec strumienia.
	 */
	public boolean hasNext() {
		return state != State.END;
	}
	
	/**
	 * @return Zwraca true gdy następne wywołanie next nie zablokuje programu.
	 * @throws IOException Zgłaszany gdy nastąpi błąd buforowania następnego segmentu.
	 */
	public boolean isReady() throws IOException {
		if (hasNext()) {
			return readSegment(false);
		} else {
			return false;
		}
	}

	/**
	 * Czyta następny segment do zmiennej segment.
	 * @param canBlock Jeśli true to funkcja może zablokować czekając na znak.
	 * @return Zwraca false jeśli nie może czytać bo by zablokowało.
	 * @throws IOException Zgłaszany gdy nastąpi błąd odczytu strumienia.
	 */
	private boolean readSegment(boolean canBlock) throws IOException {
		if (segment != null) {
			return true;
		}
		int leftCharacters = 0;
		boolean found = false;
		while (!found) {
			if (!canBlock && !reader.ready()) {
				return false;
			}
			int readResult = reader.read();
			if (readResult == -1) {
				state = State.END;
				segment = separateString(0);
				found = true;
			} else {
				char character = (char)readResult;
				builder.append(character);
				if (character == '\n') {
					state = State.READY;
					segment = separateString(1);
					found = true;
				} else if (state == State.READY) {
					if (isBreakCharacter(character)) {
						state = State.AFTER_BREAK;
						leftCharacters = 1;
					}
				} else if (state == State.AFTER_BREAK) {
					if (Character.isUpperCase(character) && leftCharacters > 1) {
						segment = separateString(leftCharacters);
						found = true;
					} else if (Character.isWhitespace(character)) {
						++leftCharacters;
					} else {
						state = State.READY;
						leftCharacters = 0;
					}
				} else {
					throw new ImpossibleException(
							"Impossible SimpleSplitter state.");
				}
			}
		}
		return true;
	}

	/**
	 * Wyodrębnia segment z buildera i zostawia w nim daną ilość znaków.
	 * @param leftCharacters Ile znaków ma zostawić w builderze.
	 * @return Zwraca wydzielony napis
	 */
	private String separateString(int leftCharacters) {
		String result = builder.substring(0, builder.length() - leftCharacters);
		builder.delete(0, builder.length() - leftCharacters);
		return result;
	}

	/**
	 * @param character
	 * @return Zwraca true jeśli podany znak jest znakiem końca zdania.
	 */
	private boolean isBreakCharacter(char character) {
		for (char ch : BREAK_CHARACTER_LIST) {
			if (ch == character) {
				return true;
			}
		}
		return false;
	}
	
	private Reader reader;
	
	private State state;
	
	private StringBuilder builder;
	
	private String segment;

}
