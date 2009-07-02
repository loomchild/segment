package net.sourceforge.segment.srx.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.segment.util.IORuntimeException;

/**
 * Represents SRX version.
 * Responsible for retrieving SRX version from a reader without modifying it.
 * @author loomchild
 */
public enum SrxVersion {

	VERSION_1_0("1.0"), VERSION_2_0("2.0");

	private final static int HEADER_BUFFER_LENGHT = 1024;

	private final static Pattern VERSION_PATTERN = Pattern
			.compile("<srx[^>]+version=\"([^\"]+)\"");

	private String versionString;

	private SrxVersion(String versionString) {
		this.versionString = versionString;
	}

	public String toString() {
		return versionString;
	}

	public static SrxVersion parse(String versionString) {
		for (SrxVersion version : SrxVersion.values()) {
			if (version.versionString.equals(versionString)) {
				return version;
			}
		}
		throw new IllegalArgumentException("Unrecognized version: "
				+ versionString + ".");
	}

	/**
	 * Returns SRX document version. Works simply by looking in document header
	 * of length {@link #HEADER_BUFFER_LENGHT} 
	 * and trying to match version regular expression, so it is imperfect.
	 * It must be possible to mark the given reader ({@link Reader#mark(int)}).
	 * 
	 * @param reader buffered reader containing SRX document with unknown version
	 * @return version string
	 * @throws IORuntimeException if IO error occurs
	 * @throws IllegalArgumentException if reader does not support marking 
	 */
	public static SrxVersion parse(BufferedReader reader) {
		try {
			if (!reader.markSupported()) {
				throw new IllegalArgumentException("Mark not supported for reader.");
			}
			reader.mark(HEADER_BUFFER_LENGHT);
			char[] headerBuffer = new char[HEADER_BUFFER_LENGHT];
			int count = reader.read(headerBuffer);
			String header = new String(headerBuffer, 0, count);
			reader.reset();
			Matcher matcher = VERSION_PATTERN.matcher(header);
			String versionString = null;
			if (matcher.find()) {
				versionString = matcher.group(1);
			}
			return parse(versionString);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

}
