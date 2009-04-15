package ui.console;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import split.simple.SimpleSplitter;
import split.splitter.Splitter;
import split.srx.Document;
import split.srx.LanguageRule;
import split.srx.LanguageRuleNotFound;
import split.srx.MapRule;
import split.srx.Parser;
import split.srx.SplitPattern;
import split.srx.SrxSplitter;

/**
 * Klasa uruchamiająca dzielenie tekstu na segmenty. Odpowiada za obsługe 
 * parametrów wejściowych i ururchomienie splittera.
 *
 * @author loomchild
 */
public class Split {
	
	public static final String DEFAULT_BEGIN_SEGMENT = "";
	public static final String DEFAULT_END_SEGMENT = "";
	
	public static void main(String[] args) {
		try {
			Split main = new Split();
			main.run(args);
		} catch (Exception e) {
			System.out.println("Błąd: " + e.getMessage());
		}
	}
	
	private Options createOptions() {
		Options options = new Options();
		options.addOption("s", "srx", true, "Plik SRX");
		options.addOption("l", "lang", true, "Kod języka tekstu wejściowego");
		options.addOption("m", "map", true, "Reguła mapowania (domyślnie " +
				"jedyna reguła)");
		options.addOption("r", "rule", true, "Reguła języka, ignoruje język");
		options.addOption("b", "begin", true, "Zaznaczenie początku segmentu " +
				"na wyjściu");
		options.addOption("e", "end", true, "Zaznaczenie końca segmentu " +
				"na wyjściu");
		options.addOption("v", "verbose", false, "Wyświetlaj komunikaty " +
				"diagnostyczne");
		options.addOption("h", "help", false, "Wyświetl tą pomoc");
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
			helpFormatter.printUsage(err, 80, "split --help");
			System.exit(1);
		}
		if (commandLine.hasOption('h')) {
			helpFormatter.printHelp("split", options);
			System.exit(0);
		}
		boolean verbose = commandLine.hasOption('v');
		Reader input = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter output = new PrintWriter(System.out);
		String beginSegment = commandLine.getOptionValue('b');
		if (beginSegment == null) {
			beginSegment = DEFAULT_BEGIN_SEGMENT;
		}
		String endSegment = commandLine.getOptionValue('e');
		if (endSegment == null) {
			endSegment = DEFAULT_END_SEGMENT;
		}
		Splitter splitter;
		String srxFileName = commandLine.getOptionValue('s');
		if (srxFileName != null) {
			Document document = Parser.getInstance().parse(srxFileName);
			String languageRuleName = commandLine.getOptionValue('r');
			LanguageRule languageRule = null;
			if (languageRuleName != null) {
				languageRule = document.getLanguageRule(languageRuleName);
			} else {
				MapRule mapRule = null;
				String mapRuleName = commandLine.getOptionValue('m');
				if (mapRuleName != null) {
					mapRule = document.getMapRule(mapRuleName);
				} else {
					mapRule = document.getSingletonMapRule();
				}
				String languageCode = commandLine.getOptionValue('l');
				if (languageCode == null) {
					languageCode = "";
				}
				languageRule = 
						mapRule.getLanguageMap(languageCode).getLanguageRule();
			}
			if (verbose) {
				System.err.println("Używam reguły " + languageRule.getName());
			}
			SplitPattern splitPattern = new SplitPattern(languageRule);
			splitter = new SrxSplitter(splitPattern, input);
		} else {
			splitter = new SimpleSplitter(input);
		}
		while (splitter.hasNext()) {
			String segment = splitter.next();
			output.print(beginSegment);
			output.print(segment);
			output.println(endSegment);
		}
		input.close();
		output.close();
	}
	
}
