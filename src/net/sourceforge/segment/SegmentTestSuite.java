package net.sourceforge.segment;

import net.sourceforge.segment.srx.LanguageMapTest;
import net.sourceforge.segment.srx.LegacySrxTextIteratorTest;
import net.sourceforge.segment.srx.RuleMatcherTest;
import net.sourceforge.segment.srx.SrxDocumentTest;
import net.sourceforge.segment.srx.SrxTextIteratorTest;
import net.sourceforge.segment.srx.UtilTest;
import net.sourceforge.segment.srx.io.SrxParsersTest;
import net.sourceforge.segment.srx.io.SrxTransformersTest;
import net.sourceforge.segment.srx.io.SrxVersionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	UtilTest.class, LanguageMapTest.class, SrxDocumentTest.class,
	RuleMatcherTest.class,

	SrxTextIteratorTest.class, LegacySrxTextIteratorTest.class,

	SrxVersionTest.class, SrxParsersTest.class, SrxTransformersTest.class
})
public class SegmentTestSuite {

}
