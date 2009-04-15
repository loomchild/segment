package textsplitter;

import java.io.IOException;

import net.rootnode.loomchild.util.exceptions.IORuntimeException;
import split.splitter.TextIterator;

/**
 * Klasa abstrakcyjna adaptująca text iterator do interfejsu TextSplitter.
 * 
 * @author loomchild
 */
public abstract class AbstractTextSplitter implements TextSplitter {

	private TextIterator textIterator;

	protected void setTextIterator(TextIterator textIterator) {
		this.textIterator = textIterator;
	}

	public boolean eofOccured() {
		return !textIterator.hasNext();
	}

	public boolean hasMoreStrings() {
		return textIterator.hasNext();
	}

	public String nextString() throws IOException {
		String nextString = null;
		try {
			nextString = textIterator.next();
		} catch (IORuntimeException e) {
			throw new IOException(e);
		}
		return nextString;
	}

}
