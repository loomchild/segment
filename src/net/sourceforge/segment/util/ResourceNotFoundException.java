package net.sourceforge.segment.util;

/**
 * Exception that indicates that resource has not been found by Classloader.
 * @author loomchild
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 318909218824445026L;

	public ResourceNotFoundException(String name) {
		super(name);
	}

	public ResourceNotFoundException(String name, Throwable cause) {
		super(name, cause);
	}

}
