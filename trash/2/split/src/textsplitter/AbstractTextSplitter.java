package textsplitter;

import java.io.IOException;

import split.splitter.Splitter;

/**
 * Klasa abstrakcyjna adaptująca splittera do interfejsu TextSplitter.
 *
 * @author loomchild
 */
public abstract class AbstractTextSplitter implements TextSplitter {

	public boolean eofOccured() {
		return !splitter.hasNext();
	}

	public boolean hasMoreStrings() throws IOException {
		return splitter.isReady();
	}

	public String nextString() throws IOException  {
		return splitter.next();
	}

	protected void setSplitter(Splitter splitter) {
		this.splitter = splitter;
	}
	
	private Splitter splitter;

}
