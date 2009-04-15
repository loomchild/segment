package net.sourceforge.segment.ui.console;

import static net.rootnode.loomchild.util.io.Util.getFileInputStream;
import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getResourceStream;
import static net.rootnode.loomchild.util.io.Util.getWriter;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.SrxTextIterator;
import net.sourceforge.segment.srx.SrxTransformer;
import net.sourceforge.segment.srx.io.Srx1Transformer;
import net.sourceforge.segment.srx.io.SrxAnyParser;
import net.sourceforge.segment.srx.io.SrxAnyTransformer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Text user interface to splitter.
 * 
 * @author loomchild
 */
public class Segment {

	public static final String DEFAULT_SRX = "net/sourceforge/segment/res/xml/default.srx";

	public static final String DEFAULT_BEGIN_SEGMENT = "";
	public static final String DEFAULT_END_SEGMENT = "\n";

	public static void main(String[] args) {
		try {
			Segment main = new Segment();
			main.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private Options createOptions() {
		Options options = new Options();
		options.addOption("s", "srx", true, "SRX file.");
		options.addOption("l", "lang", true, "Language code.");
		options.addOption("m", "map", true, "Map rule name in SRX 1.0.");
		options.addOption("b", "begin", true, "Output segment prefix.");
		options.addOption("e", "end", true, "Output segment suffix.");
		options.addOption("h", "help", false, "Print this help.");
		return options;
	}

	private void run(String[] args) throws Exception {
		Options options = createOptions();
		HelpFormatter helpFormatter = new HelpFormatter();
		BasicParser parser = new BasicParser();
		CommandLine commandLine = null;
		PrintWriter err = new PrintWriter(System.err, true);

		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			helpFormatter.printUsage(err, 80, "segment --help");
			System.exit(1);
		}
		if (commandLine.hasOption('h')) {
			helpFormatter.printHelp("segment", options);
			System.exit(0);
		}

		Reader reader = getReader((System.in));
		Writer writer = getWriter((System.out));

		String beginSegment = commandLine.getOptionValue('b');
		if (beginSegment == null) {
			beginSegment = DEFAULT_BEGIN_SEGMENT;
		}
		String endSegment = commandLine.getOptionValue('e');
		if (endSegment == null) {
			endSegment = DEFAULT_END_SEGMENT;
		}

		TextIterator textIterator;

		Reader srxReader;
		String srxFileName = commandLine.getOptionValue('s');
		if (srxFileName != null) {
			srxReader = getReader(getFileInputStream(srxFileName));
		} else {
			srxReader = getReader(getResourceStream(DEFAULT_SRX));
		}

		String languageCode = commandLine.getOptionValue('l');
		if (languageCode == null) {
			languageCode = "";
		}

		Map<String, Object> parameterMap = new HashMap<String, Object>();

		String mapRule = commandLine.getOptionValue("m");
		if (mapRule != null) {
			parameterMap.put(Srx1Transformer.MAP_RULE_NAME, mapRule);
		}

		// If there are transformation parameters then separate transformation
		// is needed.
		if (parameterMap.size() > 0) {
			SrxTransformer transformer = new SrxAnyTransformer();
			srxReader = transformer.transform(srxReader, parameterMap);
		}

		SrxParser srxParser = new SrxAnyParser();
		SrxDocument document = srxParser.parse(srxReader);
		textIterator = new SrxTextIterator(document, languageCode, reader);

		while (textIterator.hasNext()) {
			String segment = textIterator.next();
			writer.write(beginSegment);
			writer.write(segment);
			writer.write(endSegment);
		}

		srxReader.close();
		reader.close();
		writer.close();
	}

}
