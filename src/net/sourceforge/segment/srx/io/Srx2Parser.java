package net.sourceforge.segment.srx.io;

import static net.sourceforge.segment.util.Util.getContext;
import static net.sourceforge.segment.util.Util.getReader;
import static net.sourceforge.segment.util.Util.getResourceStream;
import static net.sourceforge.segment.util.Util.getSchema;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.io.bind.Body;
import net.sourceforge.segment.srx.io.bind.Languagemap;
import net.sourceforge.segment.srx.io.bind.Languagerule;
import net.sourceforge.segment.srx.io.bind.Srx;
import net.sourceforge.segment.util.Bind;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents SRX 2.0 document parser. Responsible for creating and initializing
 * Document according to given SRX.
 * 
 * @author loomchild
 */
public class Srx2Parser implements SrxParser {

	private static final Log log = LogFactory.getLog(Srx2Parser.class);

	private static final String CONTEXT = "net.sourceforge.segment.srx.io.bind";
	
	private static final String SCHEMA = "net/sourceforge/segment/res/xml/srx20.xsd";

	private static Bind bind = createBind();

	private static Bind createBind() {
        // Macintosh Java 1.5 work-around borrowed from okapi library
        // When you use -XstartOnFirstThread as a java -Xarg on Leopard, 
		// your ContextClassloader gets set to null.
		// On other Macs setting this value breaks everything.
		if (Thread.currentThread().getContextClassLoader() == null) {
			Thread.currentThread().setContextClassLoader(Srx2Parser.class.getClassLoader());
		}
		
		// Must pass the ClassLoader directly due to Java 1.5 bugs when using 
		// custom ClassLoader.
		Bind bind = new Bind(
				getContext(CONTEXT, Srx2Parser.class.getClassLoader()),
				getSchema(getReader(getResourceStream(SCHEMA))));
		return bind;
	}
	
	/**
	 * Parses SRX document from reader.
	 * 
	 * @param reader
	 * @return initialized document
	 */
	public SrxDocument parse(Reader reader) {
		Srx srx = (Srx) bind.unmarshal(reader);

		SrxDocument document = new SrxDocument();
		document.setCascade("yes".equals(srx.getHeader().getCascade()));

		Body body = srx.getBody();

		Map<String, LanguageRule> languageRuleMap = new HashMap<String, LanguageRule>();
		for (Languagerule lr : body.getLanguagerules().getLanguagerule()) {
			LanguageRule languageRule = new LanguageRule(lr
					.getLanguagerulename());
			for (net.sourceforge.segment.srx.io.bind.Rule r : lr.getRule()) {
				boolean breakRule = !"no".equals(r.getBreak());

				String before;
				if (r.getBeforebreak() != null) {
					before = r.getBeforebreak().getContent();
				} else {
					before = "";
				}

				String after;
				if (r.getAfterbreak() != null) {
					after = r.getAfterbreak().getContent();
				} else {
					after = "";
				}

				Rule rule = new Rule(breakRule, before, after);
				languageRule.addRule(rule);
			}
			languageRuleMap.put(languageRule.getName(), languageRule);
		}

		for (Languagemap lm : body.getMaprules().getLanguagemap()) {
			LanguageRule languageRule = languageRuleMap.get(lm
					.getLanguagerulename());
			if (languageRule == null) {
				log.warn("Language map \"" + lm.getLanguagepattern()
						+ "\": language rule \"" + lm.getLanguagerulename()
						+ "\" not found.");
			} else {
				document.addLanguageMap(lm.getLanguagepattern(), languageRule);
			}
		}

		return document;
	}

}
