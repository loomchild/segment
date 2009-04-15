package split.srx;

import java.util.HashMap;
import java.util.Map;

/**
 * Reprezentuje sparsowany dokument SRX. Odpowiada za przechowywanie reguł
 * językowych i mapujących.
 *
 * @author loomchild
 */
public class Document {

	private Map<String, MapRule> mapRuleMap;
	
	private Map<String, LanguageRule> languageRuleMap;

	/**
	 * Tworzy pusty dokument.
	 */
	public Document() {
		this.mapRuleMap = new HashMap<String, MapRule>();
		this.languageRuleMap = new HashMap<String, LanguageRule>();
	}
	
	/**
	 * @param name Nazwa reguły mapującej.
	 * @return Zwraca regułe mapującą.
	 * @throws MapRuleNotFound Zgłaszany gdy nie istnieje reguła o podanej nazwie.
	 */
	public MapRule getMapRule(String name) {
		MapRule mapRule = mapRuleMap.get(name);
		if (mapRule != null) {
			return mapRule;
		} else {
			throw new MapRuleNotFound(name);
		}
	}
	
	/**
	 * @return Zwraca jedyną regułe mapującą.
	 * @throws MapRuleIsNotSingleton Zgłaszany gdy nie istnieje jedyna reguła mapująca.
	 */
	public MapRule getSingletonMapRule() {
		if (mapRuleMap.size() != 1) {
			throw new MapRuleIsNotSingleton(mapRuleMap.size());
		}
		else {
			return mapRuleMap.entrySet().iterator().next().getValue();
		}
	}
	
	/**
	 * Dodaje regułe mapującą.
	 * @param mapRule reguła.
	 */
	public void putMapRule(MapRule mapRule) {
		mapRuleMap.put(mapRule.getName(), mapRule);
	}
	
	/**
	 * @param name Nazwa reguły językowej.
	 * @return Zwraca regułe językową.
	 * @throws LanguageRuleNotFound Zgłaszany gdy nie istnieje reguła o podanej nazwie.
	 */
	public LanguageRule getLanguageRule(String name) {
		LanguageRule languageRule = languageRuleMap.get(name);
		if (languageRule != null) {
			return languageRule;
		} else {
			throw new LanguageRuleNotFound(name);
		}
	}
	
	/**
	 * Dodaje regułe językową.
	 * @param languageRule reguła.
	 */
	public void putLanguageRule(LanguageRule languageRule) {
		languageRuleMap.put(languageRule.getName(), languageRule);
	}
	
}
