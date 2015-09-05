package net.sourceforge.segment.srx.io;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents SRX 2.0 document parser. Responsible for creating and initializing
 * Document according to given SRX. Uses STAX. Cannot validate.
 * 
 * @author loomchild
 */
public class Srx2StaxParser implements SrxParser {

	private static final Log log = LogFactory.getLog(Srx2StaxParser.class);

	/**
	 * Parses SRX document from reader.
	 * 
	 * @param reader
	 * @return initialized document
	 */
	public SrxDocument parse(Reader reader) {
		SrxDocument document = null;
		try {
		
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(reader);
	
			document = new SrxDocument();
			document.setCascade(true);
	
			Map<String, LanguageRule> languageRuleMap = new HashMap<String, LanguageRule>();
			
			LanguageRule languageRule = null;
			boolean breakRule = false;
			String beforeBreak = "";
			String afterBreak = "";
			
			while (parser.hasNext()) {
				int event = parser.next();
	
			    if (event == XMLStreamConstants.START_ELEMENT) {
			    	String name = parser.getLocalName();
			    	
			    	if ("languagerule".equals(name)) {
			    		String ruleName = parser.getAttributeValue(0);
			    		languageRule = new LanguageRule(ruleName);
			    		languageRuleMap.put(languageRule.getName(), languageRule);
			    	} else if ("languagemap".equals(name)) {
			    		String languagePattern = parser.getAttributeValue(0);
			    		String languageRuleName = parser.getAttributeValue(1);
			    		document.addLanguageMap(languagePattern, languageRuleMap.get(languageRuleName));
			    	} else if ("rule".equals(name)) {
						breakRule = !"no".equals(parser.getAttributeValue(0));
			    	} else if ("beforebreak".equals(name)) {
			    		beforeBreak = parser.getElementText();
			    	} else if ("afterbreak".equals(name)) {
			    		afterBreak = parser.getElementText();
			    	}
			    	// TODO: cascade
			    	
			    } else if (event == XMLStreamConstants.END_ELEMENT) {
			    	String name = parser.getLocalName();
			    	
			    	if ("rule".equals(name)) {
						Rule rule = new Rule(breakRule, beforeBreak, afterBreak);
						languageRule.addRule(rule);
						breakRule = false;
			    		beforeBreak = "";
			    		afterBreak = "";
			    	}
			    }
			}
			
			parser.close();
			
		} catch (XMLStreamException e) {
			log.error("Error parsing SRX", e);
		}
		
		return document;
	}

}
