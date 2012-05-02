package net.sourceforge.segment.srx.io;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import net.sourceforge.segment.srx.SrxTransformer;
import net.sourceforge.segment.util.XmlException;

/**
 * Represents any version intelligent SRX document transformer to newest
 * supported version.
 * 
 * @author loomchild
 * @see SrxVersion
 */

public class SrxAnyTransformer implements SrxTransformer {

	/**
	 * Transform given SRX document to newest supported version and write it to
	 * given writer. Recognizes version by using
	 * {@link SrxVersion#parse(BufferedReader)}, which does not always work
	 * perfectly.
	 * 
	 * @param reader reader containing SRX document
	 * @param writer writer to write transformed SRX document
	 * @param parameterMap map containing transformation parameters
	 */
	public void transform(Reader reader, Writer writer,
			Map<String, Object> parameterMap) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		SrxTransformer transformer = getTransformer(bufferedReader);
		transformer.transform(bufferedReader, writer, parameterMap);
	}

	/**
	 * Transform given SRX document and return Reader containing newest
	 * supported version. Recognizes version by using
	 * {@link SrxVersion#parse(BufferedReader)}, which does not always work
	 * perfectly.
	 * 
	 * @param reader reader containing SRX document
	 * @param parameterMap map containing transformation parameters
	 * @return reader containing SRX document in newest supported version
	 */
	public Reader transform(Reader reader, Map<String, Object> parameterMap) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		SrxTransformer transformer = getTransformer(bufferedReader);
		return transformer.transform(bufferedReader, parameterMap);
	}

	private SrxTransformer getTransformer(BufferedReader reader) {
		SrxTransformer transformer;

		SrxVersion version = SrxVersion.parse(reader);
		if (version == SrxVersion.VERSION_1_0) {
			transformer = new Srx1Transformer();
		} else if (version == SrxVersion.VERSION_2_0) {
			transformer = new Srx2Transformer();
		} else {
			throw new XmlException("Unsupported SRX version: \"" + version
					+ "\".");
		}

		return transformer;
	}

}
