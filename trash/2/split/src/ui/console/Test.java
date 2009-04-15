package ui.console;


import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Klasa agregująca wszystkie testy. Uruchmienie jej testuje program.
 *
 * @author loomchild
 */
public class Test extends TestSuite {

	public static junit.framework.Test suite() {		
		TestSuite suite = new TestSuite();

		suite.addTestSuite(split.simple.SimpleSplitterTest.class);
		suite.addTestSuite(split.srx.DocumentTest.class);
		suite.addTestSuite(split.srx.SrxSplitterTest.class);
		suite.addTestSuite(split.srx.LanguageMapTest.class);
		suite.addTestSuite(split.srx.MapRuleTest.class);
		suite.addTestSuite(split.srx.ParserTest.class);
		suite.addTestSuite(textsplitter.TextSplitterTest.class);

		return suite;
	}
	
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

}
