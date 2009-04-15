package net.sourceforge.segment;


/**
 * Represents abstract text iterator. Responsible for implementing remove
 * operation.
 * 
 * @author loomchild
 * 
 */
public abstract class AbstractTextIterator implements TextIterator {

	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				"Remove is not supported by TextIterator.");
	}

}
