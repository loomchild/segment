package net.sourceforge.segment.util;

/**
 * Runtime version of XML exception.
 * @author loomchild
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
