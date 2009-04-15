package split.srx;

/**
 * Wyjątek zgłaszany gdy nie ma w dokumencie reguły mapującej o podanej nazwie.
 *
 * @author Jarek Lipski (loomchild)
 */
public class MapRuleNotFound extends RuntimeException {

	private static final long serialVersionUID = 1736781247432520789L;
	
	public MapRuleNotFound(String name) {
		super("Map rule not found: " + name);
	}

}
