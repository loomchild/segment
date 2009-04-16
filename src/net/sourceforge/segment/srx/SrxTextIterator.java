package net.sourceforge.segment.srx;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rootnode.loomchild.util.exceptions.EndOfStreamException;
import net.rootnode.loomchild.util.io.ReaderCharSequence;
import net.sourceforge.segment.AbstractTextIterator;

/**
 * Represents text iterator that splits text according to SRX rules.
 * 
 * @author loomchild
 */
public class SrxTextIterator extends AbstractTextIterator {

	private List<LanguageRule> languageRuleList;

	private CharSequence text;

	private String segment;

	private Pattern breakingPattern;

	private Matcher breakingMatcher;

	private List<Pattern> nonBreakingPatternList;

	private int startPosition, endPosition;

	/**
	 * Creates text iterator that obtains language rules form given document
	 * using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}.
	 * 
	 * @param document
	 *            Document containing language rules.
	 * @param languageCode
	 *            Language code to select the rules.
	 * @param text
	 *            Text.
	 */
	public SrxTextIterator(SrxDocument document, String languageCode,
			CharSequence text) {
		this.languageRuleList = document.getLanguageRuleList(languageCode);
		this.text = text;
		this.segment = null;
		this.startPosition = 0;
		this.endPosition = 0;
		//If text is empty iterator finishes immediately.
		if (!canReadNextChar()) {
			this.startPosition = text.length();
		}
		initializePatterns();
	}

	/**
	 * Creates streaming text iterator that obtains language rules form given
	 * document using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}. To handle streams uses
	 * ReaderCharSequence, so not all possible regular expressions are accepted.
	 * See {@link ReaderCharSequence} for details.
	 * 
	 * @param document
	 *            Document containing language rules.
	 * @param languageCode
	 *            Language code to select the rules.
	 * @param reader
	 *            Reader from which text will be read.
	 */
	public SrxTextIterator(SrxDocument document, String languageCode,
			Reader reader) {
		this(document, languageCode, new ReaderCharSequence(reader));
	}

	private void initializePatterns() {

		this.nonBreakingPatternList = new ArrayList<Pattern>();

		StringBuilder breakingPatternBuilder = new StringBuilder();

		List<Rule> ruleList = extractRules(languageRuleList);
		List<List<Rule>> ruleGroupList = groupRules(ruleList);

		if (ruleGroupList.size() > 0) {
			for (List<Rule> ruleGroup : ruleGroupList) {
				if (breakingPatternBuilder.length() > 0) {
					breakingPatternBuilder.append('|');
				}
				
				// All breaking rules need to be merged because segmentation
				// need to be done in one pass when text is read from Reader.
				// Breaking rule need to be inside capturing group so
				// it is possible to recognize which breaking rule has been
				// applied during the splitting and know which non-breaking
				// rules to use.
				// Breaking rule cannot contain capturing groups.
				// Capturing groups are replaced with non-capturing groups
				// inside create breaking pattern function.
				String breakingGroupPattern = createBreakingPattern(ruleGroup);
				breakingPatternBuilder.append("(" + breakingGroupPattern + ")");

				// If first rule in the group is breaking then there are
				// no non-breaking rules in the group. In this case null 
				// is appended to non breaking pattern list, because
				// null does not match anything.
				if (!ruleGroup.get(0).isBreaking()) {
					Pattern nonBreakingGroupPattern = 
						Pattern.compile(createNonBreakingPattern(ruleGroup));
					nonBreakingPatternList.add(nonBreakingGroupPattern);
				} else {
					nonBreakingPatternList.add(null);
				}
			}
			this.breakingPattern = Pattern.compile(breakingPatternBuilder
					.toString());
			this.breakingMatcher = breakingPattern.matcher(text);
		} else {
			// null means that that pattern will not match anything
			// (as empty pattern matches everything).
			this.breakingPattern = null;
			this.breakingMatcher = null;
		}
	}

	/**
	 * @param languageRuleList
	 *            Language rule list.
	 * @return Returns merged list of rules form given language rules.
	 */
	private List<Rule> extractRules(List<LanguageRule> languageRuleList) {
		List<Rule> ruleList = new ArrayList<Rule>();
		for (LanguageRule languageRule : languageRuleList) {
			ruleList.addAll(languageRule.getRuleList());
		}
		return ruleList;
	}

	/**
	 * Divides rules to groups where breaking and non-breaking rules cannot be
	 * interlaced.
	 * 
	 * @param ruleList
	 * @return
	 */
	private List<List<Rule>> groupRules(List<Rule> ruleList) {
		List<List<Rule>> ruleGroupList = new ArrayList<List<Rule>>();
		List<Rule> ruleGroup = new ArrayList<Rule>();
		boolean previousBreaking = false;

		for (Rule rule : ruleList) {
			if (rule.isBreaking() && !previousBreaking) {
				ruleGroupList.add(ruleGroup);
			} else if (!rule.isBreaking() && previousBreaking) {
				ruleGroup = new ArrayList<Rule>();
			}
			ruleGroup.add(rule);
			previousBreaking = rule.isBreaking();
		}

		return ruleGroupList;
	}

	/**
	 * Merges all breaking rules on list into one pattern.
	 * 
	 * @param ruleList
	 *            List of rules
	 * @return Returns pattern.
	 */
	private String createBreakingPattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();

		for (Rule rule : ruleList) {
			if (rule.isBreaking()) {
				if (patternBuilder.length() > 0) {
					patternBuilder.append('|');
				}
				// Capturing groups need to be removed from patterns as
				// they will interfere with capturing group order
				// which is used to recognize which breaking rule has been
				// applied and decide which non-breaking rules to use.
				String beforePattern = Util.removeCapturingGroups(rule
						.getBeforePattern());
				String afterPattern = Util.removeCapturingGroups(rule
						.getAfterPattern());
				if (beforePattern.length() > 0) {
					patternBuilder.append("(?:" + beforePattern);
				}
				if (afterPattern.length() > 0) {
					patternBuilder.append("(?=" + afterPattern + ")");
				}
				if (beforePattern.length() > 0) {
					patternBuilder.append(")");
				}
			}
		}

		return patternBuilder.toString();
	}

	/**
	 * Creates non breaking pattern by merging given rules.
	 * 
	 * @param ruleList
	 *            Rule list.
	 * @return Non breaking pattern.
	 */
	private String createNonBreakingPattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();

		for (Rule rule : ruleList) {
			if (!rule.isBreaking()) {
				if (patternBuilder.length() > 0) {
					patternBuilder.append('|');
				}
				// As Java does not allow infinite length patterns
				// (containing * or +) in lookbehind, they need to be shortened.
				String beforePattern = Util.finitize(rule.getBeforePattern());
				String afterPattern = rule.getAfterPattern();
				patternBuilder.append("(?:");
				if (beforePattern.length() > 0) {
					patternBuilder.append("(?<=" + beforePattern + ")");
				}
				if (afterPattern.length() > 0) {
					patternBuilder.append("(?=" + afterPattern + ")");
				}
				patternBuilder.append(")");
			}
		}

		return patternBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String next() {
		if (hasNext()) {
			boolean found = false;
			if (breakingMatcher != null) {
				while (!found && find(breakingMatcher)) {
					endPosition = breakingMatcher.end();
					// When there's more than one breaking rule at the given
					// place only the first is matched, the rest is skipped.
					// So if position is not increasing the new rules are
					// applied in the same place as previously matched rule.
					if (endPosition > startPosition) {
						// Index of current capturing group
						int groupIndex = 1;
						found = true;

						for (Pattern nonBreakingPatern : nonBreakingPatternList) {

							// Null non breaking pattern does not match anything
							if (nonBreakingPatern != null) {
								Matcher nonBreakingMatcher = nonBreakingPatern
										.matcher(text);
								nonBreakingMatcher.useTransparentBounds(true);
								nonBreakingMatcher.region(endPosition,
										endPosition + 1);
								found = !nonBreakingMatcher.lookingAt();
							}

							// Break when non-breaking rule matches or
							// the current group index is equal to breaking
							// rule index, so further non-breaking rules
							// do not apply to this breaking rule.
							String group = breakingMatcher.group(groupIndex);
							if (!found || group != null) {
								break;
							}

							++groupIndex;
							
						}

					}
				}
			}
			if (!found || !canReadNextChar()) {
				endPosition = text.length();
			}
			segment = text.subSequence(startPosition, endPosition).toString();
			startPosition = endPosition;
			return segment;
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if next character can be read. It is needed as
	 * {@link net.rootnode.loomchild.util.io.ReaderCharSequence} throws
	 * EndOfStreamException at the end.
	 * @return True if next character is available.
	 */
	private boolean canReadNextChar() {
		try {
			if (endPosition < text.length()) {
				text.charAt(endPosition);
				return true;
			} else {
				return false;
			}
		} catch (EndOfStreamException e) {
			return false;
		}
	}

	/**
	 * Searches for the next pattern occurrence and return true if it has been
	 * found, false otherwise. It is needed as
	 * {@link net.rootnode.loomchild.util.io.ReaderCharSequence} throws
	 * EndOfStreamException at the end.
	 * 
	 * @param matcher
	 *            Matcher which will perform search.
	 * @return Returns true when the matcher found the pattern.
	 */
	private boolean find(Matcher matcher) {
		try {
			return matcher.find();
		} catch (EndOfStreamException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return (startPosition < text.length());
	}

}
