package net.sourceforge.segment.srx;

import java.io.Reader;

/**
 * Represents SRX parser that can parse SRX document from reader.
 * 
 * @author loomchild
 */
public interface SrxParser {

	/**
	 * Parses SRX document.
	 * 
	 * @param reader reader from which read the document
	 * @return initialized SRX document
	 */
	public SrxDocument parse(Reader reader);

}
