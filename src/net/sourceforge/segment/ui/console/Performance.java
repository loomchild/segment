package net.sourceforge.segment.ui.console;

import java.util.Random;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.LegacySrxTextIterator;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxTextIterator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Test splitting performance on random generated text and rules.
 * 
 * @author loomchild
 */
public class Performance {

	public static final int WORD_LENGTH = 5;

	public static final int SENTENCE_LENGTH = 5;

	public static final int MAX_WORD_NUMBER = (int) Math.pow(10, WORD_LENGTH);

	public static final int DEFAULT_TEXT_LENGTH = 100;

	public static final int DEFAULT_RULE_COUNT = 10;

	public static final int DEFAULT_RULE_LENGTH = 10;

	private Random random;

	public static void main(String[] args) {
		try {
			Performance performance = new Performance();
			performance.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Performance() {
		this.random = new Random();
	}

	private Options createOptions() {
		Options options = new Options();
		options.addOption("t", "textlen", true, "Text length in thousands of bytes.");
		options.addOption("r", "rulecount", true, "Rule count.");
		options.addOption("l", "rulelen", true, "Single rule length.");
		options.addOption("o", "old", false, "Use old legacy algorithm.");
		options.addOption("h", "help", false, "Print this help.");
		return options;
	}

	private void run(String[] args) throws Exception {
		Options options = createOptions();
		HelpFormatter helpFormatter = new HelpFormatter();
		BasicParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			helpFormatter.printHelp("performance", options);
			System.exit(1);
		}
		if (commandLine.hasOption('h')) {
			helpFormatter.printHelp("performance", options);
			System.exit(0);
		}
		int textLength;
		String textLengthOption = commandLine.getOptionValue("t");
		if (textLengthOption != null) {
			textLength = Integer.parseInt(textLengthOption);
			if (textLength <= 0) {
				throw new Exception("Too short text: " + textLength + "K.");
			}
		} else {
			textLength = DEFAULT_TEXT_LENGTH;
		}
		int ruleCount;
		String ruleCountOption = commandLine.getOptionValue("r");
		if (ruleCountOption != null) {
			ruleCount = Integer.parseInt(ruleCountOption);
			if (ruleCount < 0) {
				throw new Exception("Rule count must be positive: " + ruleCount);
			}
		} else {
			ruleCount = DEFAULT_RULE_COUNT;
		}
		int ruleLength;
		String ruleLengthOption = commandLine.getOptionValue("l");
		if (ruleLengthOption != null) {
			ruleLength = Integer.parseInt(ruleLengthOption);
		} else {
			ruleLength = DEFAULT_RULE_LENGTH;
			if (ruleLength < 1) {
				throw new Exception("Rule lenght must at lest 1 but is: "
						+ ruleLength);
			}
		}
		boolean legacy = commandLine.hasOption("o");

		System.out.println("Settings: " +
				"text lenght = " + textLength + "K, " +
				"rule count = " + ruleCount + ", " +
				"rule length = " + ruleLength + ".");

		Profiler profiler;

		System.out.print("Generating rules... ");
		profiler = new Profiler();
		SrxDocument document = generateSrxDocument(ruleCount, ruleLength);
		System.out.println(profiler.time() + " ms.");
		
		System.out.print("Generating text... ");
		profiler = new Profiler();
		String text = generateText(textLength * 1000);
		System.out.println(profiler.time() + " ms.");
		
		split("Splitting...", legacy, document, text);

		split("Splitting again...", legacy, document, text);
	
	}
	
	private void split(String message, boolean legacy, 
			SrxDocument document, String text) {
		Profiler profiler;
		System.out.println(message);
		
		System.out.print("  Creating splitter... ");
		profiler = new Profiler();
		TextIterator textIterator = createTextIterator(legacy, document, text);
		long createTime = profiler.time();
		System.out.println(createTime + " ms.");
		
		System.out.print("  Performing split... ");
		profiler = new Profiler();
		performSplit(textIterator);
		long splitTime = profiler.time();
		System.out.println(splitTime + " ms.");
		
		long totalTime = createTime + splitTime;
		System.out.println(totalTime + " ms.");
		
	}

	private String generateText(int length) {
		int wordCount = length / (WORD_LENGTH + 1);
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
		int number = random.nextInt(MAX_WORD_NUMBER);
		number += MAX_WORD_NUMBER;
		String word = Integer.toString(number);
		return word.substring(1);
	}

	private SrxDocument generateSrxDocument(int ruleCount, int ruleLenght) {
		SrxDocument srxDocument = new SrxDocument();
		LanguageRule languageRule = generateLanguageRule(ruleCount, ruleLenght);
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
	
	private TextIterator createTextIterator(boolean legacy, 
			SrxDocument document, String text) {
		TextIterator textIterator;
		if (!legacy) {
			textIterator = new SrxTextIterator(document, "", text);
		} else {
			textIterator = new LegacySrxTextIterator(document, "", text);
		}
		return textIterator;
	}

	private void performSplit(TextIterator textIterator) {
		while (textIterator.hasNext()) {
			textIterator.next();
		}
	}
	
}

class Profiler {
	
	private long start;
	
	public Profiler() {
		this.start = System.currentTimeMillis();
	}
	
	public long time() {
		long end = System.currentTimeMillis();
		return end - start;
	}
	
}
