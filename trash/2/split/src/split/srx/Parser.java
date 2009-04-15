package split.srx;

import java.io.IOException;
import java.net.URL;

import loomchild.util.exceptions.InitializationException;
import loomchild.util.xml.XmlException;
import loomchild.util.xml.XmlNode;
import loomchild.util.xml.XmlParser;


/**
 * Reprezentuje parser dokumentów SRX. Odpowiada za tworzenie zainicjalizowanych
 * obiektów Document. Singleton.
 *
 * @author loomchild
 */
public class Parser {
	
	private static Parser instance;
	
	private XmlParser xmlParser;
	
	/**
	 * Stosowany schemat pliku XML.
	 */
	public static final String SCHEMA = "srx.xsd"; 

	public static Parser getInstance() {
		if (instance == null) {
			instance = new Parser();
		}
		return instance;
	}
	
	/**
	 * Parsuje plik do dokumentu SRX.
	 * @param fileName Nazwa.
	 * @return Zwraca utworzony dokument.
	 * @throws IOException Zgłąszany gdy nastąpi błąd wejścia wyjścia.
	 * @throws XmlException Zgłaszany gdy nastąpi błąd parsowania.
	 */
	public Document parse(String fileName) throws IOException, XmlException {
		Document document = new Document();
		XmlNode doc = xmlParser.parseToNode(fileName);
		//TODO: Parse settings
		XmlNode header = doc.getChild("header");
		XmlNode body = doc.getChild("body");
		XmlNode languageRules = body.getChild("languagerules");
		for (XmlNode languageRule : languageRules.getChildren()) {
			String name = languageRule.getAttribute("languagerulename");
			LanguageRule lr = new LanguageRule(name);
			for (XmlNode rule : languageRule.getChildren()) {
				boolean breaking = rule.getAttribute("break").equals("yes");
				String before = rule.getChild("beforebreak").getValue();
				String after = rule.getChild("afterbreak").getValue();
				Rule r = new Rule(breaking, before, after);
				lr.addRule(r);
			}
			document.putLanguageRule(lr);
		}
		XmlNode mapRules = body.getChild("maprules");
		for (XmlNode mapRule : mapRules.getChildren()) {
			String name = mapRule.getAttribute("maprulename");
			MapRule mr = new MapRule(name);
			for (XmlNode languageMap : mapRule.getChildren()) {
				String pattern = languageMap.getAttribute("languagepattern");
				String lrname = languageMap.getAttribute("languagerulename");
				mr.addLanguageMap(new LanguageMap(pattern, 
						document.getLanguageRule(lrname)));
			}
			document.putMapRule(mr);
		}
		
		return document;
	}
	
	private Parser() {
		URL url = getClass().getClassLoader().getResource(SCHEMA);
		if (url == null) {
			throw new InitializationException(
					"Nie mogę otworzyć schematu pliku srx: " + SCHEMA);
		}
		this.xmlParser = new XmlParser(url);
	}
	
}
