package split.srx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reprezentuje wyrażnie regularne do dzielenia tesktu. 
 * Odpowiada za przechowywanie wzorca i za zbudowanie go z reguł.
 *
 * @author Jarek Lipski (loomchild)
 */
public class SplitPattern {
	
	private Pattern pattern;
	
	public SplitPattern(List<Rule> ruleList) {
		String patternString = createPattern(ruleList);
		this.pattern = Pattern.compile(patternString);
	}
	
	public SplitPattern(LanguageRule languageRule) {
		String patternString = createPattern(languageRule.getRuleList());
		System.out.println(patternString);
		this.pattern = Pattern.compile(patternString);
	}

	public Pattern get() {
		return pattern;
	}
	
	private String createPattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();
		int ruleNr = 0;
		int breakingRuleCount = 0;
		
		for (Rule rule : ruleList) {
			if (rule.isBreaking()) {
				++breakingRuleCount;
			} else if (breakingRuleCount > 0) {
				String rulePattern = 
					createPattern(ruleList, ruleNr, breakingRuleCount);
				patternBuilder.append(rulePattern);
			}
			++ruleNr;
		}
		if (breakingRuleCount > 0) {
			String rulePattern = 
				createPattern(ruleList, ruleNr, breakingRuleCount);
			patternBuilder.append(rulePattern);
		}

		return patternBuilder.toString();
	}
	
	private String createPattern(List<Rule> ruleList, int ruleNr, 
			int breakingRuleCount) {
		StringBuilder patternBuilder = new StringBuilder();

		List<Rule> breakingRuleList = ruleList.subList(
				ruleNr - breakingRuleCount, ruleNr);
		breakingRuleList = 
				mergeSimilarRules(true, breakingRuleList);
		String breakingRulePattern = 
				createBreakingRulePattern(breakingRuleList);
		patternBuilder.append(breakingRulePattern);
		
		List<Rule> nonBreakingRuleList = ruleList.subList(
				0, ruleNr - breakingRuleCount);
		nonBreakingRuleList = 
				mergeSimilarRules(false, nonBreakingRuleList);
		String nonBreakingRulePattern = 
				createNonBreakingRulePattern(nonBreakingRuleList);
		patternBuilder.append(nonBreakingRulePattern);
		
		return patternBuilder.toString();
	}
	
	private String createBreakingRulePattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();
		Iterator<Rule> ruleIterator = ruleList.iterator();
		while (ruleIterator.hasNext()) {
			Rule rule = ruleIterator.next();
			patternBuilder.append(createBreakingRulePattern(rule));
			if (ruleIterator.hasNext()) {
				patternBuilder.append("|");
			}
		}
		return patternBuilder.toString();
	}
	
	private String createBreakingRulePattern(Rule rule) {
		return "(?:" + rule.getBeforePattern() + "(?=" + 
				rule.getAfterPattern() + "))";
	}

	private String createNonBreakingRulePattern(List<Rule> ruleList) {
		StringBuilder patternBuilder = new StringBuilder();
		for (Rule rule : ruleList) {
			patternBuilder.append(createNonBreakingRulePattern(rule));
		}
		return patternBuilder.toString();
	}
	
	private String createNonBreakingRulePattern(Rule rule) {
		return "(?:(?<!" + finitize(rule.getBeforePattern()) + ")|(?!" + 
				rule.getAfterPattern() + "))";
	}

	private String finitize(String pattern) {
		return pattern;
	}

	/**
	 * Skleja ze sobą reguły które mają identyczne ciągi w afterbreak. 
	 * Powoduje to znaczne przyspieszenie wyszukiwania i kompilowania 
	 * (ze względu na mniejszą ilość lookbehind). 
	 * Wybiera z listy tylko reguły podanego typu (łąmiące albo nie).  
	 * @param breaking Typ reguł do sklejenia
	 * @param ruleList Lista reguł
	 * @return Zwraca zoptymalizoewaną listę reguł danego rodzaju.
	 */
	private List<Rule> mergeSimilarRules(boolean breaking, List<Rule> ruleList) {
		//Słownik mapujący afterBreak na beforeBreak budowanych reguł
		Map<String, String> patternMap = new HashMap<String, String>();
		
		for (Rule rule : ruleList) {
			if (rule.isBreaking() == breaking) {
				String beforePattern = rule.getBeforePattern();
				String afterPattern = rule.getAfterPattern();
				String oldBeforePattern = patternMap.get(afterPattern);
				String newBeforePattern; 
				
				if (oldBeforePattern == null) {
					newBeforePattern = "(?:" + beforePattern + ")";
				} else {
					newBeforePattern = oldBeforePattern + "|(?:" + 
							beforePattern + ")";
				}
				patternMap.put(afterPattern, newBeforePattern);
			}
		}
		Set< Map.Entry<String, String> > patternSet = patternMap.entrySet();
		List<Rule> newRuleList = new ArrayList<Rule>(patternSet.size());
		for (Map.Entry<String, String> entry : patternSet) {
			Rule rule = new Rule(breaking, entry.getValue(), entry.getKey());
			newRuleList.add(rule);
		}
		return newRuleList;
	}
	
}
