package net.sourceforge.segment.ui.console;

import static net.rootnode.loomchild.util.io.Util.getReader;
import static net.rootnode.loomchild.util.io.Util.getWriter;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.segment.srx.SrxTransformer;
import net.sourceforge.segment.srx.io.Srx1Transformer;
import net.sourceforge.segment.srx.io.SrxAnyTransformer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Text user interface to SRX transformer.
 * 
 * @author loomchild
 */
public class Transform {

	public static void main(String[] args) {
		try {
			Transform main = new Transform();
			main.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private Options createOptions() {
		Options options = new Options();
		options.addOption("m", "map", true, "Map rule name to keep (SRX 1.0).");
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
			helpFormatter.printUsage(err, 80, "transform --help");
			System.exit(1);
		}
		if (commandLine.hasOption('h')) {
			helpFormatter.printHelp("transform", options);
			System.exit(0);
		}

		Reader reader = getReader((System.in));
		Writer writer = getWriter((System.out));

		SrxTransformer transformer = new SrxAnyTransformer();
		Map<String, Object> parameterMap = new HashMap<String, Object>();

		String mapRule = commandLine.getOptionValue("m");
		if (mapRule != null) {
			parameterMap.put(Srx1Transformer.MAP_RULE_NAME, mapRule);
		}

		transformer.transform(reader, writer, parameterMap);

		reader.close();
		writer.close();
	}

}
