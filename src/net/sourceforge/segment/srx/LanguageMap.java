package net.sourceforge.segment.srx;

import java.util.regex.Pattern;

/**
 * Represents mapping between language code pattern and language rule.
 * 
 * @author loomchild
 */
public class LanguageMap {

	private Pattern languagePattern;

	private LanguageRule languageRule;

	/**
	 * Creates mapping.
	 * 
	 * @param pattern language code pattern
	 * @param languageRule language rule
	 */
	public LanguageMap(String pattern, LanguageRule languageRule) {
		this.languagePattern = Pattern.compile(pattern);
		this.languageRule = languageRule;
	}

	/**
	 * @param languageCode language code
	 * @return true if given language code matches language pattern
	 */
	public boolean matches(String languageCode) {
		return languagePattern.matcher(languageCode).matches();
	}

	/**
	 * @return language rule
	 */
	public LanguageRule getLanguageRule() {
		return languageRule;
	}

}
