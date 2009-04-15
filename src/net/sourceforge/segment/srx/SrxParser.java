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
	 * @param reader
	 *            Reader from which read the document.
	 * @return Returns initialized SRX document.
	 */
	public SrxDocument parse(Reader reader);

}
