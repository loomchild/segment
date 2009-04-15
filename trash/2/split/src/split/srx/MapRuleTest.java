package split.srx;

import junit.framework.TestCase;

public class MapRuleTest extends TestCase {

	public void testGetLanguageRule() {
		LanguageRule languageRule1 = new LanguageRule("");
		LanguageRule languageRule2 = new LanguageRule("");
		LanguageRule defaultLanguageRule = new LanguageRule("");
		MapRule mapRule = new MapRule("");
		mapRule.addLanguageMap(new LanguageMap("PL.*",languageRule1));
		mapRule.addLanguageMap(new LanguageMap("EN.*",languageRule2));
		assertEquals(languageRule1, 
				mapRule.getLanguageMap("PL_pl").getLanguageRule());
		assertEquals(languageRule2, 
				mapRule.getLanguageMap("EN_us").getLanguageRule());
		try {
			LanguageMap map = mapRule.getLanguageMap("FI_fi");
			fail("Znaleziono nieistniejącą regułę mapująca dla FI_fi: " + 
					map.getLanguagePattern());
		} catch (LanguageMapNotFound e) {
			//Spodziewane
		}
		mapRule.addLanguageMap(new LanguageMap(".*",defaultLanguageRule));
		assertEquals(defaultLanguageRule, 
				mapRule.getLanguageMap("FI_fi").getLanguageRule());
	}
	
}
