package split.srx;

/**
 * Wyjątek zgłaszany gdy nie ma w dokumencie reguły języka o podanej nazwie.
 *
 * @author Jarek Lipski (loomchild)
 */
public class LanguageRuleNotFound extends RuntimeException {

	private static final long serialVersionUID = 1088867854961885960L;

	public LanguageRuleNotFound(String name) {
		super("Language rule not found: " + name);
	}

}
