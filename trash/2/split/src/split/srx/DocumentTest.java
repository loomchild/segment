package split.srx;

import junit.framework.TestCase;

public class DocumentTest extends TestCase {

	public void testMapRule() {
		Document document = new Document();
		try {
			document.getMapRule("a");
			fail("Znaleziono nieistniejącą regułe mapującą: a");
		} catch (MapRuleNotFound e) {
			//Spodziewane
		}
		try {
			document.getSingletonMapRule();
			fail("Znaleziono nieistniejącą pojedyńczą regułe mapującą");
		} catch (MapRuleIsNotSingleton e) {
			//Spodziewane
		}
		MapRule mapRuleA = new MapRule("a");
		document.putMapRule(mapRuleA);
		assertEquals(mapRuleA, document.getMapRule("a"));
		assertEquals(mapRuleA, document.getSingletonMapRule());
		MapRule mapRuleB = new MapRule("b");
		document.putMapRule(mapRuleB);
		assertEquals(mapRuleA, document.getMapRule("a"));
		assertEquals(mapRuleB, document.getMapRule("b"));
		try {
			document.getSingletonMapRule();
			fail("Znaleziono pojedyńczą regułe mapującą mimo że były dwie");
		} catch (MapRuleIsNotSingleton e) {
			//Spodziewane
		}
		MapRule mapRuleA2 = new MapRule("a");
		document.putMapRule(mapRuleA2);
		assertEquals(mapRuleA2, document.getMapRule("a"));
	}

	public void testLanguageRule() {
		Document document = new Document();
		try {
			document.getLanguageRule("a");
			fail("Znaleziono nieistniejącą regułe języka: a");
		} catch (LanguageRuleNotFound e) {
			//Spodziewane
		}
		LanguageRule languageRuleA = new LanguageRule("a");
		LanguageRule languageRuleB = new LanguageRule("b");
		document.putLanguageRule(languageRuleA);
		document.putLanguageRule(languageRuleB);
		assertEquals(languageRuleA, document.getLanguageRule("a"));
		assertEquals(languageRuleB, document.getLanguageRule("b"));
		try {
			document.getLanguageRule("c");
			fail("Znaleziono nieistniejącą regułe języka: c");
		} catch (LanguageRuleNotFound e) {
			//Spodziewane
		}
		LanguageRule languageRuleA2 = new LanguageRule("a");
		document.putLanguageRule(languageRuleA2);
		assertEquals(languageRuleA2, document.getLanguageRule("a"));
	}
	
}
