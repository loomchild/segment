package split;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import simple.SimpleSplitter;
import srx.Document;
import srx.LanguageRule;
import srx.MapRule;
import srx.Parser;
import srx.SrxSplitter;

public class Main {

	public static final String USAGE =
		"Złe parametry. Uzyj opcji --help aby uzyskać więcej informacji.";

	public static final String HELP = 
		"Składnia: split [opcje]\n" +
		"Dzieli tekst ze standardowego wejścia na segmenty i zwraca wynik na standardowe wyjście\n" +
		"Opcje:\n" +
		"-s, --srx plik   \t Plik SRX\n" +
		"-l, --lang język\t Kod języka tekstu wejściowego\n" +
		"-m, --map reguła\t Reguła mapowania (domyślnie \"" 
			+ Document.DEFAULT_RULE_NAME + "\")\n" +
		"-r, --rule reguła\t Reguła języka, ignoruje język " +
			"(domyślnie \""	+ Document.DEFAULT_RULE_NAME + "\")\n" +
		"-i, --input plik\t Plik wejściowy zamiast standardowego wejścia\n" +
		"-o, --output plik\t Plik wyjściowy zamiast standardowego wyjścia\n" +
		"-e, --separ separator\t Separator segmentów na wyjściu " +
			"(domyślnie \"\\n\")\n" +
		"-h, --help      \t Wyświetl tą pomoc";
		
	
	public static void main(String[] args) {
		try {
			run(args);
		} catch (Exception e) {
			System.out.println("Błąd: " + e.getMessage());
		}
	}
	
	private static Map<String, String> parseArgs(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		int i = 0;
		while ((i < args.length) && args[i].startsWith("-")) {
			String arg = args[i];
			++i;
			String option = "";
			if (i < args.length && !(args[i].startsWith("-"))) {
				option = args[i];
				++i;
			}
			map.put(arg, option);
		}
		String parameters = "";
		while (i < args.length) {
			parameters += args[i];
			++i;
			if (i < args.length) {
				parameters += " ";
			}
		}
		map.put("", parameters);
		return map;
	}
	
	private static String getOption(String shortName, String longName, 
			Map<String, String> map) {
		String option = map.get(shortName);
		if (option == null) {
			option = map.get(longName);
		}
		return option;
	}
	
	private static void run(String[] args) throws Exception {
		Map<String, String> map = parseArgs(args);
		String help = getOption("-h", "--help", map);
		if (help != null) {
			System.out.println(HELP);
			System.exit(0);
		}
		String separator = getOption("-e", "--separ", map);
		if (separator == null) {
			separator = "\n";
		}
		Reader input;
		String inputFileName = getOption("-i", "--input", map);
		if (inputFileName != null) {
			input = new BufferedReader(new FileReader(inputFileName));
		} else {
			input = new BufferedReader(new InputStreamReader(System.in));
		}
		PrintWriter output;
		String outputFileName = getOption("-o", "--output", map);
		if (outputFileName != null) {
			output = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
		} else {
			output = new PrintWriter(System.out);
		}
		Splitter splitter;
		String srxFileName = getOption("-s", "--srx", map);
		if (srxFileName != null) {
			Document document = Parser.getInstance().parse(srxFileName);
			String languageRuleName = getOption("-r", "--rule", map);
			LanguageRule languageRule = null;
			if (languageRuleName != null) {
				languageRule = document.getLanguageRule(languageRuleName);
			} else {
				MapRule mapRule = null;
				String mapRuleName = getOption("-m", "--map", map);
				if (mapRuleName != null) {
					mapRule = document.getMapRule(mapRuleName);
				} else {
					mapRule = document.getDefaultMapRule();
				}
				String languageCode = getOption("-l", "--lang", map);
				if (languageCode != null) {
					languageRule = mapRule.getLanguageRule(languageCode);
				} else {
					languageRule = document.getDefaultLanguageRule();
				}
			}
			splitter = new SrxSplitter(languageRule, input);
		} else {
			splitter = new SimpleSplitter(input);
		}
		while (splitter.hasNext()) {
			String segment = splitter.next();
			output.print(segment);
			output.print(separator);
		}
		input.close();
		output.close();
	}
	
}
