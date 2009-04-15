package textsplitter;

import java.io.IOException;
import java.io.Reader;

import loomchild.util.xml.XmlException;
import split.srx.Document;
import split.srx.LanguageRule;
import split.srx.MapRule;
import split.srx.Parser;
import split.srx.SplitPattern;
import split.srx.SrxSplitter;

/**
 * Adapter klasy SrxSplitter.
 *
 * @author loomchild
 */
public class SrxTextSplitter extends AbstractTextSplitter {
	
	public SrxTextSplitter(String srxFileName) throws IOException, XmlException {
		this.document = Parser.getInstance().parse(srxFileName);		
	}
	
	public void initialize(Reader reader, String languageCode) {
		MapRule mapRule = document.getSingletonMapRule();
		LanguageRule languageRule = 
				mapRule.getLanguageMap(languageCode).getLanguageRule();
		SplitPattern splitPattern = new SplitPattern(languageRule);
		setSplitter(new SrxSplitter(splitPattern, reader));
	}

	private Document document;

}
