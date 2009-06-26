package net.sourceforge.segment.util;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 318909218824445026L;

	public ResourceNotFoundException(String name) {
		super(name);
	}

	public ResourceNotFoundException(String name, Throwable cause) {
		super(name, cause);
	}

}
