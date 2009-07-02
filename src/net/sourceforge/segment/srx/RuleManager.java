package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.segment.util.Util;

/**
 * Represents segmentation rules manager.
 * Responsible for constructing and storing breaking and non-breaking rules.
 * 
 * @author loomchild
 */
public class RuleManager {
	
	private SrxDocument document;

	private List<Rule> breakingRuleList;
	
	private Map<Rule, Pattern> nonBreakingPatternMap;
	
	/**
	 * Constructor. Responsible for retrieving rules from SRX document for
	 * given language code, constructing patterns and storing them in 
	 * quick accessible format.
	 * Adds breaking rules to {@link #breakingRuleList} and constructs
	 * corresponding non breaking patterns in {@link #nonBreakingPatternMap}.  
	 * Uses document cache to store rules and patterns. 
	 * @param document SRX document
	 * @param languageCode
	 */
	@SuppressWarnings("unchecked")
	public RuleManager(SrxDocument document, String languageCode) {
		this.document = document;
		
		List<LanguageRule> languageRuleList = 
			document.getLanguageRuleList(languageCode);
		
		Object[] cachedPatterns = 
			document.getCache().get(languageRuleList, Object[].class);
		
		if (cachedPatterns != null) {
		
			this.breakingRuleList = (List<Rule>)cachedPatterns[0];
			this.nonBreakingPatternMap = (Map<Rule, Pattern>)cachedPatterns[1];
		
		} else {
		
			this.breakingRuleList = new ArrayList<Rule>();
			this.nonBreakingPatternMap = new HashMap<Rule, Pattern>();

			StringBuilder nonBreakingPatternBuilder = new StringBuilder();
			
			for (LanguageRule languageRule : languageRuleList) {
				for (Rule rule : languageRule.getRuleList()) {

					if (rule.isBreaking()) {
					
						breakingRuleList.add(rule);
						
						Pattern nonBreakingPattern;
						
						if (nonBreakingPatternBuilder.length() > 0) {
							String nonBreakingPatternString = 
								nonBreakingPatternBuilder.toString();
							nonBreakingPattern = 
								Util.compile(document, nonBreakingPatternString);
						} else {
							nonBreakingPattern = null;
						}

						nonBreakingPatternMap.put(rule, nonBreakingPattern);
					
					} else {
					
						if (nonBreakingPatternBuilder.length() > 0) {
							nonBreakingPatternBuilder.append('|');
						}

						String patternString = createNonBreakingPatternString(rule);
						
						nonBreakingPatternBuilder.append(patternString);
				
					}
				
				}
			}

			cachedPatterns = new Object[] {
				this.breakingRuleList, this.nonBreakingPatternMap
			};
			document.getCache().put(languageRuleList, cachedPatterns);
			
		}
		
	}
	
	/**
	 * @return breaking rule list
	 */
	public List<Rule> getBreakingRuleList() {
		return breakingRuleList;
	}
	
	/**
	 * @param breakingRule
	 * @return non breaking pattern corresponding to give breaking rule
	 */
	public Pattern getNonBreakingPattern(Rule breakingRule) {
		return nonBreakingPatternMap.get(breakingRule);
	}
	
	/**
	 * Creates non breaking pattern string that can be matched in the place 
	 * where breaking rule was matched. Both parts of the rule 
	 * (beforePattern and afterPattern) are incorporated
	 * into one pattern.
	 * beforePattern is used in lookbehind, therefore it needs to be 
	 * modified so it matches finite string (contains no *, + or {n,}). 
	 * @param rule non breaking rule
	 * @return string containing non breaking pattern
	 */
	private String createNonBreakingPatternString(Rule rule) {

		String patternString = document.getCache().get(rule, String.class);
		
		if (patternString == null) {

			StringBuilder patternBuilder = new StringBuilder();
			
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
			
			patternString = patternBuilder.toString();
			
			document.getCache().put(rule, patternString);
			
		}
		
		return patternString;
		
	}
	
}
