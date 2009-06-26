package net.sourceforge.segment.srx.io;

import static net.sourceforge.segment.util.Util.getReader;
import static net.sourceforge.segment.util.Util.getResourceStream;

import java.io.BufferedReader;

import junit.framework.TestCase;

public class SrxVersionTest extends TestCase {

	public static final String SRX_1_DOCUMENT_NAME = "net/sourceforge/segment/res/test/example1.srx";
	public static final String SRX_2_DOCUMENT_NAME = "net/sourceforge/segment/res/test/example2.srx";
	public static final String NO_SRX_DOCUMENT_NAME = "net/sourceforge/segment/res/test/some.xml";
	public static final String SRX_NOVERSION_DOCUMENT_NAME = "net/sourceforge/segment/res/test/invalid.srx";

	public void testGetSrxVersion() {
		BufferedReader reader = new BufferedReader(
				getReader(getResourceStream(SRX_1_DOCUMENT_NAME)));
		SrxVersion version = SrxVersion.parse(reader);
		assertEquals(SrxVersion.VERSION_1_0, version);

		reader = new BufferedReader(
				getReader(getResourceStream(SRX_2_DOCUMENT_NAME)));
		version = SrxVersion.parse(reader);
		assertEquals(SrxVersion.VERSION_2_0, version);

		try {
			reader = new BufferedReader(
					getReader(getResourceStream(NO_SRX_DOCUMENT_NAME)));
			SrxVersion.parse(reader);
			fail("Recognized version of non SRX document.");
		} catch (IllegalArgumentException e) {
			// OK
		}

		try {
			reader = new BufferedReader(
					getReader(getResourceStream(SRX_NOVERSION_DOCUMENT_NAME)));
			SrxVersion.parse(reader);
			fail("Recognized version of SRX document without version.");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

}
