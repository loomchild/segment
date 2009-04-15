package split.srx;

import java.util.regex.Pattern;

/**
 * Reprezentuje regułe podziału albo wyjątek. Odpowiada za przechowywanie
 * swoich wzorców tekstu przed i po znaku podziału,
 *
 * @author loomchild
 */
public class Rule {

	private boolean breaking;
	
	private String beforePattern;
	
	private String afterPattern;
	
	/**
	 * Tworzy regułe.
	 * @param breaking True - dzielącą, false - wyjątek od dzielenia.
	 * @param beforePattern Wzorzec tekstu przed znakiem podziału.
	 * @param afterPattern Wzorzec tekstu po znaku podziału.
	 */
	public Rule(boolean breaking, String beforePattern, String afterPattern) {
		this.breaking = breaking;
		this.beforePattern = beforePattern;
		this.afterPattern = afterPattern;
	}
	
	/**
	 * @return Zwraca true jeśli reguła dzieli segment, false jeśli to wyjątek.
	 */
	public boolean isBreaking() {
		return breaking;
	}

	/**
	 * @return Zwraca wzorzec tekstu przed znakiem podziału.
	 */
	public String getBeforePattern() {
		return beforePattern;
	}
	
	/**
	 * @return Zwraca wzorzec tekstu po znaku podziału.
	 */
	public String getAfterPattern() {
		return afterPattern;
	}
	
}
