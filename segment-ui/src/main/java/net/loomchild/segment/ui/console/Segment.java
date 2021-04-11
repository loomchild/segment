package net.loomchild.segment.ui.console;

import static net.loomchild.segment.util.Util.getFileInputStream;
import static net.loomchild.segment.util.Util.getFileOutputStream;
import static net.loomchild.segment.util.Util.getReader;
import static net.loomchild.segment.util.Util.getResourceStream;
import static net.loomchild.segment.util.Util.getWriter;
import static net.loomchild.segment.util.Util.readAll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.loomchild.segment.srx.io.Srx2Parser;
import net.loomchild.segment.srx.io.Srx2SaxParser;
import net.loomchild.segment.srx.io.Srx2StaxParser;
import net.loomchild.segment.srx.io.SrxAnyTransformer;
import net.loomchild.segment.util.NullWriter;
import net.loomchild.segment.util.Version;
import net.loomchild.segment.TextIterator;
import net.loomchild.segment.srx.LanguageRule;
import net.loomchild.segment.srx.Rule;
import net.loomchild.segment.srx.SrxDocument;
import net.loomchild.segment.srx.SrxParser;
import net.loomchild.segment.srx.SrxTextIterator;
import net.loomchild.segment.srx.SrxTransformer;
import net.loomchild.segment.srx.io.Srx1Transformer;
import net.loomchild.segment.srx.io.SrxAnyParser;
import net.loomchild.segment.srx.legacy.AccurateSrxTextIterator;
import net.loomchild.segment.srx.legacy.FastTextIterator;
import net.loomchild.segment.srx.legacy.ScannerSrxTextIterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

/**
 * Text user interface to splitter.
 * 
 * @author loomchild
 */
public class Segment {

	private static final Log log = LogFactory.getLog(Segment.class);

	private enum Algorithm {
		accurate, fast, ultimate, scanner;
	}

	private enum Parser {
		jaxb, sax, stax;
	}

	public static final String DEFAULT_SRX = "net/loomchild/segment/res/xml/default.srx";

	public static final String EOLN = System.getProperty("line.separator");

	public static final String DEFAULT_BEGIN_SEGMENT = "";
	public static final String DEFAULT_END_SEGMENT = EOLN;

	/* These constants apply to text / SRX generation */
	public static final int WORD_LENGTH = 2;
	public static final int SENTENCE_LENGTH = 5;

	public static final String TEST_SUITE_CLASS_NAME = "net.loomchild.segment.SegmentTestSuite";

	private Random random;
	private String text;
	private boolean stdinReader;
	private boolean stdoutWriter;

	public static void main(String[] args) {
		try {
			Segment main = new Segment();
			main.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public Segment() {
		this.random = new Random();
	}

	private void run(String[] args) throws Exception {
		Options options = createOptions();
		HelpFormatter helpFormatter = new HelpFormatter();
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;

		try {

			commandLine = parser.parse(options, args);

			if (commandLine.hasOption('h')) {
				printHelp(options, helpFormatter);
			} else if (commandLine.hasOption('z')) {
				test();
			} else if (commandLine.hasOption('t')) {
				transform(commandLine);
			} else {
				segment(commandLine);
			}

		} catch (ParseException e) {
			printUsage(helpFormatter);
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

	}

	private Options createOptions() {
		Options options = new Options();
		options.addOption("s", "srx", true, "SRX file.");
		options.addOption("l", "language", true, "Language code.");
		options.addOption("m", "map", true, "Map rule name in SRX 1.0.");
		options.addOption("b", "begin", true, "Output segment prefix.");
		options.addOption("e", "end", true, "Output segment suffix.");
		options.addOption("a", "algorithm", true, "Algorithm. Can be accurate, fast or ultimate (default).");
		options.addOption("d", "parser", true, "Parser. Can be sax, stax or jaxb (default).");
		options.addOption("i", "input", true, "Use given input file instead of standard input.");
		options.addOption("o", "output", true, "Use given output file instead of standard output.");
		options.addOption("t", "transform", false, "Convert old SRX to current version.");
		options.addOption("p", "profile", false, "Print profile information.");
		options.addOption("r", "preload", false, "Preload document into memory before segmentation.");
		options.addOption("2", "twice", false, "Repeat the whole process twice.");
		options.addOption("z", "test", false, "Test the application by running a test suite.");
		options.addOption(null, "lookbehind", true, "Maximum length of a regular expression construct that occurs in lookbehind. Default: " + SrxTextIterator.DEFAULT_MAX_LOOKBEHIND_CONSTRUCT_LENGTH + ".");
		options.addOption(null, "buffer-length", true, "Length of a buffer when reading text as a stream. Default: " + SrxTextIterator.DEFAULT_BUFFER_LENGTH + ".");
		options.addOption(null, "margin", true, "If rule is matched but its position is in the margin (position > bufferLength - margin) then the matching is ignored. Default " + SrxTextIterator.DEFAULT_MARGIN + ".");
		options.addOption(null, "generate-text", true, "Generate random input with given length in KB.");
		options.addOption(null, "generate-srx", true, "Generate random segmentation rules with given rule count and rule length separated by a comma.");
		options.addOption("h", "help", false, "Print this help.");
		return options;
	}

	private void printUsage(HelpFormatter helpFormatter) {
		System.out.println("Unknown command. Use segment -h for help.");
	}

	private void printHelp(Options options, HelpFormatter helpFormatter) {
		String signature = "Segment";
		if (Version.getInstance().getVersion() != null) {
			signature += " " + Version.getInstance().getVersion();
		}
		if (Version.getInstance().getDate() != null) {
			signature += ", " + Version.getInstance().getDate();
		}
		signature += ".";
		System.out.println(signature);
		helpFormatter.printHelp("segment", options);
	}

	private void segment(CommandLine commandLine) throws IOException {

		Reader reader = null;
		Writer writer = null;

		try {

			boolean profile = commandLine.hasOption('p');
			boolean twice = commandLine.hasOption('2');
			boolean preload = commandLine.hasOption('r');

			if (twice && !profile) {
				throw new RuntimeException("Can only repeat segmentation twice in profile mode.");
			}

			reader = createTextReader(commandLine, profile, twice, preload);

			writer = createTextWriter(commandLine);

			if (preload) {
				preloadText(reader, profile);
			}

			SrxDocument document = createSrxDocument(commandLine, profile);

			createAndSegment(commandLine, document, reader, writer, profile);

			if (twice) {

				reader = createTextReader(commandLine, profile, twice, preload);

				createAndSegment(commandLine, document, reader, writer, profile);

			}

		} finally {
			cleanupReader(reader);
			cleanupWriter(writer);
		}

	}

	private void test() {
		JUnitCore core = new JUnitCore();
		core.addListener(new TextListener(System.out));
		try {
			Class<?> klass = Class.forName(TEST_SUITE_CLASS_NAME);
			core.run(klass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to find test suite class: "
					+ TEST_SUITE_CLASS_NAME
					+ ". Check that you have tests JAR in your classpath.", e);
		}
	}

	private Reader createTextReader(CommandLine commandLine, boolean profile,
			boolean twice, boolean preload) throws IOException {
		Reader reader;

		if (commandLine.hasOption("generate-text")) {
			reader = createRandomTextReader(commandLine.getOptionValue("generate-text"), profile);
		} else if (commandLine.hasOption('i')) {
			reader = createFileReader(commandLine.getOptionValue('i'));
		} else {
			if (twice && !preload) {
				throw new RuntimeException("Cannot read standard input twice. "
						+ "Preload text (-r), provide an input file (-i) or generate input text (-x).");
			}
			reader = createStandardInputReader();
		}

		return reader;
	}

	private Reader createStandardInputReader() {
		Reader reader = getReader(System.in);
		// Indicate that our reader is standard input to avoid closing it.
		stdinReader = true;
		return reader;
	}

	private Reader createFileReader(String fileName) throws IOException {
		InputStream inputStream = getFileInputStream(fileName);
		Reader reader = getReader(inputStream);

		return reader;
	}

	private Reader createRandomTextReader(String generateTextOption,
			boolean profile) {

		if (text == null) {
			long start = System.currentTimeMillis();
			if (profile) {
				System.out.print("Generating text... ");
			}

			this.text = generateText(generateTextOption);

			if (profile) {
				System.out.println(System.currentTimeMillis() - start + " ms.");
			}
		}

		Reader reader = new StringReader(text);
		return reader;
	}

	private String generateText(String generateTextOption) {
		int textLength = Integer.parseInt(generateTextOption);
		if (textLength < 1) {
			throw new RuntimeException("Text too short: " + textLength + "K.");
		}

		int wordCount = textLength * 1024 / (WORD_LENGTH + 1);
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < wordCount; ++i) {
			stringBuilder.append(' ');
			String word = generateWord(WORD_LENGTH);
			stringBuilder.append(word);
			if ((i % SENTENCE_LENGTH) == 0) {
				stringBuilder.append('.');
			}
		}

		return stringBuilder.toString();
	}

	private String generateWord(int length) {
		StringBuilder word = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			char character = generateCharacter();
			word.append(character);
		}
		return word.toString();
	}

	private char generateCharacter() {
		int character = random.nextInt('Z' - 'A' + 1) + 'A';
		return (char)character;
	}

	private Writer createTextWriter(CommandLine commandLine) {
		Writer writer;

		if (commandLine.hasOption('p')) {
			writer = new NullWriter();
		} else if (commandLine.hasOption('o')) {
			writer = createFileWriter(commandLine.getOptionValue('o'));
		} else {
			writer = createStandardOutputWriter();
		}

		return writer;
	}

	private Writer createStandardOutputWriter() {
		Writer writer = getWriter(System.out);
		// Indicate that our writer is standard output to avoid closing it.
		stdoutWriter = true;
		return writer;
	}

	private Writer createFileWriter(String fileName) {
		OutputStream outputStream = getFileOutputStream(fileName);
		Writer writer = getWriter(outputStream);

		return writer;
	}

	private SrxDocument createSrxDocument(CommandLine commandLine,
			boolean profile) throws IOException {
		SrxDocument document;

		if (commandLine.hasOption("generate-srx")) {
			document = generateSrxDocument(commandLine, profile);
		} else {
			document = readSrxDocument(commandLine, profile);
		}

		return document;
	}

	private SrxDocument readSrxDocument(CommandLine commandLine, boolean profile)
			throws IOException {

		if (profile) {
			System.out.print("Reading rules... ");
		}

		long start = System.currentTimeMillis();

		Reader srxReader;

		String fileName = commandLine.getOptionValue('s');
		if (fileName != null) {
			srxReader = getReader(getFileInputStream(fileName));
		} else {
			srxReader = getReader(getResourceStream(DEFAULT_SRX));
		}

		Map<String, Object> parameterMap = new HashMap<String, Object>();

		String mapRule = commandLine.getOptionValue('m');
		if (mapRule != null) {
			parameterMap.put(Srx1Transformer.MAP_RULE_NAME, mapRule);
		}

		// If there are transformation parameters then separate transformation
		// is needed.
		if (parameterMap.size() > 0) {
			SrxTransformer transformer = new SrxAnyTransformer();
			srxReader = transformer.transform(srxReader, parameterMap);
		}

		SrxParser srxParser = createParser(commandLine, profile);

		SrxDocument document = srxParser.parse(srxReader);
		srxReader.close();

		if (profile) {
			System.out.println(System.currentTimeMillis() - start + " ms.");
		}

		return document;
	}

	private SrxParser createParser(CommandLine commandLine, boolean profile) {
		Parser parser = Parser.jaxb;
		String parserString = commandLine.getOptionValue('d');
		if (parserString != null) {
			parser = Parser.valueOf(parserString);
		}

		SrxParser srxParser;
		switch (parser) {
		case jaxb:
			srxParser = new Srx2Parser();
			break;
		case sax:
			srxParser = new Srx2SaxParser();
			break;
		case stax:
			srxParser = new Srx2StaxParser();
			break;
		default:
			throw new IllegalArgumentException("Unknown parser: " + parser + ".");
		}

		srxParser = new SrxAnyParser(srxParser);

		return srxParser;
	}

	private SrxDocument generateSrxDocument(CommandLine commandLine, boolean profile) {
		if (profile) {
			System.out.print("Generating rules... ");
		}

		long start = System.currentTimeMillis();

		String generateSrxOption = commandLine.getOptionValue("generate-srx");
		String[] parts = generateSrxOption.split(",");
		if (parts.length != 2) {
			throw new RuntimeException("Cannot parse rule count and length.");
		}
		int ruleCount = Integer.parseInt(parts[0]);
		if (ruleCount < 0) {
			throw new RuntimeException("Rule count must be positive: " + ruleCount + ".");
		}
		int ruleLength = Integer.parseInt(parts[1]);
		if (ruleLength < 1) {
			throw new RuntimeException("Rule length must be greater or equal to one: " + ruleCount + ".");
		}

		SrxDocument srxDocument = new SrxDocument();
		LanguageRule languageRule = generateLanguageRule(ruleCount, ruleLength);
		srxDocument.addLanguageMap(".*", languageRule);

		if (profile) {
			System.out.println(System.currentTimeMillis() - start + " ms.");
		}

		return srxDocument;
	}

	private LanguageRule generateLanguageRule(int ruleCount, int ruleLenght) {
		LanguageRule languageRule = new LanguageRule("");
		// Add rules
		for (int i = 0; i < ruleCount; ++i) {
			Rule rule = generateRule(ruleLenght);
			languageRule.addRule(rule);
		}
		// Add end of sentence rule
		languageRule.addRule(new Rule(true, "\\.", " "));
		return languageRule;
	}

	private Rule generateRule(int length) {
		StringBuilder regex = new StringBuilder();
		regex.append('(');
		for (int i = 0; i < length; ++i) {
			String word = generateWord(WORD_LENGTH);
			regex.append(word);
			if (i != length - 1) {
				regex.append('|');
			}
		}
		regex.append(')');
		Rule rule = new Rule(false, regex + "\\.", " ");
		return rule;
	}

	private void createAndSegment(CommandLine commandLine,
			SrxDocument document, Reader reader, Writer writer, boolean profile) 
			throws IOException {

		if (profile) {
			System.out.println("Segmenting... ");
		}

		long start = System.currentTimeMillis();

		TextIterator textIterator = createTextIterator(commandLine, document, reader, profile);

		performSegment(commandLine, textIterator, writer, profile);

		if (profile) {
			System.out.println(System.currentTimeMillis() - start + " ms.");
		}

	}

	private TextIterator createTextIterator(CommandLine commandLine,
			SrxDocument document, Reader reader, boolean profile) {

		TextIterator textIterator;

		String languageCode = commandLine.getOptionValue('l');
		if (languageCode == null) {
			languageCode = "";
		}

		String algorithmString = commandLine.getOptionValue('a');
		Algorithm algorithm = Algorithm.ultimate;
		if (algorithmString != null) {
			algorithm = Algorithm.valueOf(algorithmString);
		}

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		if (commandLine.hasOption("lookbehind")) {
			if (algorithm != Algorithm.ultimate && algorithm != Algorithm.fast) {
				throw new IllegalArgumentException("--lookbehind parameter can be only used with ultimate or fast algorithm.");
			}
			parameterMap.put(
					SrxTextIterator.MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER,
					Integer.parseInt(commandLine.getOptionValue("lookbehind")));
		}
		if (commandLine.hasOption("buffer-length")) {
			if (commandLine.hasOption('r')) {
				throw new IllegalArgumentException("--buffer-length can be only used when reading text from a stream (--preload option not allowed).");
			}
			parameterMap.put(
					SrxTextIterator.BUFFER_LENGTH_PARAMETER, 
					Integer.parseInt(commandLine.getOptionValue("buffer-length")));
		}
		if (commandLine.hasOption("margin")) {
			if (algorithm != Algorithm.ultimate) {
				throw new IllegalArgumentException("--margin parameter can be only used with ultimate algorithm.");
			}
			parameterMap.put(
					SrxTextIterator.MARGIN_PARAMETER,
					Integer.parseInt(commandLine.getOptionValue("margin")));
		}

		if (profile) {
			System.out.print("    Creating text iterator... ");
		}

		long start = System.currentTimeMillis();

		if (algorithm == Algorithm.accurate) {
			if (text != null) {
				textIterator = new AccurateSrxTextIterator(document, languageCode, text);
			} else {
				throw new IllegalArgumentException("For accurate algorithm preload option (-r) is mandatory.");
			}
		} else if (algorithm == Algorithm.ultimate) {
			if (text != null) {
				textIterator = new SrxTextIterator(document, languageCode, text, parameterMap);
			} else {
				textIterator = new SrxTextIterator(document, languageCode, reader, parameterMap);
			}
		} else if (algorithm == Algorithm.fast) {
			if (text != null) {
				textIterator = new FastTextIterator(document, languageCode, text, parameterMap);
			} else {
				textIterator = new FastTextIterator(document, languageCode, reader, parameterMap);
			}
		} else if (algorithm == Algorithm.scanner) {
			if (text != null) {
				textIterator = new ScannerSrxTextIterator(document, languageCode, text, parameterMap);
			} else {
				textIterator = new ScannerSrxTextIterator(document, languageCode, reader, parameterMap);
			}
		} else {
			throw new IllegalArgumentException("Unknown algorithm: " + algorithm + ".");
		}

		if (profile) {
			System.out.println(System.currentTimeMillis() - start + " ms.");
		}

		return textIterator;
	}

	private void performSegment(CommandLine commandLine,
			TextIterator textIterator, Writer writer, boolean profile)
			throws IOException {

		String beginSegment = commandLine.getOptionValue('b');
		if (beginSegment == null) {
			beginSegment = DEFAULT_BEGIN_SEGMENT;
		}
		String endSegment = commandLine.getOptionValue('e');
		if (endSegment == null) {
			endSegment = DEFAULT_END_SEGMENT;
		}

		if (profile) {
			System.out.print("    Performing segmentation... ");
		}

		long start = System.currentTimeMillis();

		while (textIterator.hasNext()) {
			String segment = textIterator.next();
			writer.write(beginSegment);
			writer.write(segment);
			writer.write(endSegment);
		}

		if (profile) {
			System.out.println(System.currentTimeMillis() - start + " ms.");
		}

	}

	private String preloadText(Reader reader, boolean profile) {
		if (text == null) {
			if (profile) {
				System.out.print("Preloading text... ");
			}
			long start = System.currentTimeMillis();
			text = readAll(reader);
			if (profile) {
				System.out.println(System.currentTimeMillis() - start + " ms.");
			}
		}
		return text;
	}

	private void transform(CommandLine commandLine) throws IOException {

		Reader reader;
		if (commandLine.hasOption('i')) {
			reader = createFileReader(commandLine.getOptionValue('i'));
		} else {
			reader = createStandardInputReader();
		}

		Writer writer;
		if (commandLine.hasOption('o')) {
			writer = createFileWriter(commandLine.getOptionValue('o'));
		} else {
			writer = createStandardOutputWriter();
		}

		String mapRule = commandLine.getOptionValue("m");

		try {
			SrxTransformer transformer = new SrxAnyTransformer();
			Map<String, Object> parameterMap = new HashMap<String, Object>();

			if (mapRule != null) {
				parameterMap.put(Srx1Transformer.MAP_RULE_NAME, mapRule);
			}

			transformer.transform(reader, writer, parameterMap);
		} finally {
			cleanupReader(reader);
			cleanupWriter(writer);
		}
	}

	/**
	 * Cleans up reader, which means closing it if it is not standard input.
	 * Does nothing if reader is null. Catches any exceptions and writes them to
	 * the log.
	 * 
	 * @param reader
	 */
	private void cleanupReader(Reader reader) {
		try {
			if (reader != null && !stdinReader) {
				reader.close();
			}
		} catch (IOException e) {
			log.error("Error cleaning up reader.", e);
		}
	}

	/**
	 * Cleans up writer, which means flushing it and closing it if it is not
	 * standard input. Does nothing if writer is null. Catches any exceptions
	 * and writes them to the log.
	 * 
	 * @param writer
	 */
	private void cleanupWriter(Writer writer) {
		try {
			if (writer != null) {
				writer.flush();
				if (!stdoutWriter) {
					writer.close();
				}
			}
		} catch (IOException e) {
			log.error("Error cleaning up writer.", e);
		}
	}

}
