package net.sourceforge.segment.srx.io;

import static net.sourceforge.segment.util.Util.getParameter;
import static net.sourceforge.segment.util.Util.getReader;
import static net.sourceforge.segment.util.Util.getResourceStream;
import static net.sourceforge.segment.util.Util.getSchema;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.util.Util;
import net.sourceforge.segment.util.XmlException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Represents SRX 2.0 document parser. Responsible for creating and initializing
 * Document according to given SRX. Uses SAX. Can validate.
 * 
 * @author loomchild
 */
public class Srx2SaxParser implements SrxParser {

	/**
	 * Whether parser should validate input against XML Schema or not.
	 */
	public static final String VALIDATE_PARAMETER = "validate";
	
	/**
	 * Default validate parameter value.
	 */
	public static final boolean DEFAULT_VALIDATE = true;
	
	
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(Srx2SaxParser.class);

	private static final String SCHEMA = "net/sourceforge/segment/res/xml/srx20.xsd";
	
	private SAXParserFactory factory;

	private static class SrxHandler extends DefaultHandler {
		
		private SrxDocument document;

		private String elementName;
		
		private Map<String, LanguageRule> languageRuleMap;
		
		private LanguageRule languageRule;
		
		private boolean breakRule;
		private StringBuilder beforeBreak;
		private StringBuilder afterBreak;

		
		public SrxHandler(SrxDocument document) {
			this.document = document;
		}
		
	    public void startDocument() throws SAXException {
			languageRuleMap = new HashMap<String, LanguageRule>();
			beforeBreak = new StringBuilder();
			afterBreak = new StringBuilder();
	    	resetRule();
	    }

	    public void endDocument() throws SAXException {
	    }
		
	    public void startElement(String uri, String localName, String qName, 
	    		Attributes attributes) throws SAXException {
	    	elementName = localName;
	    	
	    	if ("header".equals(localName)) {
    			document.setCascade("yes".equals(getValue(attributes, "cascade")));
	    	} else if ("languagerule".equals(localName)) {
	    		String languageRuleName = getValue(attributes, "languagerulename");
	    		languageRule = new LanguageRule(languageRuleName);
	    		languageRuleMap.put(languageRule.getName(), languageRule);
	    	} else if ("languagemap".equals(localName)) {
	    		String languagePattern = getValue(attributes, "languagepattern");
	    		String languageRuleName = getValue(attributes, "languagerulename");
	    		document.addLanguageMap(languagePattern, languageRuleMap.get(languageRuleName));
	    	} else if ("rule".equals(localName)) {
				breakRule = !"no".equals(getValue(attributes, "break"));
	    	}
	    }
	    
	    public void endElement(String uri, String localName, String qName)
	    		throws SAXException {
	    	elementName = null;
	    	
	    	if ("rule".equals(localName)) {
	    		String beforeBreakString = beforeBreak.toString();
	    		String afterBreakString = afterBreak.toString();
				Rule rule = new Rule(breakRule, beforeBreakString, afterBreakString);
				languageRule.addRule(rule);
				resetRule();
	    	}
	    }


	    public void characters(char ch[], int start, int length)
	    		throws SAXException {
	    	if ("beforebreak".equals(elementName)) {
	    		beforeBreak.append(ch, start, length);
	    	} else if ("afterbreak".equals(elementName)) {
	    		afterBreak.append(ch, start, length);
	    	}
	    }
	    
	    
	    public void fatalError(SAXParseException e) {
	    	throw new XmlException("Fatal error parsing SRX", e);
	    }

	    public void error(SAXParseException e) {
	    	throw new XmlException("Error parsing SRX", e);
	    }

	    public void warning(SAXParseException e) {
	    	throw new XmlException("Warning parsing SRX", e);
	    }

	    
	    private void resetRule() {
			breakRule = false;
    		beforeBreak.setLength(0);
    		afterBreak.setLength(0);
	    }
	    
	    private String getValue(Attributes attributes, String localName) {
	    	for (int i = 0; i < attributes.getLength(); ++i) {
	    		if (localName.equals(attributes.getLocalName(i))) {
	    			return attributes.getValue(i);
	    		}
	    	}
	    	return null;
	    }

	}

	/**
	 * Creates SAX parser with default parameters.
	 */
	public Srx2SaxParser() {
		this(Util.getEmptyParameterMap());
	}
	
	/**
	 * Creates SAX parser with given parameters.
	 * @param parameterMap parameters
	 */
	public Srx2SaxParser(Map<String, Object> parameterMap) {
		boolean validate = getParameter(parameterMap.get(VALIDATE_PARAMETER), 
				DEFAULT_VALIDATE);
		
		factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		
		if (validate) {
			factory.setSchema(getSchema(getReader(getResourceStream(SCHEMA))));
		}
	}
	
	/**
	 * Parses SRX document from reader.
	 * 
	 * @param reader
	 * @return initialized document
	 */
	public SrxDocument parse(Reader reader) {
		try {
			SAXParser parser = factory.newSAXParser();
			
			SrxDocument document = new SrxDocument();
			
			InputSource source = new InputSource(reader);
			source.setEncoding("UTF-8");
			
			SrxHandler handler = new SrxHandler(document);
			parser.parse(source, handler);

			return document;
		} catch (ParserConfigurationException e) {
			throw new XmlException("Parser configuration exception parsing SRX document", e);
		} catch (SAXException e) {
			throw new XmlException("SAX exception parsing SRX document", e);
		} catch (IOException e) {
			throw new XmlException("IO exception parsing SRX document", e);
		}
	}

}
