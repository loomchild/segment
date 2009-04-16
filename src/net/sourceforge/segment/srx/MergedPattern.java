package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents merged splitting pattern.
 * Responsible for merging breaking rules into one large pattern and 
 * creating non breaking rules pattern. 
 * @author loomchild
 */
public class MergedPattern {

	private Pattern breakingPattern;

	private List<Pattern> nonBreakingPatternList;

	public MergedPattern(List<LanguageRule> languageRuleList) {
		
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
		} else {
			// null means that that pattern will not match anything
			// (as empty pattern matches everything).
			this.breakingPattern = null;
		}
	}

	public Pattern getBreakingPattern() {
		return breakingPattern;
	}

	public List<Pattern> getNonBreakingPatternList() {
		return nonBreakingPatternList;
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

}
