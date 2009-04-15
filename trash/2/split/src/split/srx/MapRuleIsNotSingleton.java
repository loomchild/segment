package split.srx;

/**
 * Wyjątek zgłaszany gdy próbuje się pobrać z dokumentu jedyną regułe mapującą, 
 * ale nie ma żadnych reguł albo jest więcej niż jedna. 
 *
 * @author Jarek Lipski (loomchild)
 */
public class MapRuleIsNotSingleton extends IllegalStateException {

	private static final long serialVersionUID = -8763720655249979758L;

	public MapRuleIsNotSingleton(int count) {
		super("There is not exactly one map rule, there is " + 
				count + " rules.");
	}
	
}
