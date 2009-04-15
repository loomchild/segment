package textsplitter;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;

import java.io.IOException;
import java.io.Reader;

import net.rootnode.loomchild.util.exceptions.IORuntimeException;
import split.splitter.TextIterator;
import split.srx.SrxDocument;
import split.srx.SrxParser;
import split.srx.SrxTextIterator;
import split.srx.io.SrxAnyParser;

/**
 * Adapter klasy SrxSplitter.
 * 
 * @author loomchild
 */
public class SrxTextSplitter extends AbstractTextSplitter {

	private SrxDocument document;

	public SrxTextSplitter(Reader srxFileReader) throws IOException {
		try {
			SrxParser parser = new SrxAnyParser();
			this.document = parser.parse(srxFileReader);
		} catch (IORuntimeException e) {
			throw new IOException(e);
		}
	}
	
	public SrxTextSplitter(String srxFileName) throws IOException {
		this(getReader(getResourceStream(srxFileName)));
	}

	public void initialize(Reader reader, String languageCode) {
		TextIterator textIterator = new SrxTextIterator(document, languageCode,
				reader);
		setTextIterator(textIterator);
	}

}
