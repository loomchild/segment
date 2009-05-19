package net.sourceforge.segment;

import net.sourceforge.segment.srx.LanguageMapTest;
import net.sourceforge.segment.srx.LegacySrxTextIteratorStringTest;
import net.sourceforge.segment.srx.RuleMatcherTest;
import net.sourceforge.segment.srx.SrxDocumentTest;
import net.sourceforge.segment.srx.SrxTextIteratorReaderTest;
import net.sourceforge.segment.srx.SrxTextIteratorStringTest;
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

	SrxTextIteratorStringTest.class, SrxTextIteratorReaderTest.class, 
	LegacySrxTextIteratorStringTest.class,

	SrxVersionTest.class, SrxParsersTest.class, SrxTransformersTest.class
})
public class SegmentTestSuite {

}
