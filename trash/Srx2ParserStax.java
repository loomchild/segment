package net.sourceforge.segment.srx.io;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;
import static net.rootnode.loomchild.util.xml.Util.getContext;
import static net.rootnode.loomchild.util.xml.Util.getSchema;
import static net.rootnode.loomchild.util.xml.Util.getSource;
import static net.rootnode.loomchild.util.xml.Util.validate;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import net.rootnode.loomchild.util.exceptions.XmlException;
import net.rootnode.loomchild.util.xml.Bind;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.io.bind.Beforebreak;
import net.sourceforge.segment.srx.io.bind.Body;
import net.sourceforge.segment.srx.io.bind.Languagemap;
import net.sourceforge.segment.srx.io.bind.Languagerule;
import net.sourceforge.segment.srx.io.bind.Srx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.txw2.output.StaxSerializer;


/**
 * Represents SRX 2.0 document parser. Responsible for creating and initializing
 * Document according to given SRX.
 * 
 * @author loomchild
 */
public class Srx2ParserStax implements SrxParser {

	private static final Log log = LogFactory.getLog(Srx2Parser.class);

	private static final String SCHEMA = "net/sourceforge/segment/res/xml/srx20.xsd";
	
	private static final String NAMESPACE = "http://www.lisa.org/srx20";

	private static final QName HEADER_ELEMENT = new QName(NAMESPACE, "header");
	private static final QName LANGUAGERULE_ELEMENT = new QName(NAMESPACE, "languagerule");
	private static final QName RULE_ELEMENT = new QName(NAMESPACE, "rule");
	private static final QName BEFOREBREAK_ELEMENT = new QName(NAMESPACE, "beforebreak");
	private static final QName AFTERBREAK_ELEMENT = new QName(NAMESPACE, "afterbreak");
	private static final QName LANGUAGEMAP_ELEMENT = new QName(NAMESPACE, "languagemap");
	
	private static final QName CASCADE_ATTRIBUTE = new QName("cascade");
	private static final QName LANGUAGERULENAME_ATTRIBUTE = new QName("languagerulename");
	private static final QName BREAKING_ATTRIBUTE = new QName("breaking");
	private static final QName LANGUAGEPATTERN_ATTRIBUTE = new QName("languagepattern");
	
	/**
	 * Parses SRX document from reader.
	 * 
	 * @param reader
	 *            Reader.
	 * @return Returns initialized document.
	 */
	public SrxDocument parse(Reader reader) {
		
		try {
			
			Schema schema = getSchema(getReader(getResourceStream(SCHEMA)));

			Source source = getSource(reader);
			
			validate(schema, source);
			
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = factory.createXMLEventReader(source);
			
			SrxDocument document = new SrxDocument();
			Map<String, LanguageRule> languageRuleMap = new HashMap<String, LanguageRule>();
			
			LanguageRule languageRule = null;
			
			boolean breaking = false;
			String before = null;
			String after = null;
			
			boolean inBefore = false;
			boolean inAfter = false;
			
			while(eventReader.hasNext()) {
				
				XMLEvent event = eventReader.nextEvent();
				
				if (event.isStartElement()) {
					
					StartElement startElement = event.asStartElement();
					
					if (HEADER_ELEMENT.equals(startElement.getName())) {
						Attribute attribute = 
							startElement.getAttributeByName(CASCADE_ATTRIBUTE);
						boolean cascade = false;
						if (attribute != null) {
							cascade = "yes".equals(attribute.getValue());
						}
						document.setCascade(cascade);
					}
					
					if (LANGUAGERULE_ELEMENT.equals(startElement.getName())) {
						Attribute attribute = 
							startElement.getAttributeByName(LANGUAGERULENAME_ATTRIBUTE);
						String name = attribute.getValue();
						languageRule = new LanguageRule(name);
					}
					
					if (RULE_ELEMENT.equals(startElement.getName())) {
						Attribute attribute = 
							startElement.getAttributeByName(BREAKING_ATTRIBUTE);
						breaking = true;
						if (attribute != null) {
							breaking = !"no".equals(attribute.getValue());
						}
					}
	
					if (BEFOREBREAK_ELEMENT.equals(startElement.getName())) {
						inBefore = true;
						before = "";
					}
	
					if (AFTERBREAK_ELEMENT.equals(startElement.getName())) {
						inAfter = true;
						after = "";
					}
	
					if (LANGUAGEMAP_ELEMENT.equals(startElement.getName())) {
						Attribute patternAttribute = 
							startElement.getAttributeByName(LANGUAGEPATTERN_ATTRIBUTE);
						Attribute nameAttribute = 
							startElement.getAttributeByName(LANGUAGERULENAME_ATTRIBUTE);
						String pattern = patternAttribute.getValue();
						String name = nameAttribute.getValue();
						LanguageRule mappedLanguageRule = languageRuleMap.get(name);
						if (mappedLanguageRule == null) {
							log.warn("Language map \"" + pattern
							+ "\": language rule \"" + name + "\" not found.");
						} else {
							document.addLanguageMap(pattern, mappedLanguageRule);
						}
					}
	
				}
				
				if (event.isEndElement()) {
					
					EndElement endElement = event.asEndElement();
					
					if (LANGUAGERULE_ELEMENT.equals(endElement.getName())) {
						languageRuleMap.put(languageRule.getName(), languageRule);
						languageRule = null;
					}
	
					if (RULE_ELEMENT.equals(endElement.getName())) {
						Rule rule = new Rule(breaking, before, after);
						languageRule.addRule(rule);
						breaking = false;
						before = null;
						after = null;
					}
	
					if (BEFOREBREAK_ELEMENT.equals(endElement.getName())) {
						inBefore = false;
					}
	
					if (AFTERBREAK_ELEMENT.equals(endElement.getName())) {
						inAfter = false;
					}
	
				}
				
				if (event.isCharacters()) {
					
					Characters characters = event.asCharacters();
					
					if (inBefore) {
						before = before + characters.getData();
					}
	
					if (inAfter) {
						after = after + characters.getData();
					}
	
				}
				
			}
			
			return document;
			
		} catch (XMLStreamException e) {
			
			throw new XmlException("Error parsing document", e);
			
		}
	}

}
