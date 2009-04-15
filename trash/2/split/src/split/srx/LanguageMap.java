package split.srx;

import java.util.regex.Pattern;

/**
 * Reprezentuje mapowanie z danego wzorca na regułe językową.
 *
 * @author loomchild
 */
public class LanguageMap {
	
	private Pattern languagePattern;
	
	private LanguageRule languageRule;

	/**
	 * Tworzy mapowanie.
	 * @param pattern Wyrażenie regularne jako wzorzec kodu języka.
	 * @param languageRule Odpowiadająca wzorcowi reguła językowa.
	 */
	public LanguageMap(String pattern, LanguageRule languageRule) {
		this.languagePattern = Pattern.compile(pattern);
		this.languageRule = languageRule;		
	}
	
	/**
	 * @param languageCode Kod języka.
	 * @return Zwraca true jeśli mapowanie pasuje do danego kodu języka.
	 */
	public boolean matches(String languageCode) {
		return languagePattern.matcher(languageCode).matches();
	}

	/**
	 * @return Zwraca mapowaną regułę językową.
	 */
	public LanguageRule getLanguageRule() {
		return languageRule;
	}

	/**
	 * @return Zwraca wzorzec dopasowania języka.
	 */
	public String getLanguagePattern() {
		return languagePattern.pattern();
	}

}
