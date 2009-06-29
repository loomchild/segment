package net.sourceforge.segment.ui.console;

import static net.sourceforge.segment.util.Util.getFileInputStream;
import static net.sourceforge.segment.util.Util.getFileOutputStream;
import static net.sourceforge.segment.util.Util.getReader;
import static net.sourceforge.segment.util.Util.getResourceStream;
import static net.sourceforge.segment.util.Util.getWriter;
import static net.sourceforge.segment.util.Util.readAll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.sourceforge.segment.SegmentTestSuite;
import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.Version;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.SrxTextIterator;
import net.sourceforge.segment.srx.SrxTransformer;
import net.sourceforge.segment.srx.io.Srx1Transformer;
import net.sourceforge.segment.srx.io.SrxAnyParser;
import net.sourceforge.segment.srx.io.SrxAnyTransformer;
import net.sourceforge.segment.srx.legacy.MergedPatternTextIterator;
import net.sourceforge.segment.srx.legacy.PolengSrxTextIterator;
import net.sourceforge.segment.util.NullWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.junit.internal.runners.TextListener;
import org.junit.runner.JUnitCore;


/**
 * Text user interface to splitter.
 * 
 * @author loomchild
 */
public class Segment {
	
	private enum Algorithm {
		poleng, merge, ultimate;
	}

	public static final String DEFAULT_SRX = "net/sourceforge/segment/res/xml/default.srx";

	public static final String EOLN = System.getProperty("line.separator");
	
	public static final String DEFAULT_BEGIN_SEGMENT = "";
	public static final String DEFAULT_END_SEGMENT = EOLN;
	
	/* These constants apply to text / SRX generation */
	public static final int WORD_LENGTH = 2;
	public static final int SENTENCE_LENGTH = 5;
	
	private Random random;
	private String text;

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

	private Options createOptions() {
		Options options = new Options();
		options.addOption("s", "srx", true, "SRX file.");
		options.addOption("l", "lang", true, "Language code.");
		options.addOption("m", "map", true, "Map rule name in SRX 1.0.");
		options.addOption("b", "begin", true, "Output segment prefix.");
		options.addOption("e", "end", true, "Output segment suffix.");
		options.addOption("a", "algorithm", true, "Algorithm. Can be poleng, merge or ultimate (default).");
		options.addOption("u", "ultimate", false, "Use utlimate algorithm.");
		options.addOption("i", "input", true, "Use given input file instead of standard input.");
		options.addOption("o", "output", true, "Use given output file instead of standard output.");
		options.addOption("t", "transform", false, "Convert old SRX to current version.");
		options.addOption("p", "profile", false, "Print profile information.");
		options.addOption("r", "preload", false, "Preload document into memory before segmentation.");
		options.addOption("2", "twice", false, "Repeat the whole process twice.");
		options.addOption("x", "generate-text", true, "Generate random input with given length in KB.");
		options.addOption("y", "generate-srx", true, "Generate random segmentation rules with given rule count and rule length separated by a comma.");
		options.addOption("z", "test", false, "Test the application by running a test suite.");
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
	
	private void test() {
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener());
        core.run(SegmentTestSuite.class);
	}
	
	private void segment(CommandLine commandLine) throws IOException {

		Reader reader = null;
		Writer writer = null;
		
		try {

			boolean profile = commandLine.hasOption('p');
			boolean twice = commandLine.hasOption('2');
			
			if (twice && !profile) {
				throw new RuntimeException("Can only repeat segmentation twice in profile mode.");
			}

			reader = createTextReader(commandLine, profile, twice);

			writer = createTextWriter(commandLine);

			SrxDocument document = createSrxDocument(commandLine, profile);

			createAndSegment(commandLine, document, reader, writer, profile);

			if (twice) {
				
				reader = createTextReader(commandLine, profile, twice);
				
				createAndSegment(commandLine, document, reader, writer, profile);
				
			}

		} finally {
			
			if (reader != null) {
				reader.close();
			}

			if (writer != null) {
				writer.close();
			}
			
		}
		
	}
	
	private Reader createTextReader(CommandLine commandLine, boolean profile, 
			boolean twice) throws IOException {
		Reader reader;

		if (commandLine.hasOption('x')) {
			reader = createRandomTextReader(commandLine.getOptionValue('x'), 
					profile);
		} else if (commandLine.hasOption('i')) {
			reader = createFileReader(commandLine.getOptionValue('i'));
		} else {
			if (twice) {
				throw new RuntimeException("Cannot read standard input twice. " +
						"Provide an input file or generate input text.");
			}
			reader = createStandardInputReader();
		}
		
		return reader;
	}

	private Reader createStandardInputReader() {
		Reader reader = getReader(System.in);
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

		long start = System.currentTimeMillis();

		if (commandLine.hasOption('y')) {
			if (profile) {
				System.out.print("Generating rules... ");
			}
			String generateSrxOption = commandLine.getOptionValue('y');
			document = generateSrxDocument(generateSrxOption);
		} else {
			String fileName = commandLine.getOptionValue('s');
			String mapRule = commandLine.getOptionValue('m');
			if (profile) {
				System.out.print("Reading rules... ");
			}
			document = readSrxDocument(fileName, mapRule);
		}

		if (profile) {
			System.out.println(System.currentTimeMillis() - start + " ms.");
		}
		
		return document;
	}

	private SrxDocument readSrxDocument(String fileName, String mapRule) 
			throws IOException {
		Reader srxReader;

		if (fileName != null) {
			srxReader = getReader(getFileInputStream(fileName));
		} else {
			srxReader = getReader(getResourceStream(DEFAULT_SRX));
		}

		Map<String, Object> parameterMap = new HashMap<String, Object>();

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
		srxReader.close();

		return document;
	}


	private SrxDocument generateSrxDocument(String generateSrxOption) {
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
			SrxDocument document, Reader reader, Writer writer, 
			boolean profile) throws IOException {
		
		if (profile) {
			System.out.println("Segmenting... ");
		}

		long start = System.currentTimeMillis();

		TextIterator textIterator = createTextIterator(commandLine, 
				document, reader, profile);


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
		
		boolean preload = commandLine.hasOption('r');
		
		if (algorithm == Algorithm.poleng && !preload) {
			throw new IllegalArgumentException("For poleng algorithm preload option (-r) is mandatory.");
		}
		
		if (preload) {
			preloadText(reader, profile);
		}
		
		if (profile) {
			System.out.print("    Creating text iterator... ");
		}

		long start = System.currentTimeMillis();
		
		if (algorithm == Algorithm.poleng) {
			textIterator = new PolengSrxTextIterator(document, languageCode, text);
		} else if (algorithm == Algorithm.ultimate) {
			if (preload) {
				textIterator = new SrxTextIterator(document, languageCode, text);
			} else {
				textIterator = new SrxTextIterator(document, languageCode, reader);
			}
		} else if (algorithm == Algorithm.merge) {
			if (preload) {
				textIterator = new MergedPatternTextIterator(document, languageCode, text);
			} else {
				textIterator = new MergedPatternTextIterator(document, languageCode, reader);
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
				System.out.print("    Preloading text... ");
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
			reader.close();
			writer.close();
			
		}
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
		}
		
	}

}

