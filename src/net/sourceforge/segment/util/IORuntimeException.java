package net.sourceforge.segment.util;

import java.io.IOException;

/**
 * Represents runtime version of {@link IOException}. 
 * Used to avoid declaring thrown exceptions. 
 * @author loomchild
 */
public class IORuntimeException extends RuntimeException {

	private static final long serialVersionUID = -6587044052300876023L;

	public IORuntimeException(IOException exception) {
		super(exception);
	}

	public void rethrow() throws IOException {
		throw (IOException) getCause();
	}

}
