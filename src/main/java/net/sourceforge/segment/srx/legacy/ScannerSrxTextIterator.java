package net.sourceforge.segment.srx.legacy;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import net.sourceforge.segment.AbstractTextIterator;
import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.util.Util;

/** 
 * <p>
 * Quick and Dirty implementation of {@link TextIterator} using {@link Scanner}.
 * </p>
 * 
 * <p>
 * Preliminary tests showed that it requires between 50% and 100% more time
 * to complete than default text iterator. 
 * Probably the reason is slow matching of exception rules,
 * but also splitting break-rule-only is slower.
 * </p>
 * 
 * <p>
 * This implementation is also not able to solve overlapping rules, like 
 * other one-big-pattern-scan iterators and there seems to be no easy solution.
 * Although this should not happen in input patterns, in large SRX file 
 * using cascading it is very easy to miss this.
 * </p>
 * 
 * <p>
 * One solution could be sorting patterns by length, but this is sometimes 
 * impossible to do. For example:<br/>
 * Rules are "(ab)+" and "a(b)+"<br/>
 * Inputs are "ababx" and "abbbx"<br/>
 * For first input order of exception rules should be reversed for the text
 * to be split as early as possible, but for the second input it shouldn't.
 * The solution could be to use reluctant quantifiers instead of greedy ones, 
 * but that is changing the input patterns provided by user and therefore 
 * is undesirable.
 * </p>
 *  
 * @author loomchild
 */
public class ScannerSrxTextIterator extends AbstractTextIterator {

	private Scanner scanner;
	
	private Map<Pattern, Pattern> exceptionMap;
	
	private boolean noBreakRules;
	
	public ScannerSrxTextIterator(SrxDocument document, String languageCode, 
			String text, Map<String, Object> parameterMap) {
		this(document, languageCode, new Scanner(text));
	}
	
	public ScannerSrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader, Map<String, Object> parameterMap) {
		this(document, languageCode, new Scanner(reader));
	}
	
	private ScannerSrxTextIterator(SrxDocument document, String languageCode,
			Scanner scanner) {
		List<LanguageRule> languageRuleList = 
			document.getLanguageRuleList(languageCode);
		String separator = 
			createSeparator(languageRuleList);
		this.scanner = scanner;
		this.scanner.useDelimiter(Pattern.compile(separator));
		this.exceptionMap = createExceptions(languageRuleList);
	}
	
	private String createSeparator(List<LanguageRule> languageRuleList) {
		this.noBreakRules = true;
		StringBuilder separator = new StringBuilder();
		for (LanguageRule languageRule : languageRuleList) {
			for (Rule rule : languageRule.getRuleList()) {
				if (rule.isBreak()) {
					String regex = createBreakRegexLookahead(rule);
					if (regex.length() > 0) {
						separator.append(regex);
						separator.append("|");
					}
					this.noBreakRules = false;
				}
			}
		}
		if (separator.length() > 0) {
			separator.deleteCharAt(separator.length() - 1);
			return "(?=" + separator.toString() + ")";
		} else {
			return "";
		}
	}
	
	private Map<Pattern, Pattern> createExceptions(
			List<LanguageRule> languageRuleList) {
		Map<Pattern, Pattern> result = 
			new LinkedHashMap<Pattern, Pattern>();
		// Needs to be null first to distinguish empty rule (matching everything)
		// from no rules (matching nothing).
		StringBuilder exception = null;
		for (LanguageRule languageRule : languageRuleList) {
			for (Rule rule : languageRule.getRuleList()) {
				if (rule.isBreak()) {
					Pattern pattern = Pattern.compile(createBreakRegexNoLookahead(rule));
					
					Pattern exceptionPattern;
					if (exception != null) {
						exceptionPattern = Pattern.compile(exception.toString());
					} else {
						exceptionPattern = null;
					}
					result.put(pattern, exceptionPattern);
				} else {
					if (exception == null) {
						exception = new StringBuilder();
					} else if (exception.length() > 0) {
						exception.append("|");
					}
					exception.append(createExceptionRegex(rule));
				}
			}
		}
		return result;
	}
	
	private String createBreakRegexLookahead(Rule rule) {
		StringBuilder regex = new StringBuilder();
		if (rule.getAfterPattern().length() > 0 || 
				rule.getBeforePattern().length() > 0) {
			regex.append("(?:");
			if (rule.getBeforePattern().length() > 0) {
				regex.append(rule.getBeforePattern());
			}
			if (rule.getAfterPattern().length() > 0) {
				regex.append(rule.getAfterPattern()); 
			}
			regex.append(")");
		}
		return regex.toString();
	}

	private String createBreakRegexNoLookahead(Rule rule) {
		StringBuilder regex = new StringBuilder();
		if (rule.getAfterPattern().length() > 0 || 
				rule.getBeforePattern().length() > 0) {
			regex.append("\\G(");
			if (rule.getBeforePattern().length() > 0) {
				regex.append(rule.getBeforePattern());
			}
			regex.append(")(?="); 
			if (rule.getAfterPattern().length() > 0) {
				regex.append(rule.getAfterPattern()); 
			}
			regex.append(")");
		}
		return regex.toString();
	}

	private String createExceptionRegex(Rule rule) {
		StringBuilder regex = new StringBuilder();
		if (rule.getAfterPattern().length() > 0 || 
				rule.getBeforePattern().length() > 0) {
			regex.append("(?:(?<=");
			if (rule.getBeforePattern().length() > 0) {
				regex.append(Util.finitize(rule.getBeforePattern(), 100));
			}
			regex.append(")\\G(?="); 
			if (rule.getAfterPattern().length() > 0) {
				regex.append(rule.getAfterPattern()); 
			}
			regex.append("))");
		}
		return regex.toString();
	}
	
	public boolean hasNext() {
		return scanner.hasNext();
	}

	public String next() {
		StringBuilder segment = new StringBuilder();
		
		do {
			segment.append(scanner.next());
		} while (scanner.hasNext() && (noBreakRules || isException(segment)));

		return segment.toString();
	}

	private boolean isException(StringBuilder segment) {
		if (exceptionMap.size() > 0) {
			for (Map.Entry<Pattern, Pattern> entry : exceptionMap.entrySet()) {
				String result = scanner.findWithinHorizon(entry.getKey(), 100);
				if (result != null) {
					segment.append(result);
					Pattern pattern = entry.getValue();
					if (pattern != null) {
						if (scanner.findWithinHorizon(pattern, 1) != null) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			throw new IllegalStateException("No matching rule found.");
		} else {
			return false;
		}
	}
	
}
