package textsplitter;

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

		suite.addTestSuite(textsplitter.TextSplitterTest.class);

		return suite;
	}

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

}
