package net.sourceforge.segment;

import net.sourceforge.segment.srx.LanguageMapTest;
import net.sourceforge.segment.srx.RuleMatcherTest;
import net.sourceforge.segment.srx.SrxDocumentTest;
import net.sourceforge.segment.srx.SrxTextIteratorReaderTest;
import net.sourceforge.segment.srx.SrxTextIteratorStringTest;
import net.sourceforge.segment.srx.TextManagerTest;
import net.sourceforge.segment.srx.io.SrxParsersTest;
import net.sourceforge.segment.srx.io.SrxTransformersTest;
import net.sourceforge.segment.srx.io.SrxVersionTest;
import net.sourceforge.segment.srx.legacy.AccurateSrxTextIteratorStringTest;
import net.sourceforge.segment.srx.legacy.BufferTest;
import net.sourceforge.segment.srx.legacy.FastTextIteratorReaderTest;
import net.sourceforge.segment.srx.legacy.FastTextIteratorStringTest;
import net.sourceforge.segment.srx.legacy.ReaderCharSequenceTest;
import net.sourceforge.segment.srx.legacy.ReaderMatcherTest;
import net.sourceforge.segment.util.UtilTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	LanguageMapTest.class, 
	SrxDocumentTest.class, 
	TextManagerTest.class,
	SrxVersionTest.class, 
	SrxParsersTest.class, 
	SrxTransformersTest.class,
	UtilTest.class, 
	SrxTextIteratorStringTest.class, 
	SrxTextIteratorReaderTest.class,

	RuleMatcherTest.class,
	BufferTest.class,
	ReaderCharSequenceTest.class,
	ReaderMatcherTest.class,
	AccurateSrxTextIteratorStringTest.class,
	FastTextIteratorStringTest.class, 
	FastTextIteratorReaderTest.class 

})
public class SegmentTestSuite {

}
