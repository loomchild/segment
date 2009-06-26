package net.sourceforge.segment.util;

/**
 * XML exception.
 * 
 * @author Jarek Lipski (loomchild)
 */
public class XmlException extends RuntimeException {

	private static final long serialVersionUID = -143693366659133245L;

	public XmlException(String message) {
		super(message);
	}

	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlException(Throwable cause) {
		super(cause);
	}

}
