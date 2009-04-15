package net.sourceforge.segment.ui.console;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Perform automatic tests.
 * 
 * @author loomchild
 */
public class Test extends TestSuite {

	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTestSuite(net.sourceforge.segment.srx.UtilTest.class);
		suite.addTestSuite(net.sourceforge.segment.srx.LanguageMapTest.class);
		suite.addTestSuite(net.sourceforge.segment.srx.SrxDocumentTest.class);
		suite.addTestSuite(net.sourceforge.segment.srx.SrxTextIteratorTest.class);

		suite.addTestSuite(net.sourceforge.segment.srx.io.SrxVersionTest.class);
		suite.addTestSuite(net.sourceforge.segment.srx.io.SrxParsersTest.class);
		suite.addTestSuite(net.sourceforge.segment.srx.io.SrxTransformersTest.class);

		return suite;
	}

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

}
