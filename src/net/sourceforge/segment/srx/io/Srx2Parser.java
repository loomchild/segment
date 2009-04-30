package net.sourceforge.segment.srx.io;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;
import static net.rootnode.loomchild.util.xml.Util.getSchema;
import static net.rootnode.loomchild.util.xml.Util.getContext;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.rootnode.loomchild.util.xml.Bind;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.io.bind.Body;
import net.sourceforge.segment.srx.io.bind.Languagemap;
import net.sourceforge.segment.srx.io.bind.Languagerule;
import net.sourceforge.segment.srx.io.bind.Srx;

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

	//private static final String CONTEXT = "net.sourceforge.segment.srx.io.bind";
	private static final Class<?> CLASS_TO_BE_BOUND = 
		net.sourceforge.segment.srx.io.bind.Srx.class;
	
	private static final String SCHEMA = "net/sourceforge/segment/res/xml/srx20.xsd";

	private static Bind bind = new Bind(getContext(CLASS_TO_BE_BOUND),
			getSchema(getReader(getResourceStream(SCHEMA))));

	/**
	 * Parses SRX document from reader.
	 * 
	 * @param reader
	 *            Reader.
	 * @return Returns initialized document.
	 */
	public SrxDocument parse(Reader reader) {
		Srx srx = (Srx) bind.unmarshal(reader);

		SrxDocument document = new SrxDocument();
		document.setCascade(srx.getHeader().getCascade().equals("yes"));

		Body body = srx.getBody();

		Map<String, LanguageRule> languageRuleMap = new HashMap<String, LanguageRule>();
		for (Languagerule lr : body.getLanguagerules().getLanguagerule()) {
			LanguageRule languageRule = new LanguageRule(lr
					.getLanguagerulename());
			for (net.sourceforge.segment.srx.io.bind.Rule r : lr.getRule()) {
				boolean breaking = r.getBreak().equals("yes");

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

				Rule rule = new Rule(breaking, before, after);
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
