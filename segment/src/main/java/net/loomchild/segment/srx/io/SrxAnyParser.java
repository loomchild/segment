package net.loomchild.segment.srx.io;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import net.loomchild.segment.srx.SrxDocument;
import net.loomchild.segment.srx.SrxParser;
import net.loomchild.segment.srx.SrxTransformer;
import net.loomchild.segment.util.XmlException;

/**
 * Represents any version intelligent SRX document parser. Responsible for
 * creating appropriate SRX parser to given SRX document version.
 * 
 * @author loomchild
 */
public class SrxAnyParser implements SrxParser {
	
	private SrxParser parser;

	/**
	 * Creates SRX any parser using given SRX 2.0 parser.
	 * @param parser
	 */
	public SrxAnyParser(SrxParser parser) {
		this.parser = parser;
	}
	
	/**
	 * Creates SRX any parser using default SRX 2.0 parser.
	 */
	public SrxAnyParser() {
		this(new Srx2Parser());
	}
	
	/**
	 * Parses SRX document from reader. Selects appropriate SRX parser for
	 * document version.
	 * 
	 * @param reader
	 * @return Return initialized document
	 */
	public SrxDocument parse(Reader reader) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		reader = bufferedReader;

		SrxVersion version = SrxVersion.parse(bufferedReader);
		if (version == SrxVersion.VERSION_1_0) {
			SrxTransformer transformer = new Srx1Transformer();
			Map<String, Object> parameterMap = Collections.emptyMap();
			reader = transformer.transform(bufferedReader, parameterMap);
		} else if (version != SrxVersion.VERSION_2_0) {
			throw new XmlException("Unsupported SRX version: \"" + version
					+ "\".");
		}

		return parser.parse(reader);
	}

}
