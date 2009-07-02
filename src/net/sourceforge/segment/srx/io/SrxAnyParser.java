package net.sourceforge.segment.srx.io;

import java.io.BufferedReader;
import java.io.Reader;

import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.util.XmlException;

/**
 * Represents any version intelligent SRX document parser. Responsible for
 * creating appropriate SRX parser to given SRX document version.
 * 
 * @author loomchild
 */
public class SrxAnyParser implements SrxParser {

	/**
	 * Parses SRX document from reader. Selects appropriate SRX parser for
	 * document version.
	 * 
	 * @param reader
	 * @return Return initialized document
	 */
	public SrxDocument parse(Reader reader) {
		SrxParser parser;
		BufferedReader bufferedReader = new BufferedReader(reader);

		SrxVersion version = SrxVersion.parse(bufferedReader);
		if (version == SrxVersion.VERSION_1_0) {
			parser = new Srx1Parser();
		} else if (version == SrxVersion.VERSION_2_0) {
			parser = new Srx2Parser();
		} else {
			throw new XmlException("Unsupported SRX version: \"" + version
					+ "\".");
		}

		return parser.parse(bufferedReader);
	}

}
