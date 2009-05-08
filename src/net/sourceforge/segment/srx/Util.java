package net.sourceforge.segment.srx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	public static final int DEFAULT_FINITE_INFINITY = 100;

	private static final Pattern STAR_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\*");

	private static final Pattern PLUS_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})(?<![\\?\\*\\+]|\\{[0-9],?[0-9]?\\}?\\})\\+");

	private static final Pattern RANGE_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\{\\s*([0-9]+)\\s*,\\s*\\}");

	private static final Pattern CAPTURING_GROUP_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\((?!\\?)");

	/**
	 * Replaces block quotes in regular expressions with normal quotes. For
	 * example "\Qabc\E" will be replace with "\a\b\c".
	 * 
	 * @param pattern
	 *            Pattern.
	 * @return Returns pattern with replaced block quotes.
	 */
	public static String removeBlockQuotes(String pattern) {
		StringBuilder patternBuilder = new StringBuilder();
		boolean quote = false;
		char previousChar = 0;

		for (int i = 0; i < pattern.length(); ++i) {
			char currentChar = pattern.charAt(i);

			if (quote) {
				if (previousChar == '\\' && currentChar == 'E') {
					quote = false;
					// Need to remove "\\" at the end as it has been added
					// in previous iteration.
					patternBuilder.delete(patternBuilder.length() - 2,
							patternBuilder.length());
				} else {
					patternBuilder.append('\\');
					patternBuilder.append(currentChar);
				}
			} else {
				if (previousChar == '\\' && currentChar == 'Q') {
					quote = true;
					// Need to remove "\" at the end as it has been added
					// in previous iteration.
					patternBuilder.deleteCharAt(patternBuilder.length() - 1);
				} else {
					patternBuilder.append(currentChar);
				}
			}

			previousChar = currentChar;
		}

		return patternBuilder.toString();
	}

	/**
	 * Changes unlimited length pattern to limited length pattern. It is done by
	 * replacing "*" and "+" symbols with their finite counterparts - "{0,n}"
	 * and {1,n}. As a side effect block quotes are replaced with normal quotes
	 * by using {@link #removeBlockQuotes(String)}.
	 * 
	 * @param pattern
	 *            Pattern to be finitized.
	 * @param infinity
	 *            n number.
	 * @return Returns limited length pattern.
	 */
	public static String finitize(String pattern, int infinity) {
		String finitePattern = removeBlockQuotes(pattern);
		
		Matcher starMatcher = STAR_PATTERN.matcher(finitePattern);
		finitePattern = starMatcher.replaceAll("{0," + infinity + "}");
		
		Matcher plusMatcher = PLUS_PATTERN.matcher(finitePattern);
		finitePattern = plusMatcher.replaceAll("{1," + infinity + "}");
		
		Matcher rangeMatcher = RANGE_PATTERN.matcher(finitePattern);
		finitePattern = rangeMatcher.replaceAll("{$1," + infinity + "}");
		
		return finitePattern;
	}

	/**
	 * Finitizes pattern with default infinity. {@link #finitize(String, int)}
	 * 
	 * @param pattern
	 *            Pattern to be finitized.
	 * @return Finite pattern.
	 */
	public static String finitize(String pattern) {
		return finitize(pattern, DEFAULT_FINITE_INFINITY);
	}

	/**
	 * Replaces capturing groups with non-capturing groups in the given regular
	 * expression. As a side effect block quotes are replaced with normal quotes
	 * by using {@link #removeBlockQuotes(String)}.
	 * 
	 * @param pattern
	 *            Pattern.
	 * @return Returns modified pattern.
	 */
	public static String removeCapturingGroups(String pattern) {
		String newPattern = removeBlockQuotes(pattern);
		Matcher capturingGroupMatcher = CAPTURING_GROUP_PATTERN
				.matcher(newPattern);
		newPattern = capturingGroupMatcher.replaceAll("(?:");
		return newPattern;
	}

}
