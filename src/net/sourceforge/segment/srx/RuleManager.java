package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.segment.util.Util;

/**
 * Represents segmentation rules manager.
 * Responsible for constructing and storing break and exception rules.
 * 
 * @author loomchild
 */
public class RuleManager {
	
	private SrxDocument document;

	private List<Rule> breakRuleList;
	
	private Map<Rule, Pattern> exceptionPatternMap;
	
	/**
	 * Constructor. Responsible for retrieving rules from SRX document for
	 * given language code, constructing patterns and storing them in 
	 * quick accessible format.
	 * Adds break rules to {@link #breakRuleList} and constructs
	 * corresponding exception patterns in {@link #exceptionPatternMap}.  
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
		
			this.breakRuleList = (List<Rule>)cachedPatterns[0];
			this.exceptionPatternMap = (Map<Rule, Pattern>)cachedPatterns[1];
		
		} else {
		
			this.breakRuleList = new ArrayList<Rule>();
			this.exceptionPatternMap = new HashMap<Rule, Pattern>();

			StringBuilder exceptionPatternBuilder = new StringBuilder();
			
			for (LanguageRule languageRule : languageRuleList) {
				for (Rule rule : languageRule.getRuleList()) {

					if (rule.isBreak()) {
					
						breakRuleList.add(rule);
						
						Pattern exceptionPattern;
						
						if (exceptionPatternBuilder.length() > 0) {
							String exceptionPatternString = 
								exceptionPatternBuilder.toString();
							exceptionPattern = 
								Util.compile(document, exceptionPatternString);
						} else {
							exceptionPattern = null;
						}

						exceptionPatternMap.put(rule, exceptionPattern);
					
					} else {
					
						if (exceptionPatternBuilder.length() > 0) {
							exceptionPatternBuilder.append('|');
						}

						String patternString = createExceptionPatternString(rule);
						
						exceptionPatternBuilder.append(patternString);
				
					}
				
				}
			}

			cachedPatterns = new Object[] {
				this.breakRuleList, this.exceptionPatternMap
			};
			document.getCache().put(languageRuleList, cachedPatterns);
			
		}
		
	}
	
	/**
	 * @return break rule list
	 */
	public List<Rule> getBreakRuleList() {
		return breakRuleList;
	}
	
	/**
	 * @param breakRule
	 * @return exception pattern corresponding to give break rule
	 */
	public Pattern getExceptionPattern(Rule breakRule) {
		return exceptionPatternMap.get(breakRule);
	}
	
	/**
	 * Creates exception pattern string that can be matched in the place 
	 * where break rule was matched. Both parts of the rule 
	 * (beforePattern and afterPattern) are incorporated
	 * into one pattern.
	 * beforePattern is used in lookbehind, therefore it needs to be 
	 * modified so it matches finite string (contains no *, + or {n,}). 
	 * @param rule exception rule
	 * @return string containing exception pattern
	 */
	private String createExceptionPatternString(Rule rule) {

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
