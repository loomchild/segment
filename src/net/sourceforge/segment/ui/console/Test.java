package net.sourceforge.segment.ui.console;

import junit.framework.TestSuite;
import net.sourceforge.segment.SegmentTestSuite;

import org.junit.internal.runners.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Perform automatic tests.
 * 
 * @author loomchild
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	SegmentTestSuite.class
})
public class Test extends TestSuite {

	public void run(String[] args) {
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener());
        core.run(Test.class);
	}

}
