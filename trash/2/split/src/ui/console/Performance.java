package ui.console;



import java.util.Random;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import split.srx.LanguageRule;
import split.srx.Rule;
import split.srx.SplitPattern;
import split.srx.SrxSplitter;

/**
 * Klasa testująca wydajność dzielenia na segmenty poprzez wygenerowanie 
 * losowego tekstu i reguł i zmierzenia czasu działania splittera.
 *
 * @author loomchild
 */
public class Performance {

	public static final int WORD_LENGTH = 5;
	
	public static final int SENTENCE_LENGTH = 5;
	
	public static final int MAX_WORD_NUMBER = (int)Math.pow(10, WORD_LENGTH);
	
	public static final int DEFAULT_TEXT_LENGTH = 10000;
	
	public static final int DEFAULT_RULE_COUNT = 1;
	
	public static final int DEFAULT_RULE_LENGTH = 1;
	
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
		options.addOption("t", "textlen", true, "Długość tekstu");
		options.addOption("r", "rulecount", true, "Ilość reguł");
		options.addOption("l", "rulelen", true, "Długość reguły");
		options.addOption("h", "help", false, "Wyświetl tą pomoc");
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
			if (textLength < 0) {
				throw new Exception("Too short text: " + textLength);
			}
		} else {
			textLength = DEFAULT_TEXT_LENGTH;
		}
		int ruleCount;
		String ruleCountOption = commandLine.getOptionValue("r");
		if (ruleCountOption != null) {
			ruleCount = Integer.parseInt(ruleCountOption);
			if (ruleCount < 0) {
				throw new Exception("Rule count must be positive: " 
						+ ruleCount);
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

		System.out.println("Generuje regułe języka");
		LanguageRule languageRule = generateLanguageRule(ruleCount, ruleLength);
		System.out.println("Generuje tekst");
		String text = generateText(textLength); 
		System.out.println("Kompiluje regułe dzielącą");
		SplitPattern splitPattern = new SplitPattern(languageRule);
		System.out.println("Tworze splitter");
		SrxSplitter splitter = new SrxSplitter(splitPattern, text);

		System.out.println("Rozpoczynam dzielenie");
		long startTime = System.currentTimeMillis();
		while (splitter.hasNext()) {
			String segment = splitter.next();
		}
		long endTime = System.currentTimeMillis();
		float time = ((endTime - startTime) / 100) / 10.0f;
		System.out.println("Upłyneło " + time + " sek.");
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
	
	private LanguageRule generateLanguageRule(int ruleCount, int ruleLenght) {
		LanguageRule languageRule = new LanguageRule("");
		//Dodaj reguły
		for (int i = 0; i < ruleCount; ++i) {
			Rule rule = generateRule(ruleLenght);
			languageRule.addRule(rule);
		}
		//Dodaje regułe kończącą zdanie
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
	
}
