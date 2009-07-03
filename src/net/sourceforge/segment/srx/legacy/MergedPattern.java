package net.sourceforge.segment.srx.legacy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.Rule;
import net.sourceforge.segment.util.Util;

/**
 * Represents merged splitting pattern.
 * Responsible for merging breaking rules into one large pattern and 
 * creating non breaking rules pattern. 
 * @author loomchild
 */
public class MergedPattern {

	private Pattern breakingPattern;

	private List<Pattern> nonBreakingPatternList;
	
	private List<Integer> breakingRuleIndexList;

	public MergedPattern(List<LanguageRule> languageRuleList) {
		
		StringBuilder breakingPatternBuilder = new StringBuilder();
		
		this.nonBreakingPatternList = new ArrayList<Pattern>();
		
		// This list contains indexes of last breaking rules that occur before
		// given non breaking pattern on the list. 
		// It has the same size as nonBreakingPatternList.
		// It is needed to recognize which non breaking rules to use for
		// given braking rule.
		this.breakingRuleIndexList = new ArrayList<Integer>();
		
		// Number or breaking rules already added to breaking pattern.
		int breakingRuleIndex = 0;

		List<Rule> ruleList = extractRules(languageRuleList);
		List<List<Rule>> ruleGroupList = groupRules(ruleList);

		for (List<Rule> ruleGroup : ruleGroupList) {
			if (ruleGroup.get(0).isBreak()) {
				
				if (breakingPatternBuilder.length() > 0) {
					breakingPatternBuilder.append('|');
				}

				// All breaking rules need to be merged because segmentation
				// need to be done in one pass when text is read from Reader.
				String breakingGroupPattern = createBreakingPattern(ruleGroup);
				breakingPatternBuilder.append(breakingGroupPattern);

				// Increase current braking rule index.
				breakingRuleIndex += ruleGroup.size();

			} else {
				
				// Add non breaking pattern
				Pattern nonBreakingGroupPattern = 
					Pattern.compile(createNonBreakingPattern(ruleGroup));
				nonBreakingPatternList.add(nonBreakingGroupPattern);

				// Add the index of last breaking rule before given 
				// non breaking pattern.
				breakingRuleIndexList.add(breakingRuleIndex);

			}
		}		

		if (breakingPatternBuilder.length() > 0) {
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

	/**
	 * Returns all applicable non breaking rules when breaking rule with a
	 * given number was matched (non breaking rules that occur before
	 * given breaking rule in SRX file).
	 * @param breakingRuleIndex
	 * @return Active non breaking patterns for a given breaking rule
	 */
	public List<Pattern> getNonBreakingPatternList(int breakingRuleIndex) {
		
		List<Pattern> result = new ArrayList<Pattern>();
		
		Iterator<Pattern> patternIterator = nonBreakingPatternList.iterator();
		
		for (int currentBreakingRuleIndex : breakingRuleIndexList) {
			if (currentBreakingRuleIndex >= breakingRuleIndex) {
				break;
			}
			result.add(patternIterator.next());
		}
		
		return result;
	
	}

	/**
	 * @param languageRuleList
	 * @return merged list of rules form given language rules
	 */
	private List<Rule> extractRules(List<LanguageRule> languageRuleList) {
		List<Rule> ruleList = new ArrayList<Rule>();
		for (LanguageRule languageRule : languageRuleList) {
			ruleList.addAll(languageRule.getRuleList());
		}
		return ruleList;
	}

	/**
	 * Divides rules to groups where all rules in the same group are 
	 * either breaking or non breaking. Does not change rule order.
	 * 
	 * @param ruleList
	 * @return list of grouped rules
	 */
	private List<List<Rule>> groupRules(List<Rule> ruleList) {
		List<List<Rule>> ruleGroupList = new ArrayList<List<Rule>>();

		List<Rule> ruleGroup = null;
		Rule previousRule = null;
		
		for (Rule rule : ruleList) {
			if (previousRule == null ||
					rule.isBreak() != previousRule.isBreak()) {
				ruleGroup = new ArrayList<Rule>();
				ruleGroupList.add(ruleGroup);
			}
			ruleGroup.add(rule);
			previousRule = rule;
		}

		return ruleGroupList;
	}

	/**
	 * Merges all breaking rules on list into one pattern.
	 * 
	 * @param ruleList
	 * @return breaking pattern
	 */
	private String createBreakingPattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();

		for (Rule rule : ruleList) {
			if (patternBuilder.length() > 0) {
				patternBuilder.append('|');
			}
			// Capturing groups need to be removed from patterns as
			// they will interfere with capturing group order
			// which is used to recognize which breaking rule has been
			// applied and decide which non-breaking rules to use.
			String beforePattern = 
				Util.removeCapturingGroups(rule.getBeforePattern());
			String afterPattern = 
				Util.removeCapturingGroups(rule.getAfterPattern());
			// Whore pattern would be in lookahead because alternative 
			// behaves differently in lookahead - first matching not first
			// in order is returned first. For example:
			// Input: "aaa"
			// Pattern "aaa|aa" matches "aaa", but pattern "aa|aaa" matches "aa".
			// Pattern "(?=aaa|aa)" always matches "aa". 
			patternBuilder.append("(?=");
			
			patternBuilder.append(beforePattern);
			
			// This will be after break point. 
			patternBuilder.append("()");
			
			patternBuilder.append(afterPattern);
			
			patternBuilder.append(")");
		}

		return patternBuilder.toString();
	}

	/**
	 * Creates non breaking pattern by merging given rules.
	 * 
	 * @param ruleList
	 * @return Non breaking pattern
	 */
	private String createNonBreakingPattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();

		for (Rule rule : ruleList) {
			if (patternBuilder.length() > 0) {
				patternBuilder.append('|');
			}
			// As Java does not allow infinite length patterns
			// in lookbehind, before pattern need to be shortened.
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

		return patternBuilder.toString();
	}

}
