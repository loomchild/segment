package split.srx;

/**
 * Wyjątek zgłaszany gdy nie uda się znaleźć mapowania w regule mapującej 
 * dla danego kodu języka.
 *
 * @author Jarek Lipski (loomchild)
 */
public class LanguageMapNotFound extends RuntimeException {

	private static final long serialVersionUID = 2234141212274789705L;

	public LanguageMapNotFound(String languageCode) {
		super("Language map not found for language code: " + languageCode);
	}
	
}
