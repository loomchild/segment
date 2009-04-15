package net.sourceforge.segment;

import java.util.Iterator;

/**
 * Text iterator interface.
 * 
 * @author loomchild
 */
public interface TextIterator extends Iterator<String> {

	/**
	 * @return Returns next segment in text, or null if end of text has been
	 *         reached.
	 */
	public String next();

	/**
	 * @return Returns true if there are more segments.
	 */
	public boolean hasNext();

	/**
	 * Unsupported operation.
	 * 
	 * @throws UnsupportedOperationException.
	 */
	public void remove();

}
