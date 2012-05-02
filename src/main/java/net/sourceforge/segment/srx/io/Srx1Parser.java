package net.sourceforge.segment.srx.io;

import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.SrxTransformer;


/**
 * Represents SRX 1.0 parser. Transforms document to SRX 2.0 using
 * {@link Srx1Transformer} and then parses it using {@link Srx2Parser}.
 * 
 * @author loomchild
 */
public class Srx1Parser implements SrxParser {

	/**
	 * Transforms document to SRX 2.0 using {@link Srx1Transformer} and default
	 * transformation parameters and parses it using {@link Srx2Parser}.
	 * 
	 * @param reader reader from which read the document
	 * @return initialized SRX document
	 */
	public SrxDocument parse(Reader reader) {
		SrxTransformer transformer = new Srx1Transformer();
		Map<String, Object> parameterMap = Collections.emptyMap();
		Reader reader2 = transformer.transform(reader, parameterMap);
		SrxParser parser2 = new Srx2Parser();
		return parser2.parse(reader2);
	}

}
