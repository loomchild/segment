package split.srx;

import java.util.LinkedList;
import java.util.List;

/**
 * Reprezentuje regułe mapującą. Odpowiada za mapowanie z kodu języka na 
 * regułe językową. 
 *
 * @author loomchild
 */
public class MapRule {
	
	private List<LanguageMap> languageMapList;

	private String name;
	
	public MapRule(String name) {
		this.name = name;
		this.languageMapList = new LinkedList<LanguageMap>();
	}

	/**
	 * Wyszukuje najlepiej dopasowane mapowanie dla języka o podanym kodzie.
	 * @param languageCode Kod języka, np PL_pl.
	 * @return Zwraca mapowanie odpowiednie dla danego kodu.
	 * @throws LanguageMapNotFound Zgłaszany gdy nie znaleziono żadnego mapowania. 
	 */
	public LanguageMap getLanguageMap(String languageCode) {
		for (LanguageMap languageMap : languageMapList) {
			if (languageMap.matches(languageCode)) {
				return languageMap;
			}
		}
		throw new LanguageMapNotFound(languageCode);
	}
	
	/**
	 * Dodaje mapowanie.
	 * @param languageMap Mapowanie.
	 */
	public void addLanguageMap(LanguageMap languageMap) {
		languageMapList.add(languageMap);
	}
	
	/**
	 * @return Zwraca swoją nazwę.
	 */
	public String getName() {
		return name;
	}
	
}
