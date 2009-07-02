package net.sourceforge.segment.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is used to ignore XML DTD entities. 
 * Without it XML parsing will fail if DTD could not be found in specified location.
 * @author loomchild
 */
public class IgnoreDTDEntityResolver implements EntityResolver {

	public IgnoreDTDEntityResolver() {
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		return new InputSource(new ByteArrayInputStream(new byte[0]));
	}

}
