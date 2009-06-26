package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.segment.util.Util;

public class RuleManager {
	
	private SrxDocument document;

	private List<Rule> breakingRuleList;
	
	private Map<Rule, Pattern> nonBreakingPaternMap;
	
	public RuleManager(SrxDocument document, String languageCode) {
		this.document = document;
		
		List<LanguageRule> languageRuleList = 
			document.getLanguageRuleList(languageCode);
		
		this.breakingRuleList = new ArrayList<Rule>();
		this.nonBreakingPaternMap = new HashMap<Rule, Pattern>();

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

					nonBreakingPaternMap.put(rule, nonBreakingPattern);
				
				} else {
				
					if (nonBreakingPatternBuilder.length() > 0) {
						nonBreakingPatternBuilder.append('|');
					}

					String patternString = createNonBreakingPatternString(rule);
					
					nonBreakingPatternBuilder.append(patternString);
			
				}
			
			}
		}
		
	}
	
	public List<Rule> getBreakingRuleList() {
		return breakingRuleList;
	}
	
	public Pattern getNonBreakingPattern(Rule breakingRule) {
		return nonBreakingPaternMap.get(breakingRule);
	}
	
	private String createNonBreakingPatternString(Rule rule) {

		String patternString = (String)document.getCache().get(rule);
		
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
