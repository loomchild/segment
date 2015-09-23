package net.loomchild.segment;

import net.loomchild.segment.srx.RuleMatcherTest;
import net.loomchild.segment.srx.SrxDocumentTest;
import net.loomchild.segment.srx.SrxTextIteratorReaderTest;
import net.loomchild.segment.srx.SrxTextIteratorStringTest;
import net.loomchild.segment.srx.io.SrxVersionTest;
import net.loomchild.segment.srx.legacy.BufferTest;
import net.loomchild.segment.srx.legacy.ReaderMatcherTest;
import net.loomchild.segment.util.UtilTest;
import net.loomchild.segment.srx.LanguageMapTest;
import net.loomchild.segment.srx.TextManagerTest;
import net.loomchild.segment.srx.io.SrxParsersTest;
import net.loomchild.segment.srx.io.SrxTransformersTest;
import net.loomchild.segment.srx.legacy.AccurateSrxTextIteratorStringTest;
import net.loomchild.segment.srx.legacy.FastTextIteratorReaderTest;
import net.loomchild.segment.srx.legacy.FastTextIteratorStringTest;
import net.loomchild.segment.srx.legacy.ReaderCharSequenceTest;

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
